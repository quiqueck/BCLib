package org.betterx.bclib.api.v2.levelgen.features.blockpredicates;

import org.betterx.bclib.BCLib;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;

public class Types {
    public static final BlockPredicateType<IsFullShape> FULL_SHAPE = register(
            BCLib.makeID("full_shape"),
            IsFullShape.CODEC
    );

    public static <P extends BlockPredicate> BlockPredicateType<P> register(ResourceLocation location, Codec<P> codec) {
        return Registry.register(Registry.BLOCK_PREDICATE_TYPES, location, () -> codec);
    }

    public static void ensureStaticInitialization() {

    }
}
