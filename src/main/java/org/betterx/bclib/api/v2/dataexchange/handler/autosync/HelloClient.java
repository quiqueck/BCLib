package org.betterx.bclib.api.v2.dataexchange.handler.autosync;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.dataexchange.DataExchangeAPI;
import org.betterx.bclib.api.v2.dataexchange.DataHandler;
import org.betterx.bclib.api.v2.dataexchange.DataHandlerDescriptor;
import org.betterx.bclib.client.gui.screens.ModListScreen;
import org.betterx.bclib.client.gui.screens.ProgressScreen;
import org.betterx.bclib.client.gui.screens.SyncFilesScreen;
import org.betterx.bclib.client.gui.screens.WarnBCLibVersionMismatch;
import org.betterx.bclib.config.Configs;
import org.betterx.bclib.config.ServerConfig;
import org.betterx.worlds.together.util.ModUtil;
import org.betterx.worlds.together.util.ModUtil.ModInfo;
import org.betterx.worlds.together.util.PathUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.loader.api.metadata.ModEnvironment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Sent from the Server to the Client.
 * <p>
 * For Details refer to {@link HelloServer}
 */
public class HelloClient extends DataHandler.FromServer {
    public record OfferedModInfo(String version, int size, boolean canDownload) {
    }

    public interface IServerModMap extends Map<String, OfferedModInfo> {
    }

    public static class ServerModMap extends HashMap<String, OfferedModInfo> implements IServerModMap {
    }

    public static final DataHandlerDescriptor DESCRIPTOR = new DataHandlerDescriptor(
            new ResourceLocation(
                    BCLib.MOD_ID,
                    "hello_client"
            ),
            HelloClient::new,
            false,
            false
    );

    public HelloClient() {
        super(DESCRIPTOR.IDENTIFIER);
    }

    static String getBCLibVersion() {
        return ModUtil.getModVersion(BCLib.MOD_ID);
    }

    @Override
    protected boolean prepareDataOnServer() {
        if (!Configs.SERVER_CONFIG.isAllowingAutoSync()) {
            BCLib.LOGGER.info("Auto-Sync was disabled on the server.");
            return false;
        }

        AutoSync.loadSyncFolder();
        return true;
    }

