package org.betterx.bclib.api.v2.levelgen.features.placement;

import net.minecraft.core.Vec3i;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;

import java.util.Optional;

/**
 * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.placement.Is} instead.
 */
@Deprecated(forRemoval = true)
public class Is extends org.betterx.bclib.api.v3.levelgen.features.placement.Is {

    public Is(BlockPredicate predicate, Optional<Vec3i> offset) {
        super(predicate, offset);
    }
}
