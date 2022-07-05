package org.betterx.bclib.api.v2.levelgen.features.placement;

import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;

import java.util.Optional;


/**
 * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.placement.IsBasin} instead.
 */
@Deprecated(forRemoval = true)
public class IsBasin extends org.betterx.bclib.api.v3.levelgen.features.placement.IsBasin {

    public IsBasin(BlockPredicate predicate) {
        super(predicate);
    }

    public IsBasin(BlockPredicate predicate, Optional<BlockPredicate> topPredicate) {
        super(predicate, topPredicate);
    }
}