    @Override
    protected void serializeDataOnServer(FriendlyByteBuf buf) {
        final String vbclib = getBCLibVersion();
        BCLib.LOGGER.info("Sending Hello to Client. (server=" + vbclib + ")");

        //write BCLibVersion (=protocol version)
        buf.writeInt(ModUtil.convertModVersion(vbclib));

        if (Configs.SERVER_CONFIG.isOfferingMods() || Configs.SERVER_CONFIG.isOfferingInfosForMods()) {
            List<String> mods = DataExchangeAPI.registeredMods();
            final List<String> inmods = mods;
            if (Configs.SERVER_CONFIG.isOfferingAllMods() || Configs.SERVER_CONFIG.isOfferingInfosForMods()) {
                mods = new ArrayList<>(inmods.size());
                mods.addAll(inmods);
                mods.addAll(ModUtil
                        .getMods()
                        .entrySet()
                        .stream()
                        .filter(entry -> entry.getValue().metadata.getEnvironment() != ModEnvironment.SERVER && !inmods.contains(
                                entry.getKey()))
                        .map(entry -> entry.getKey())
                        .collect(Collectors.toList())
                );
            }

            mods = mods
                    .stream()
                    .filter(entry -> !Configs.SERVER_CONFIG.get(ServerConfig.EXCLUDED_MODS).contains(entry))
                    .collect(Collectors.toList());

            //write Plugin Versions
            buf.writeInt(mods.size());
            for (String modID : mods) {
                final String ver = ModUtil.getModVersion(modID);
                int size = 0;

                final ModInfo mi = ModUtil.getModInfo(modID);
                if (mi != null) {
                    try {
                        size = (int) Files.size(mi.jarPath);
                    } catch (IOException e) {
                        BCLib.LOGGER.error("Unable to get File Size: " + e.getMessage());
                    }
                }


                writeString(buf, modID);
                buf.writeInt(ModUtil.convertModVersion(ver));
                buf.writeInt(size);
                final boolean canDownload = size > 0 && Configs.SERVER_CONFIG.isOfferingMods() && (Configs.SERVER_CONFIG.isOfferingAllMods() || inmods.contains(
                        modID));
                buf.writeBoolean(canDownload);

                if (Configs.MAIN_CONFIG.verboseLogging())
                    BCLib.LOGGER.info("	- Listing Mod " + modID + " v" + ver + " (size: " + PathUtil.humanReadableFileSize(
                            size) + ", download=" + canDownload + ")");
            }
        } else {
            BCLib.LOGGER.info("Server will not list Mods.");
            buf.writeInt(0);
        }

        if (Configs.SERVER_CONFIG.isOfferingFiles() || Configs.SERVER_CONFIG.isOfferingConfigs()) {
            //do only include files that exist on the server
            final List<AutoFileSyncEntry> existingAutoSyncFiles = AutoSync.getAutoSyncFiles()
                                                                          .stream()
                                                                          .filter(e -> e.fileName.exists())
                                                                          .filter(e -> (e.isConfigFile() && Configs.SERVER_CONFIG.isOfferingConfigs()) || (e instanceof AutoFileSyncEntry.ForDirectFileRequest && Configs.SERVER_CONFIG.isOfferingFiles()))
                                                                          .collect(Collectors.toList());

            //send config Data
            buf.writeInt(existingAutoSyncFiles.size());
            for (AutoFileSyncEntry entry : existingAutoSyncFiles) {
                entry.serialize(buf);
                if (Configs.MAIN_CONFIG.verboseLogging())
                    BCLib.LOGGER.info("	- Offering " + (entry.isConfigFile() ? "Config " : "File ") + entry);
            }
        } else {
            BCLib.LOGGER.info("Server will neither offer Files nor Configs.");
            buf.writeInt(0);
        }

        if (Configs.SERVER_CONFIG.isOfferingFiles()) {
            buf.writeInt(AutoSync.syncFolderDescriptions.size());
            AutoSync.syncFolderDescriptions.forEach(desc -> {
                if (Configs.MAIN_CONFIG.verboseLogging())
                    BCLib.LOGGER.info("	- Offering Folder " + desc.localFolder + " (allowDelete=" + desc.removeAdditionalFiles + ")");
                desc.serialize(buf);
            });
        } else {
            BCLib.LOGGER.info("Server will not offer Sync Folders.");
            buf.writeInt(0);
        }

        buf.writeBoolean(Configs.SERVER_CONFIG.isOfferingInfosForMods());
    }

    String bclibVersion = "0.0.0";


    IServerModMap modVersion = new ServerModMap();
    List<AutoSync.AutoSyncTriple> autoSyncedFiles = null;
    List<SyncFolderDescriptor> autoSynFolders = null;
    boolean serverPublishedModInfo = false;

    @Environment(EnvType.CLIENT)
    @Override
    protected void deserializeIncomingDataOnClient(FriendlyByteBuf buf, PacketSender responseSender) {
        //read BCLibVersion (=protocol version)
        bclibVersion = ModUtil.convertModVersion(buf.readInt());

        //read Plugin Versions
        modVersion = new ServerModMap();
        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            final String id = readString(buf);
            final String version = ModUtil.convertModVersion(buf.readInt());
            final int size;
            final boolean canDownload;
            //since v0.4.1 we also send the size of the mod-File
            size = buf.readInt();
            canDownload = buf.readBoolean();
            modVersion.put(id, new OfferedModInfo(version, size, canDownload));
        }

