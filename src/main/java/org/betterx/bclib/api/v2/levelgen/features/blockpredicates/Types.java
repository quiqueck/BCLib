package org.betterx.bclib.api.v2.levelgen.features.blockpredicates;

import org.betterx.bclib.api.v3.levelgen.features.blockpredicates.BlockPredicates;
import org.betterx.bclib.api.v3.levelgen.features.blockpredicates.IsFullShape;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;

/**
 * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.blockpredicates.BlockPredicates} instead
 */
@Deprecated(forRemoval = true)
public class Types {
    /**
     * @deprecated Please use {@link BlockPredicates#FULL_SHAPE} instead
     */
    @Deprecated(forRemoval = true)
    public static final BlockPredicateType<IsFullShape> FULL_SHAPE = BlockPredicates.FULL_SHAPE;

    /**
     * @param location
     * @param codec
     * @param <P>
     * @return
     * @deprecated Please use {@link BlockPredicates#register(ResourceLocation, Codec)} instead
     */
    @Deprecated(forRemoval = true)
    public static <P extends BlockPredicate> BlockPredicateType<P> register(ResourceLocation location, Codec<P> codec) {
        return BlockPredicates.register(location, codec);
    }

    public static void ensureStaticInitialization() {

    }
}
