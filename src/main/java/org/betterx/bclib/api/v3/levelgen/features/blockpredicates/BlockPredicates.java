package org.betterx.bclib.api.v3.levelgen.features.blockpredicates;

import org.betterx.bclib.BCLib;
import org.betterx.worlds.together.tag.v3.CommonBlockTags;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.material.Fluids;

public class BlockPredicates {
    public static final BlockPredicate ONLY_NYLIUM = BlockPredicate.matchesTag(BlockTags.NYLIUM);
    public static final BlockPredicate ONLY_MYCELIUM = BlockPredicate.matchesTag(CommonBlockTags.MYCELIUM);
    public static final BlockPredicate ONLY_GRAVEL_OR_SAND = BlockPredicate.matchesBlocks(
            Blocks.GRAVEL,
            Blocks.SAND,
            Blocks.RED_SAND
    );
    public static final BlockPredicate ONLY_SOUL_GROUND = BlockPredicate.matchesTag(CommonBlockTags.SOUL_GROUND);
    public static final BlockPredicate ONLY_NETHER_GROUND = BlockPredicate.matchesTag(CommonBlockTags.NETHER_TERRAIN);
    public static final BlockPredicate ONLY_NETHER_GROUND_AND_BASALT = BlockPredicate.anyOf(
            ONLY_NETHER_GROUND,
            BlockPredicate.matchesBlocks(Blocks.BASALT)
    );
    public static final BlockPredicate ONLY_GROUND = BlockPredicate.matchesTag(CommonBlockTags.TERRAIN);

    public static final BlockPredicate ONLY_LAVA = BlockPredicate.matchesFluids(Fluids.LAVA);
    public static final BlockPredicate ONLY_GROUND_OR_LAVA = BlockPredicate.anyOf(
            BlockPredicate.matchesTag(CommonBlockTags.TERRAIN),
            BlockPredicate.matchesFluids(Fluids.LAVA)
    );
    public static final BlockPredicateType<IsFullShape> FULL_SHAPE = register(
            BCLib.makeID("full_shape"),
            IsFullShape.CODEC
    );

    public static <P extends BlockPredicate> BlockPredicateType<P> register(ResourceLocation location, Codec<P> codec) {
        return Registry.register(BuiltInRegistries.BLOCK_PREDICATE_TYPE, location, () -> codec);
    }

    public static void ensureStaticInitialization() {

    }
}
