package org.betterx.bclib.api.v2.dataexchange.handler.autosync;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.dataexchange.DataHandler;
import org.betterx.bclib.api.v2.dataexchange.DataHandlerDescriptor;
import org.betterx.bclib.client.gui.screens.ConfirmRestartScreen;
import org.betterx.bclib.config.Configs;
import org.betterx.bclib.util.Pair;
import org.betterx.bclib.util.Triple;
import org.betterx.worlds.together.util.PathUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SendFiles extends DataHandler.FromServer<SendFiles.SendFilesPayload> {
    public static class SendFilesPayload extends DataHandlerDescriptor.PacketPayload<SendFilesPayload> {
        public final String token;
        public final List<AutoFileSyncEntry> files;
        public final List<Pair<AutoFileSyncEntry, byte[]>> incomingFiles;

        protected SendFilesPayload(String token, List<AutoFileSyncEntry> files) {
            super(DESCRIPTOR);
            this.token = token;
            this.files = files;
            this.incomingFiles = null;
        }

        SendFilesPayload(FriendlyByteBuf buf) {
            super(DESCRIPTOR);
            this.files = null;
            this.token = readString(buf);
            if (!token.equals(RequestFiles.currentToken)) {
                RequestFiles.newToken();
                BCLib.LOGGER.error("Unrequested File Transfer!");
                this.incomingFiles = new ArrayList<>(0);
                return;
            }
            RequestFiles.newToken();

            if (Configs.CLIENT_CONFIG.isAcceptingConfigs() || Configs.CLIENT_CONFIG.isAcceptingFiles() || Configs.CLIENT_CONFIG.isAcceptingMods()) {
                int size = buf.readInt();
                this.incomingFiles = new ArrayList<>(size);
                if (Configs.MAIN_CONFIG.verboseLogging())
                    BCLib.LOGGER.info("Server sent " + size + " Files:");
                for (int i = 0; i < size; i++) {
                    Triple<AutoFileSyncEntry, byte[], AutoSyncID> p = AutoFileSyncEntry.deserializeContent(buf);
                    if (p.first != null) {
                        final String type;
                        if (p.first.isConfigFile() && Configs.CLIENT_CONFIG.isAcceptingConfigs()) {
                            this.incomingFiles.add(p);
                            type = "Accepted Config ";
                        } else if (p.first instanceof AutoFileSyncEntry.ForModFileRequest && Configs.CLIENT_CONFIG.isAcceptingMods()) {
                            this.incomingFiles.add(p);
                            type = "Accepted Mod ";
                        } else if (Configs.CLIENT_CONFIG.isAcceptingFiles()) {
                            this.incomingFiles.add(p);
                            type = "Accepted File ";
                        } else {
                            type = "Ignoring ";
                        }
                        if (Configs.MAIN_CONFIG.verboseLogging())
                            BCLib.LOGGER.info("	- " + type + p.first + " (" + PathUtil.humanReadableFileSize(p.second.length) + ")");
                    } else {
                        if (Configs.MAIN_CONFIG.verboseLogging())
                            BCLib.LOGGER.error("   - Failed to receive File " + p.third + ", possibly sent from a Mod that is not installed on the client.");
                    }
                }
            } else {
                this.incomingFiles = List.of();
            }
        }

        protected void write(FriendlyByteBuf buf) {
            writeString(buf, this.token);
            buf.writeInt(this.files.size());

            if (Configs.MAIN_CONFIG.verboseLogging())
                BCLib.LOGGER.info("Sending " + this.files.size() + " Files to Client:");

            for (AutoFileSyncEntry entry : this.files) {
                int length = entry.serializeContent(buf);
                if (Configs.MAIN_CONFIG.verboseLogging())
                    BCLib.LOGGER.info("	- " + entry + " (" + PathUtil.humanReadableFileSize(length) + ")");
            }
        }
    }

    public static final DataHandlerDescriptor<SendFilesPayload> DESCRIPTOR = new DataHandlerDescriptor<>(
            DataHandlerDescriptor.Direction.SERVER_TO_CLIENT,
            ResourceLocation.fromNamespaceAndPath(
                    BCLib.MOD_ID,
                    "send_files"
            ),
            SendFilesPayload::new,
            SendFiles::new,
            false,
            false
    );

    protected List<AutoFileSyncEntry> files;
    private String token;

    public SendFiles() {
        this(null, "");
    }

    public SendFiles(List<AutoFileSyncEntry> files, String token) {
        super(DESCRIPTOR.IDENTIFIER.id());
        this.files = files;
        this.token = token;
    }

    @Override
    protected boolean prepareDataOnServer() {
        if (!Configs.SERVER_CONFIG.isAllowingAutoSync()) {
            BCLib.LOGGER.info("Auto-Sync was disabled on the server.");
            return false;
        }

        return true;
    }

    @Override
    protected SendFilesPayload serializeDataOnServer() {
        List<AutoFileSyncEntry> existingFiles = files.stream()
                                                     .filter(e -> e != null && e.fileName != null && e.fileName.exists())
                                                     .collect(Collectors.toList());

		/*
		//this will try to send a file that was not registered or requested by the client
		existingFiles.add(new AutoFileSyncEntry("none", new File("D:\\MinecraftPlugins\\BetterNether\\run\\server.properties"),true,(a, b, content) -> {
			System.out.println("Got Content:" + content.length);
			return true;
		}));*/
		
		/*//this will try to send a folder-file that was not registered or requested by the client
		existingFiles.add(new AutoFileSyncEntry.ForDirectFileRequest(DataExchange.SYNC_FOLDER.folderID, new File("test.json"), DataExchange.SYNC_FOLDER.mapAbsolute("test.json").toFile()));*/
		
		/*//this will try to send a folder-file that was not registered or requested by the client and is outside the base-folder
		existingFiles.add(new AutoFileSyncEntry.ForDirectFileRequest(DataExchange.SYNC_FOLDER.folderID, new File("../breakout.json"), DataExchange.SYNC_FOLDER.mapAbsolute("../breakout.json").toFile()));*/


        return new SendFilesPayload(token, existingFiles);
    }

    private List<Pair<AutoFileSyncEntry, byte[]>> receivedFiles;

    @Environment(EnvType.CLIENT)
    @Override
    protected void deserializeIncomingDataOnClient(SendFilesPayload payload, PacketSender responseSender) {
        this.token = payload.token;
        this.receivedFiles = payload.incomingFiles;
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void runOnClientGameThread(Minecraft client) {
        if (Configs.CLIENT_CONFIG.isAcceptingConfigs() || Configs.CLIENT_CONFIG.isAcceptingFiles() || Configs.CLIENT_CONFIG.isAcceptingMods()) {
            BCLib.LOGGER.info("Writing Files:");

            for (Pair<AutoFileSyncEntry, byte[]> entry : receivedFiles) {
                final AutoFileSyncEntry e = entry.first;
                final byte[] data = entry.second;

                writeSyncedFile(e, data, e.fileName);
            }

            showConfirmRestart(client);
        }
    }


    @Environment(EnvType.CLIENT)
    static void writeSyncedFile(AutoSyncID e, byte[] data, File fileName) {
        if (fileName != null && !PathUtil.isChildOf(PathUtil.GAME_FOLDER, fileName.toPath())) {
            BCLib.LOGGER.error(fileName + " is not within game folder " + PathUtil.GAME_FOLDER);
            return;
        }

        if (!PathUtil.MOD_BAK_FOLDER.toFile().exists()) {
            PathUtil.MOD_BAK_FOLDER.toFile().mkdirs();
        }

        Path path = fileName != null ? fileName.toPath() : null;
        Path removeAfter = null;
        if (e instanceof AutoFileSyncEntry.ForModFileRequest mase) {
            removeAfter = path;
            int count = 0;
            final String prefix = "_bclib_synced_";
            String name = prefix + mase.modID + "_" + mase.version.replace(".", "_") + ".jar";
            do {
                if (path != null) {
                    //move to the same directory as the existing Mod
                    path = path.getParent()
                               .resolve(name);
                } else {
                    //move to the default mode location
                    path = PathUtil.MOD_FOLDER.resolve(name);
                }
                count++;
                name = prefix + mase.modID + "_" + mase.version.replace(".", "_") + "__" + String.format(
                        "%03d",
                        count
                ) + ".jar";
            } while (path.toFile().exists());
        }

        BCLib.LOGGER.info("	- Writing " + path + " (" + PathUtil.humanReadableFileSize(data.length) + ")");
        try {
            final File parentFile = path.getParent()
                                        .toFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            Files.write(path, data);
            if (removeAfter != null) {
                final String bakFileName = removeAfter.toFile().getName();
                String collisionFreeName = bakFileName;
                Path targetPath;
                int count = 0;
                do {
                    targetPath = PathUtil.MOD_BAK_FOLDER.resolve(collisionFreeName);
                    count++;
                    collisionFreeName = String.format("%03d", count) + "_" + bakFileName;
                } while (targetPath.toFile().exists());

                BCLib.LOGGER.info("	- Moving " + removeAfter + " to " + targetPath);
                removeAfter.toFile().renameTo(targetPath.toFile());
            }
            AutoSync.didReceiveFile(e, fileName);


        } catch (IOException ioException) {
            BCLib.LOGGER.error("	--> Writing " + fileName + " failed: " + ioException);
        }
    }

    @Environment(EnvType.CLIENT)
    protected void showConfirmRestart(Minecraft client) {
        client.setScreen(new ConfirmRestartScreen(() -> {
            Minecraft.getInstance()
                     .setScreen(null);
            client.stop();
        }));

    }
}