        //read config Data
        count = buf.readInt();
        autoSyncedFiles = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            //System.out.println("Deserializing ");
            AutoSync.AutoSyncTriple t = AutoFileSyncEntry.deserializeAndMatch(buf);
            autoSyncedFiles.add(t);
            //System.out.println(t.first);
        }


        autoSynFolders = new ArrayList<>(1);
        //since v0.4.1 we also send the sync folders
        final int folderCount = buf.readInt();
        for (int i = 0; i < folderCount; i++) {
            SyncFolderDescriptor desc = SyncFolderDescriptor.deserialize(buf);
            autoSynFolders.add(desc);
        }

        serverPublishedModInfo = buf.readBoolean();
    }

    @Environment(EnvType.CLIENT)
    private void processAutoSyncFolder(
            final List<AutoSyncID> filesToRequest,
            final List<AutoSyncID.ForDirectFileRequest> filesToRemove
    ) {
        if (!Configs.CLIENT_CONFIG.isAcceptingFiles()) {
            return;
        }

        if (autoSynFolders.size() > 0) {
            if (Configs.MAIN_CONFIG.verboseLogging())
                BCLib.LOGGER.info("Folders offered by Server:");
        }

        autoSynFolders.forEach(desc -> {
            //desc contains the fileCache sent from the server, load the local version to get hold of the actual file cache on the client
            SyncFolderDescriptor localDescriptor = AutoSync.getSyncFolderDescriptor(desc.folderID);
            if (localDescriptor != null) {
                if (Configs.MAIN_CONFIG.verboseLogging())
                    BCLib.LOGGER.info("	- " + desc.folderID + " (" + desc.localFolder + ", allowRemove=" + desc.removeAdditionalFiles + ")");
                localDescriptor.invalidateCache();

                desc.relativeFilesStream()
                    .filter(desc::discardChildElements)
                    .forEach(subFile -> {
                        if (Configs.MAIN_CONFIG.verboseLogging())
                            BCLib.LOGGER.warning("	   * " + subFile.relPath + " (REJECTED)");
                    });


                if (desc.removeAdditionalFiles) {
                    List<AutoSyncID.ForDirectFileRequest> additionalFiles = localDescriptor.relativeFilesStream()
                                                                                           .filter(subFile -> !desc.hasRelativeFile(
                                                                                                   subFile))
                                                                                           .map(desc::mapAbsolute)
                                                                                           .filter(desc::acceptChildElements)
                                                                                           .map(absPath -> new AutoSyncID.ForDirectFileRequest(
                                                                                                   desc.folderID,
                                                                                                   absPath.toFile()
                                                                                           ))
                                                                                           .collect(Collectors.toList());

                    if (Configs.MAIN_CONFIG.verboseLogging())
                        additionalFiles.forEach(aid -> BCLib.LOGGER.info("	   * " + desc.localFolder.relativize(aid.relFile.toPath()) + " (missing on server)"));
                    filesToRemove.addAll(additionalFiles);
                }

                desc.relativeFilesStream()
                    .filter(desc::acceptChildElements)
                    .forEach(subFile -> {
                        SyncFolderDescriptor.SubFile localSubFile = localDescriptor.getLocalSubFile(subFile.relPath);
                        if (localSubFile != null) {
                            //the file exists locally, check if the hashes match
                            if (!localSubFile.hash.equals(subFile.hash)) {
                                if (Configs.MAIN_CONFIG.verboseLogging())
                                    BCLib.LOGGER.info("	   * " + subFile.relPath + " (changed)");
                                filesToRequest.add(new AutoSyncID.ForDirectFileRequest(
                                        desc.folderID,
                                        new File(subFile.relPath)
                                ));
                            } else {
                                if (Configs.MAIN_CONFIG.verboseLogging())
                                    BCLib.LOGGER.info("	   * " + subFile.relPath);
                            }
                        } else {
                            //the file is missing locally
                            if (Configs.MAIN_CONFIG.verboseLogging())
                                BCLib.LOGGER.info("	   * " + subFile.relPath + " (missing on client)");
                            filesToRequest.add(new AutoSyncID.ForDirectFileRequest(
                                    desc.folderID,
                                    new File(subFile.relPath)
                            ));
                        }
                    });

                //free some memory
                localDescriptor.invalidateCache();
            } else {
                if (Configs.MAIN_CONFIG.verboseLogging())
                    BCLib.LOGGER.info("	- " + desc.folderID + " (Failed to find)");
            }
        });
    }

    @Environment(EnvType.CLIENT)
    private void processSingleFileSync(final List<AutoSyncID> filesToRequest) {
        final boolean debugHashes = Configs.CLIENT_CONFIG.shouldPrintDebugHashes();

        if (autoSyncedFiles.size() > 0) {
            if (Configs.MAIN_CONFIG.verboseLogging())
                BCLib.LOGGER.info("Files offered by Server:");
        }

        //Handle single sync files
        //Single files need to be registered for sync on both client and server
        //There are no restrictions to the target folder, but the client decides the final
        //location.
        for (AutoSync.AutoSyncTriple e : autoSyncedFiles) {
            String actionString = "";
            FileContentWrapper contentWrapper = new FileContentWrapper(e.serverContent);
            if (e.localMatch == null) {
                actionString = "(unknown source -> omitting)";
                //filesToRequest.add(new AutoSyncID(e.serverHash.modID, e.serverHash.uniqueID));
            } else if (e.localMatch.needTransfer.test(e.localMatch.getFileHash(), e.serverHash, contentWrapper)) {
                actionString = "(prepare update)";
                //we did not yet receive the new content
                if (contentWrapper.getRawContent() == null) {
                    filesToRequest.add(new AutoSyncID(e.serverHash.modID, e.serverHash.uniqueID));
                } else {
                    filesToRequest.add(new AutoSyncID.WithContentOverride(
                            e.serverHash.modID,
                            e.serverHash.uniqueID,
                            contentWrapper,
                            e.localMatch.fileName
                    ));
                }
            }
            if (Configs.MAIN_CONFIG.verboseLogging()) {
                BCLib.LOGGER.info("	- " + e + ": " + actionString);
                if (debugHashes) {
                    BCLib.LOGGER.info("	  * " + e.serverHash + " (Server)");
                    BCLib.LOGGER.info("	  * " + e.localMatch.getFileHash() + " (Client)");
                    BCLib.LOGGER.info("	  * local Content " + (contentWrapper.getRawContent() == null));
                }
            }
        }
    }


    @Environment(EnvType.CLIENT)
    private void processModFileSync(final List<AutoSyncID> filesToRequest, final Set<String> mismatchingMods) {
        for (Entry<String, OfferedModInfo> e : modVersion.entrySet()) {
            final String localVersion = ModUtil.convertModVersion(ModUtil.convertModVersion(ModUtil.getModVersion(e.getKey())));
            final OfferedModInfo serverInfo = e.getValue();

            ModInfo nfo = ModUtil.getModInfo(e.getKey());
            final boolean clientOnly = nfo != null && nfo.metadata.getEnvironment() == ModEnvironment.CLIENT;
            final boolean requestMod = !clientOnly && !serverInfo.version.equals(localVersion) && serverInfo.size > 0 && serverInfo.canDownload;

            if (Configs.MAIN_CONFIG.verboseLogging())
                BCLib.LOGGER.info("	- " + e.getKey() + " (client=" + localVersion + ", server=" + serverInfo.version + ", size=" + PathUtil.humanReadableFileSize(
                        serverInfo.size) + (requestMod ? ", requesting" : "") + (serverInfo.canDownload
                        ? ""
                        : ", not offered") + (clientOnly ? ", client only" : "") + ")");
            if (requestMod) {
                filesToRequest.add(new AutoSyncID.ForModFileRequest(e.getKey(), serverInfo.version));
            }
            if (!serverInfo.version.equals(localVersion)) {
                mismatchingMods.add(e.getKey());
            }
        }

        mismatchingMods.addAll(ModListScreen.localMissing(modVersion));
        mismatchingMods.addAll(ModListScreen.serverMissing(modVersion));
    }

    @Override
    protected boolean isBlocking() {
        return true;
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void runOnClientGameThread(Minecraft client) {
        if (!Configs.CLIENT_CONFIG.isAllowingAutoSync()) {
            BCLib.LOGGER.info("Auto-Sync was disabled on the client.");
            return;
        }
        final String localBclibVersion = getBCLibVersion();
        BCLib.LOGGER.info("Received Hello from Server. (client=" + localBclibVersion + ", server=" + bclibVersion + ")");

        if (ModUtil.convertModVersion(localBclibVersion) != ModUtil.convertModVersion(bclibVersion)) {
            showBCLibError(client);
            return;
        }

        final List<AutoSyncID> filesToRequest = new ArrayList<>(2);
        final List<AutoSyncID.ForDirectFileRequest> filesToRemove = new ArrayList<>(2);
        final Set<String> mismatchingMods = new HashSet<>(2);


        processModFileSync(filesToRequest, mismatchingMods);
        processSingleFileSync(filesToRequest);
        processAutoSyncFolder(filesToRequest, filesToRemove);

        //Handle folder sync
        //Both client and server need to know about the folder you want to sync
        //Files can only get placed within that folder

        if ((filesToRequest.size() > 0 || filesToRemove.size() > 0) && (Configs.CLIENT_CONFIG.isAcceptingMods() || Configs.CLIENT_CONFIG.isAcceptingConfigs() || Configs.CLIENT_CONFIG.isAcceptingFiles())) {
            showSyncFilesScreen(client, filesToRequest, filesToRemove);
            return;
        } else if (serverPublishedModInfo && mismatchingMods.size() > 0 && Configs.CLIENT_CONFIG.isShowingModInfo()) {
            client.setScreen(new ModListScreen(
                    client.screen,
                    Component.translatable("title.bclib.modmissmatch"),
                    Component.translatable("message.bclib.modmissmatch"),
                    CommonComponents.GUI_PROCEED,
                    ModUtil.getMods(),
                    modVersion
            ));
            return;
        }
    }

    @Environment(EnvType.CLIENT)
    protected void showBCLibError(Minecraft client) {
        BCLib.LOGGER.error("BCLib differs on client and server.");
        client.setScreen(new WarnBCLibVersionMismatch((download) -> {
            if (download) {
                requestBCLibDownload();

                this.onCloseSyncFilesScreen();
            } else {
                Minecraft.getInstance()
                         .setScreen(null);
            }
        }));
    }

    @Environment(EnvType.CLIENT)
    protected void showSyncFilesScreen(
            Minecraft client,
            List<AutoSyncID> files,
            final List<AutoSyncID.ForDirectFileRequest> filesToRemove
    ) {
        int configFiles = 0;
        int singleFiles = 0;
        int folderFiles = 0;
        int modFiles = 0;

        for (AutoSyncID aid : files) {
            if (aid.isConfigFile()) {
                configFiles++;
            } else if (aid instanceof AutoSyncID.ForModFileRequest) {
                modFiles++;
            } else if (aid instanceof AutoSyncID.ForDirectFileRequest) {
                folderFiles++;
            } else {
                singleFiles++;
            }
        }

        client.setScreen(new SyncFilesScreen(
                modFiles,
                configFiles,
                singleFiles,
                folderFiles,
                filesToRemove.size(),
                modVersion,
                (downloadMods, downloadConfigs, downloadFiles, removeFiles) -> {
                    if (downloadMods || downloadConfigs || downloadFiles) {
                        BCLib.LOGGER.info("Updating local Files:");
                        List<AutoSyncID.WithContentOverride> localChanges = new ArrayList<>(
                                files.toArray().length);
                        List<AutoSyncID> requestFiles = new ArrayList<>(files.toArray().length);

                        files.forEach(aid -> {
                            if (aid.isConfigFile() && downloadConfigs) {
                                processOfferedFile(requestFiles, aid);
                            } else if (aid instanceof AutoSyncID.ForModFileRequest && downloadMods) {
                                processOfferedFile(requestFiles, aid);
                            } else if (downloadFiles) {
                                processOfferedFile(requestFiles, aid);
                            }
                        });

                        requestFileDownloads(requestFiles);
                    }
                    if (removeFiles) {
                        filesToRemove.forEach(aid -> {
                            BCLib.LOGGER.info("	- " + aid.relFile + " (removing)");
                            aid.relFile.delete();
                        });
                    }

                    this.onCloseSyncFilesScreen();
                }
        ));
    }

    @Environment(EnvType.CLIENT)
    private void onCloseSyncFilesScreen() {
        Minecraft.getInstance()
                 .setScreen(ChunkerProgress.getProgressScreen());
    }

    private void processOfferedFile(List<AutoSyncID> requestFiles, AutoSyncID aid) {
        if (aid instanceof AutoSyncID.WithContentOverride) {
            final AutoSyncID.WithContentOverride aidc = (AutoSyncID.WithContentOverride) aid;
            BCLib.LOGGER.info("	- " + aid + " (updating Content)");

            SendFiles.writeSyncedFile(aid, aidc.contentWrapper.getRawContent(), aidc.localFile);
        } else {
            requestFiles.add(aid);
            BCLib.LOGGER.info("	- " + aid + " (requesting)");
        }
    }

    private void requestBCLibDownload() {
        BCLib.LOGGER.warning("Starting download of BCLib");
        requestFileDownloads(List.of(new AutoSyncID.ForModFileRequest(BCLib.MOD_ID, bclibVersion)));
    }

    @Environment(EnvType.CLIENT)
    private void requestFileDownloads(List<AutoSyncID> files) {
        BCLib.LOGGER.info("Starting download of Files:" + files.size());

        final ProgressScreen progress = new ProgressScreen(
                null,
                Component.translatable("title.bclib.filesync.progress"),
                Component.translatable("message.bclib.filesync.progress")
        );
        progress.progressStart(Component.translatable("message.bclib.filesync.progress.stage.empty"));
        ChunkerProgress.setProgressScreen(progress);

        DataExchangeAPI.send(new RequestFiles(files));
    }
}
