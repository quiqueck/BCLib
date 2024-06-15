package org.betterx.bclib.api.v2.dataexchange;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.dataexchange.handler.DataExchange;
import org.betterx.worlds.together.util.ModUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.google.common.collect.Lists;

import java.util.List;

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
            BCLib.LOGGER.verbose("Mod Dependency '" + modID + "' not found. This is probably OK.");
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
}
