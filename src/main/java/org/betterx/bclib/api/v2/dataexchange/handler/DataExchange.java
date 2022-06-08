package org.betterx.bclib.api.v2.dataexchange.handler;

import org.betterx.bclib.api.v2.dataexchange.*;

import net.minecraft.resources.ResourceLocation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import java.util.HashSet;
import java.util.Set;

abstract public class DataExchange {


    private static DataExchangeAPI instance;

    protected static DataExchangeAPI getInstance() {
        if (instance == null) {
            instance = new DataExchangeAPI();
        }
        return instance;
    }

    protected ConnectorServerside server;
    protected ConnectorClientside client;
    protected final Set<DataHandlerDescriptor> descriptors;


    private final boolean didLoadSyncFolder = false;

    abstract protected ConnectorClientside clientSupplier(DataExchange api);

    abstract protected ConnectorServerside serverSupplier(DataExchange api);

    protected DataExchange() {
        descriptors = new HashSet<>();
    }

    public Set<DataHandlerDescriptor> getDescriptors() {
        return descriptors;
    }

    public static DataHandlerDescriptor getDescriptor(ResourceLocation identifier) {
        return getInstance().descriptors.stream().filter(d -> d.equals(identifier)).findFirst().orElse(null);
    }

    @Environment(EnvType.CLIENT)
    protected void initClientside() {
        if (client != null) return;
        client = clientSupplier(this);

        ClientPlayConnectionEvents.INIT.register(client::onPlayInit);
        ClientPlayConnectionEvents.JOIN.register(client::onPlayReady);
        ClientPlayConnectionEvents.DISCONNECT.register(client::onPlayDisconnect);
    }

    protected void initServerSide() {
        if (server != null) return;
        server = serverSupplier(this);

        ServerPlayConnectionEvents.INIT.register(server::onPlayInit);
        ServerPlayConnectionEvents.JOIN.register(server::onPlayReady);
        ServerPlayConnectionEvents.DISCONNECT.register(server::onPlayDisconnect);
    }

    /**
     * Initializes all datastructures that need to exist in the client component.
     * <p>
     * This is automatically called by BCLib. You can register {@link DataHandler}-Objects before this Method is called
     */
    @Environment(EnvType.CLIENT)
    public static void prepareClientside() {
        DataExchange api = DataExchange.getInstance();
        api.initClientside();

    }

    /**
     * Initializes all datastructures that need to exist in the server component.
     * <p>
     * This is automatically called by BCLib. You can register {@link DataHandler}-Objects before this Method is called
     */
    public static void prepareServerside() {
        DataExchange api = DataExchange.getInstance();
        api.initServerSide();
    }


    /**
     * Automatically called before the player enters the world.
     * <p>
     * This is automatically called by BCLib. It will send all {@link DataHandler}-Objects that have {@link DataHandlerDescriptor#sendBeforeEnter} set to*
     * {@code true},
     */
    @Environment(EnvType.CLIENT)
    public static void sendOnEnter() {
        getInstance().descriptors.forEach((desc) -> {
            if (desc.sendBeforeEnter) {
                BaseDataHandler h = desc.JOIN_INSTANCE.get();
                if (!h.getOriginatesOnServer()) {
                    getInstance().client.sendToServer(h);
                }
            }
        });
    }


}
