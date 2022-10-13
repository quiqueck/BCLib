package org.betterx.bclib.api.v2.dataexchange;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.dataexchange.handler.DataExchange;
import org.betterx.bclib.api.v2.dataexchange.handler.autosync.AutoSync;
import org.betterx.bclib.api.v2.dataexchange.handler.autosync.AutoSyncID;
import org.betterx.bclib.config.Config;
import org.betterx.bclib.config.Configs;
import org.betterx.worlds.together.util.ModUtil;

import net.minecraft.network.FriendlyByteBuf;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;
import java.util.function.BiConsumer;

public class DataExchangeAPI extends DataExchange {
    private final static List<String> MODS = Lists.newArrayList();

    /**
     * You should never need to create a custom instance of this Object.
     */
    public DataExchangeAPI() {
        super();
    }

    @Environment(EnvType.CLIENT)
    protected ConnectorClientside clientSupplier(DataExchange api) {
        return new ConnectorClientside(api);
    }

    protected ConnectorServerside serverSupplier(DataExchange api) {
        return new ConnectorServerside(api);
    }

    /**
     * Register a mod to participate in the DataExchange.
     *
     * @param modID - {@link String} modID.
     */
    public static void registerMod(String modID) {
        if (!MODS.contains(modID)) MODS.add(modID);
    }

    /**
     * Register a mod dependency to participate in the DataExchange.
     *
     * @param modID - {@link String} modID.
     */
    public static void registerModDependency(String modID) {
        if (ModUtil.getModInfo(modID, false) != null && !"0.0.0".equals(ModUtil.getModVersion(modID))) {
            registerMod(modID);
        } else {
            if (Configs.MAIN_CONFIG.verboseLogging())
                BCLib.LOGGER.info("Mod Dependency '" + modID + "' not found. This is probably OK.");
        }
    }

    /**
     * Returns the IDs of all registered Mods.
     *
     * @return List of modIDs
     */
    public static List<String> registeredMods() {
        return MODS;
    }

    /**
     * Add a new Descriptor for a {@link DataHandler}.
     *
     * @param desc The Descriptor you want to add.
     */
    public static void registerDescriptor(DataHandlerDescriptor desc) {
        DataExchange api = DataExchange.getInstance();
        api.getDescriptors()
           .add(desc);
    }

    /**
     * Bulk-Add a Descriptors for your {@link DataHandler}-Objects.
     *
     * @param desc The Descriptors you want to add.
     */
    public static void registerDescriptors(List<DataHandlerDescriptor> desc) {
        DataExchange api = DataExchange.getInstance();
        api.getDescriptors()
           .addAll(desc);
    }

    /**
     * Sends the Handler.
     * <p>
     * Depending on what the result of {@link DataHandler#getOriginatesOnServer()}, the Data is sent from the server
     * to the client (if {@code true}) or the other way around.
     * <p>
     * The method {@link DataHandler#serializeData(FriendlyByteBuf, boolean)} is called just before the data is sent. You should
     * use this method to add the Data you need to the communication.
     *
     * @param h The Data that you want to send
     */
    public static void send(BaseDataHandler h) {
        if (h.getOriginatesOnServer()) {
            DataExchangeAPI.getInstance().server.sendToClient(h);
        } else {
            DataExchangeAPI.getInstance().client.sendToServer(h);
        }
    }

    /**
     * Registers a File for automatic client syncing.
     *
     * @param modID    The ID of the calling Mod
     * @param fileName The name of the File
     */
    public static void addAutoSyncFile(String modID, File fileName) {
        AutoSync.addAutoSyncFileData(modID, fileName, false, SyncFileHash.NEED_TRANSFER);
    }

    /**
     * Registers a File for automatic client syncing.
     * <p>
     * The file is synced of the {@link SyncFileHash} on client and server are not equal. This method will not copy the
     * configs content from the client to the server.
     *
     * @param modID    The ID of the calling Mod
     * @param uniqueID A unique Identifier for the File. (see {@link SyncFileHash#uniqueID} for
     *                 Details
     * @param fileName The name of the File
     */
    public static void addAutoSyncFile(String modID, String uniqueID, File fileName) {
        AutoSync.addAutoSyncFileData(modID, uniqueID, fileName, false, SyncFileHash.NEED_TRANSFER);
    }

    /**
     * Registers a File for automatic client syncing.
     * <p>
     * The content of the file is requested for comparison. This will copy the
     * entire file from the client to the server.
     * <p>
     * You should only use this option, if you need to compare parts of the file in order to decide
     * if the File needs to be copied. Normally using the {@link SyncFileHash}
     * for comparison is sufficient.
     *
     * @param modID        The ID of the calling Mod
     * @param fileName     The name of the File
     * @param needTransfer If the predicate returns true, the file needs to get copied to the server.
     */
    public static void addAutoSyncFile(String modID, File fileName, AutoSync.NeedTransferPredicate needTransfer) {
        AutoSync.addAutoSyncFileData(modID, fileName, true, needTransfer);
    }

    /**
     * Registers a File for automatic client syncing.
     * <p>
     * The content of the file is requested for comparison. This will copy the
     * entire file from the client to the server.
     * <p>
     * You should only use this option, if you need to compare parts of the file in order to decide
     * if the File needs to be copied. Normally using the {@link SyncFileHash}
     * for comparison is sufficient.
     *
     * @param modID        The ID of the calling Mod
     * @param uniqueID     A unique Identifier for the File. (see {@link SyncFileHash#uniqueID} for
     *                     Details
     * @param fileName     The name of the File
     * @param needTransfer If the predicate returns true, the file needs to get copied to the server.
     */
    public static void addAutoSyncFile(
            String modID,
            String uniqueID,
            File fileName,
            AutoSync.NeedTransferPredicate needTransfer
    ) {
        AutoSync.addAutoSyncFileData(modID, uniqueID, fileName, true, needTransfer);
    }

    /**
     * Register a function that is called whenever the client receives a file from the server and replaced toe local
     * file with the new content.
     * <p>
     * This callback is usefull if you need to reload the new content before the game is quit.
     *
     * @param callback A Function that receives the AutoSyncID as well as the Filename.
     */
    public static void addOnWriteCallback(BiConsumer<AutoSyncID, File> callback) {
        AutoSync.addOnWriteCallback(callback);
    }

    /**
     * Returns the sync-folder for a given Mod.
     * <p>
     * BCLib will ensure that the contents of sync-folder on the client is the same as the one on the server.
     *
     * @param modID ID of the Mod
     * @return The path to the sync-folder
     */
    public static File getModSyncFolder(String modID) {
        File fl = AutoSync.SYNC_FOLDER.localFolder.resolve(modID.replace(".", "-")
                                                                .replace(":", "-")
                                                                .replace("\\", "-")
                                                                .replace("/", "-"))
                                                  .normalize()
                                                  .toFile();

        if (!fl.exists()) {
            fl.mkdirs();
        }
        return fl;
    }

    static {
        addOnWriteCallback(Config::reloadSyncedConfig);
    }
}
