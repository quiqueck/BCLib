package org.betterx.bclib.api.v2.dataexchange;

import org.betterx.bclib.BCLib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.Collection;
import java.util.List;

public abstract class DataHandler<T extends CustomPacketPayload> extends BaseDataHandler<T> {
    public abstract static class WithoutPayload<T extends CustomPacketPayload> extends DataHandler<T> {
        protected WithoutPayload(ResourceLocation identifier, boolean originatesOnServer) {
            super(identifier, originatesOnServer);
        }

        @Override
        protected boolean prepareData(boolean isClient) {
            return true;
        }

        @Override
        protected T serializeData(boolean isClient) {
            return null;
        }

        @Override
        protected void deserializeIncomingData(
                T payload,
                PacketSender responseSender,
                boolean isClient
        ) {
        }
    }

    protected DataHandler(ResourceLocation identifier, boolean originatesOnServer) {
        super(identifier, originatesOnServer);
    }

    protected boolean prepareData(boolean isClient) {
        return true;
    }

    abstract protected T serializeData(boolean isClient);

    abstract protected void deserializeIncomingData(
            T payload,
            PacketSender responseSender,
            boolean isClient
    );

    abstract protected void runOnGameThread(Minecraft client, MinecraftServer server, boolean isClient);

    @Environment(EnvType.CLIENT)
    @Override
    void receiveFromServer(
            Minecraft client,
            ClientPacketListener handler,
            T payload,
            PacketSender responseSender
    ) {
        deserializeIncomingData(payload, responseSender, true);
        final Runnable runner = () -> runOnGameThread(client, null, true);

        if (isBlocking()) client.executeBlocking(runner);
        else client.execute(runner);
    }

    @Override
    void receiveFromClient(
            MinecraftServer server,
            ServerPlayer player,
            ServerGamePacketListenerImpl handler,
            T payload,
            PacketSender responseSender
    ) {
        super.receiveFromClient(server, player, handler, payload, responseSender);

        deserializeIncomingData(payload, responseSender, false);
        final Runnable runner = () -> runOnGameThread(null, server, false);

        if (isBlocking()) server.executeBlocking(runner);
        else server.execute(runner);
    }

    @Override
    void sendToClient(MinecraftServer server) {
        if (prepareData(false)) {
            T obj = serializeData(false);
            _sendToClient(getIdentifier(), server, PlayerLookup.all(server), obj);
        }
    }

    @Override
    void sendToClient(MinecraftServer server, ServerPlayer player) {
        if (prepareData(false)) {
            T obj = serializeData(false);
            _sendToClient(getIdentifier(), server, List.of(player), obj);
        }
    }


    public static <T extends CustomPacketPayload> void _sendToClient(
            ResourceLocation identifier,
            MinecraftServer server,
            Collection<ServerPlayer> players,
            T payload
    ) {
        if (payload == null) return;
        for (ServerPlayer player : players) {
            ServerPlayNetworking.send(player, payload);
        }

    }

    @Environment(EnvType.CLIENT)
    @Override
    void sendToServer(Minecraft client) {
        if (prepareData(true)) {
            T obj = serializeData(true);
            ClientPlayNetworking.send(obj);
        }
    }

    /**
     * A Message that always originates on the Client
     */
    public abstract static class FromClient<T extends CustomPacketPayload> extends BaseDataHandler<T> {
        public abstract static class WithoutPayload<T extends CustomPacketPayload> extends FromClient<T> {
            protected WithoutPayload(ResourceLocation identifier) {
                super(identifier);
            }

            @Override
            protected boolean prepareDataOnClient() {
                return true;
            }

            @Override
            protected T serializeDataOnClient() {
                return null;
            }

            @Override
            protected void deserializeIncomingDataOnServer(T payload, Player player, PacketSender responseSender) {

            }
        }

        protected FromClient(ResourceLocation identifier) {
            super(identifier, false);
        }

        @Environment(EnvType.CLIENT)
        protected boolean prepareDataOnClient() {
            return true;
        }

        @Environment(EnvType.CLIENT)
        abstract protected T serializeDataOnClient();

        protected abstract void deserializeIncomingDataOnServer(
                T payload,
                Player player,
                PacketSender responseSender
        );

