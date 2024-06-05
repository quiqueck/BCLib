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

public class DataHandlerDescriptor<T extends DataHandlerDescriptor.PacketPayload<T>> {
    public enum Direction {
        CLIENT_TO_SERVER,
        SERVER_TO_CLIENT
    }

    public interface PayloadFactory<T extends PacketPayload<T>> {
        T create(FriendlyByteBuf buf);
    }

    public abstract static class PacketPayload<T extends PacketPayload<T>> implements CustomPacketPayload {
        protected final DataHandlerDescriptor<T> descriptor;

        protected PacketPayload(DataHandlerDescriptor<T> desc) {
            this.descriptor = desc;
        }

        protected abstract void write(FriendlyByteBuf buf);

        @Override
        public final @NotNull Type<T> type() {
            return this.descriptor.IDENTIFIER;
        }
    }

    public DataHandlerDescriptor(
            @NotNull Direction direction,
            @NotNull ResourceLocation identifier,
            @NotNull PayloadFactory<T> factory,
            @NotNull Supplier<BaseDataHandler<T>> instancer
    ) {
        this(direction, identifier, factory, instancer, instancer, false, false);
    }

    public DataHandlerDescriptor(
            @NotNull Direction direction,
            @NotNull ResourceLocation identifier,
            @NotNull PayloadFactory<T> factory,
            @NotNull Supplier<BaseDataHandler<T>> instancer,
            boolean sendOnJoin,
            boolean sendBeforeEnter
    ) {
        this(direction, identifier, factory, instancer, instancer, sendOnJoin, sendBeforeEnter);
    }

    public DataHandlerDescriptor(
            @NotNull Direction direction,
            @NotNull ResourceLocation identifier,
            @NotNull PayloadFactory<T> factory,
            @NotNull Supplier<BaseDataHandler<T>> receiv_instancer,
            @NotNull Supplier<BaseDataHandler<T>> join_instancer,
            boolean sendOnJoin,
            boolean sendBeforeEnter
    ) {
        this.DIRECTION = direction;
        this.INSTANCE = receiv_instancer;
        this.JOIN_INSTANCE = join_instancer;
        this.IDENTIFIER = new CustomPacketPayload.Type<>(identifier);

        this.sendOnJoin = sendOnJoin;
        this.sendBeforeEnter = sendBeforeEnter;

        this.PAYLOAD_FACTORY = factory;
        this.STREAM_CODEC = CustomPacketPayload.codec(
                PacketPayload::write,
                factory::create
        );

        if (direction == Direction.SERVER_TO_CLIENT)
            PayloadTypeRegistry.playS2C().register(this.IDENTIFIER, STREAM_CODEC);
        else if (direction == Direction.CLIENT_TO_SERVER)
            PayloadTypeRegistry.playC2S().register(this.IDENTIFIER, STREAM_CODEC);
    }

    public final Direction DIRECTION;
    @NotNull
    public final StreamCodec<FriendlyByteBuf, T> STREAM_CODEC;
    public final boolean sendOnJoin;
    public final boolean sendBeforeEnter;
    @NotNull
    public final PayloadFactory<T> PAYLOAD_FACTORY;
    @NotNull
    public final CustomPacketPayload.Type<T> IDENTIFIER;
    @NotNull
    public final Supplier<BaseDataHandler<T>> INSTANCE;
    @NotNull
    public final Supplier<BaseDataHandler<T>> JOIN_INSTANCE;

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

    void receiveFromServer(
            Object payload,
            ClientPlayNetworking.Context context
    ) {
        BaseDataHandler<T> h = this.INSTANCE.get();
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
            Object payload,
            ServerPlayNetworking.Context context
    ) {
        BaseDataHandler<T> h = this.INSTANCE.get();
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
