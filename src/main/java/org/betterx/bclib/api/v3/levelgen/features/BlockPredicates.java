package org.betterx.bclib.api.v3.levelgen.features;

import org.betterx.bclib.api.v2.tag.CommonBlockTags;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.material.Fluids;

public class BlockPredicates {
    public static final BlockPredicate ONLY_NYLIUM = BlockPredicate.matchesTag(BlockTags.NYLIUM);
    public static final BlockPredicate ONLY_NETHER_GROUND = BlockPredicate.matchesTag(CommonBlockTags.NETHER_TERRAIN);
    public static final BlockPredicate ONLY_GROUND = BlockPredicate.matchesTag(CommonBlockTags.TERRAIN);

    public static final BlockPredicate ONLY_LAVA = BlockPredicate.matchesFluids(Fluids.LAVA);
}
