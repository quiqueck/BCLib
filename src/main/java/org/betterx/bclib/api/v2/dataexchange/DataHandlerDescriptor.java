package org.betterx.bclib.api.v2.dataexchange;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.Objects;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public class DataHandlerDescriptor<T extends CustomPacketPayload> {
    public enum Direction {
        CLIENT_TO_SERVER,
        SERVER_TO_CLIENT
    }

    public static abstract class PacketHandler<T extends CustomPacketPayload> {
        public abstract void write(T payload, FriendlyByteBuf buf);
        public abstract T read(FriendlyByteBuf buf);

        void receiveFromServer(
                DataHandlerDescriptor<?> desc,
                Object payload,
                ClientPlayNetworking.Context context
        ) {
            BaseDataHandler h = desc.INSTANCE.get();
            try (Minecraft client = context.client()) {
                //noinspection unchecked
                h.receiveFromServer(
                        client,
                        client.getConnection(),
                        (T) payload,
                        context.responseSender()
                );
            }
        }

        void receiveFromClient(
                DataHandlerDescriptor<?> desc,
                Object payload,
                ServerPlayNetworking.Context context
        ) {
            BaseDataHandler h = desc.INSTANCE.get();
            //noinspection unchecked
            h.receiveFromClient(
                    context.player().server,
                    context.player(),
                    context.player().connection,
                    (T) payload,
                    context.responseSender()
            );
        }
    }

    public abstract static class PacketPayload<T extends PacketPayload> implements CustomPacketPayload {
        private final DataHandlerDescriptor<T> DESCRIPTOR;

        protected PacketPayload(DataHandlerDescriptor<T> desc) {
            this.DESCRIPTOR = desc;
        }

        @Override
        public final Type<T> type() {
            return this.DESCRIPTOR.IDENTIFIER;
        }
    }

    public DataHandlerDescriptor(
            @NotNull Direction direction,
            @NotNull ResourceLocation identifier,
            @NotNull PacketHandler<T> packetHandler,
            @NotNull Supplier<BaseDataHandler> instancer
    ) {
        this(direction, identifier, packetHandler, instancer, instancer, false, false);
    }

    public DataHandlerDescriptor(
            @NotNull Direction direction,
            @NotNull ResourceLocation identifier,
            @NotNull PacketHandler<T> packetHandler,
            @NotNull Supplier<BaseDataHandler> instancer,
            boolean sendOnJoin,
            boolean sendBeforeEnter
    ) {
        this(direction, identifier, packetHandler, instancer, instancer, sendOnJoin, sendBeforeEnter);
    }

    public DataHandlerDescriptor(
            @NotNull Direction direction,
            @NotNull ResourceLocation identifier,
            @NotNull PacketHandler<T> packetHandler,
            @NotNull Supplier<BaseDataHandler> receiv_instancer,
            @NotNull Supplier<BaseDataHandler> join_instancer,
            boolean sendOnJoin,
            boolean sendBeforeEnter
    ) {
        this.DIRECTION = direction;
        this.INSTANCE = receiv_instancer;
        this.JOIN_INSTANCE = join_instancer;
        this.IDENTIFIER = new CustomPacketPayload.Type<>(identifier);

        this.sendOnJoin = sendOnJoin;
        this.sendBeforeEnter = sendBeforeEnter;

        this.PACKET_HANDLER = packetHandler;
        this.STREAM_CODEC = CustomPacketPayload.codec(packetHandler::write, packetHandler::read);

        if (direction == Direction.SERVER_TO_CLIENT)
            PayloadTypeRegistry.playS2C().register(this.IDENTIFIER, STREAM_CODEC);
        else if (direction == Direction.SERVER_TO_CLIENT)
            PayloadTypeRegistry.playC2S().register(this.IDENTIFIER, STREAM_CODEC);
    }

    public final Direction DIRECTION;
    @NotNull
    public final StreamCodec<FriendlyByteBuf, T> STREAM_CODEC;
    public final boolean sendOnJoin;
    public final boolean sendBeforeEnter;
    @NotNull
    public final PacketHandler<T> PACKET_HANDLER;
    @NotNull
    public final CustomPacketPayload.Type<T> IDENTIFIER;
    @NotNull
    public final Supplier<BaseDataHandler> INSTANCE;
    @NotNull
    public final Supplier<BaseDataHandler> JOIN_INSTANCE;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof ResourceLocation) {
            return o.equals(IDENTIFIER);
        }
        if (!(o instanceof DataHandlerDescriptor that)) return false;
        return IDENTIFIER.equals(that.IDENTIFIER);
    }

    @Override
    public int hashCode() {
        return Objects.hash(IDENTIFIER);
    }
}
