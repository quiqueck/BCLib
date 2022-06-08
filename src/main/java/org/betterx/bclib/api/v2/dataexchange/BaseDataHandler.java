package org.betterx.bclib.api.v2.dataexchange;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public abstract class BaseDataHandler {
    private final boolean originatesOnServer;
    @NotNull
    private final ResourceLocation identifier;

    protected BaseDataHandler(ResourceLocation identifier, boolean originatesOnServer) {
        this.originatesOnServer = originatesOnServer;
        this.identifier = identifier;
    }

    final public boolean getOriginatesOnServer() {
        return originatesOnServer;
    }

    final public ResourceLocation getIdentifier() {
        return identifier;
    }

    @Environment(EnvType.CLIENT)
    abstract void receiveFromServer(
            Minecraft client,
            ClientPacketListener handler,
            FriendlyByteBuf buf,
            PacketSender responseSender
    );

    private ServerPlayer lastMessageSender;

    void receiveFromClient(
            MinecraftServer server,
            ServerPlayer player,
            ServerGamePacketListenerImpl handler,
            FriendlyByteBuf buf,
            PacketSender responseSender
    ) {
        lastMessageSender = player;
    }

    final protected boolean reply(BaseDataHandler message, MinecraftServer server) {
        if (lastMessageSender == null) return false;
        message.sendToClient(server, lastMessageSender);
        return true;
    }

    abstract void sendToClient(MinecraftServer server);

    abstract void sendToClient(MinecraftServer server, ServerPlayer player);

    @Environment(EnvType.CLIENT)
    abstract void sendToServer(Minecraft client);

    protected boolean isBlocking() {
        return false;
    }

    @Override
    public String toString() {
        return "BasDataHandler{" + "originatesOnServer=" + originatesOnServer + ", identifier=" + identifier + '}';
    }

    /**
     * Write a String to a buffer (Convenience Method)
     *
     * @param buf The buffer to write to
     * @param s   The String you want to write
     */
    public static void writeString(FriendlyByteBuf buf, String s) {
        buf.writeByteArray(s.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Read a string from a buffer (Convenience Method)
     *
     * @param buf Thea buffer to read from
     * @return The received String
     */
    public static String readString(FriendlyByteBuf buf) {
        byte[] data = buf.readByteArray();
        return new String(data, StandardCharsets.UTF_8);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseDataHandler)) return false;
        BaseDataHandler that = (BaseDataHandler) o;
        return originatesOnServer == that.originatesOnServer && identifier.equals(that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originatesOnServer, identifier);
    }
}

