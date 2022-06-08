package org.betterx.bclib.api.v2.dataexchange;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public class DataHandlerDescriptor {
    public DataHandlerDescriptor(@NotNull ResourceLocation identifier, @NotNull Supplier<BaseDataHandler> instancer) {
        this(identifier, instancer, instancer, false, false);
    }

    public DataHandlerDescriptor(
            @NotNull ResourceLocation identifier,
            @NotNull Supplier<BaseDataHandler> instancer,
            boolean sendOnJoin,
            boolean sendBeforeEnter
    ) {
        this(identifier, instancer, instancer, sendOnJoin, sendBeforeEnter);
    }

    public DataHandlerDescriptor(
            @NotNull ResourceLocation identifier,
            @NotNull Supplier<BaseDataHandler> receiv_instancer,
            @NotNull Supplier<BaseDataHandler> join_instancer,
            boolean sendOnJoin,
            boolean sendBeforeEnter
    ) {
        this.INSTANCE = receiv_instancer;
        this.JOIN_INSTANCE = join_instancer;
        this.IDENTIFIER = identifier;

        this.sendOnJoin = sendOnJoin;
        this.sendBeforeEnter = sendBeforeEnter;
    }

    public final boolean sendOnJoin;
    public final boolean sendBeforeEnter;
    @NotNull
    public final ResourceLocation IDENTIFIER;
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