        protected abstract void runOnServerGameThread(MinecraftServer server, Player player);

        @Override
        void receiveFromServer(
                Minecraft client,
                ClientPacketListener handler,
                CustomPacketPayload payload,
                PacketSender responseSender
        ) {
            BCLib.LOGGER.error("[Internal Error] The message '" + getIdentifier() + "' must originate from the client!");
        }

        @Override
        void receiveFromClient(
                MinecraftServer server,
                ServerPlayer player,
                ServerGamePacketListenerImpl handler,
                T payload,
                PacketSender responseSender
        ) {
            super.receiveFromClient(server, player, handler, payload, responseSender);

            deserializeIncomingDataOnServer(payload, player, responseSender);
            final Runnable runner = () -> runOnServerGameThread(server, player);

            if (isBlocking()) server.executeBlocking(runner);
            else server.execute(runner);
        }

        @Override
        void sendToClient(MinecraftServer server) {
            BCLib.LOGGER.error("[Internal Error] The message '" + getIdentifier() + "' must originate from the client!");
        }

        @Override
        void sendToClient(MinecraftServer server, ServerPlayer player) {
            BCLib.LOGGER.error("[Internal Error] The message '" + getIdentifier() + "' must originate from the client!");
        }

        @Environment(EnvType.CLIENT)
        @Override
        void sendToServer(Minecraft client) {
            if (prepareDataOnClient()) {
                T obj = serializeDataOnClient();
                if (obj != null) ClientPlayNetworking.send(obj);
            }
        }
    }

    /**
     * A Message that always originates on the Server
     */
    public abstract static class FromServer<T extends CustomPacketPayload> extends BaseDataHandler<T> {
        public abstract static class WithoutPayload<T extends CustomPacketPayload> extends FromServer<T> {
            protected WithoutPayload(ResourceLocation identifier) {
                super(identifier);
            }

            @Override
            protected boolean prepareDataOnServer() {
                return true;
            }

            @Override
            protected T serializeDataOnServer() {
                return null;
            }

            @Override
            protected void deserializeIncomingDataOnClient(T payload, PacketSender responseSender) {
            }
        }

        protected FromServer(ResourceLocation identifier) {
            super(identifier, true);
        }

        protected boolean prepareDataOnServer() {
            return true;
        }

        abstract protected T serializeDataOnServer();

        @Environment(EnvType.CLIENT)
        abstract protected void deserializeIncomingDataOnClient(T payload, PacketSender responseSender);

        @Environment(EnvType.CLIENT)
        abstract protected void runOnClientGameThread(Minecraft client);


        @Override
        void receiveFromServer(
                Minecraft client,
                ClientPacketListener handler,
                T payload,
                PacketSender responseSender
        ) {
            deserializeIncomingDataOnClient(payload, responseSender);
            final Runnable runner = () -> runOnClientGameThread(client);

            if (isBlocking()) client.executeBlocking(runner);
            else client.execute(runner);
        }

        @Override
        void receiveFromClient(
                MinecraftServer server,
                ServerPlayer player,
                ServerGamePacketListenerImpl handler,
                T payload,
                PacketSender responseSender
        ) {
            super.receiveFromClient(server, player, handler, payload, responseSender);
            BCLib.LOGGER.error("[Internal Error] The message '" + getIdentifier() + "' must originate from the server!");
        }

        public void receiveFromMemory(T payload) {
            receiveFromServer(Minecraft.getInstance(), null, payload, null);
        }

        @Override
        final void sendToClient(MinecraftServer server) {
            if (prepareDataOnServer()) {
                T obj = serializeDataOnServer();

                _sendToClient(getIdentifier(), server, PlayerLookup.all(server), obj);
            }
        }

        @Override
        final void sendToClient(MinecraftServer server, ServerPlayer player) {
            if (prepareDataOnServer()) {
                T obj = serializeDataOnServer();
                _sendToClient(getIdentifier(), server, List.of(player), obj);
            }
        }

        @Environment(EnvType.CLIENT)
        @Override
        final void sendToServer(Minecraft client) {
            BCLib.LOGGER.error("[Internal Error] The message '" + getIdentifier() + "' must originate from the server!");
        }
    }
}
