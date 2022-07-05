package org.betterx.bclib.api.v2.levelgen.features.placement;

import net.minecraft.core.Direction;
import net.minecraft.util.valueproviders.IntProvider;

/**
 * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.placement.Extend} instead.
 */
@Deprecated(forRemoval = true)
public class Extend extends org.betterx.bclib.api.v3.levelgen.features.placement.Extend {
    public Extend(Direction direction, IntProvider length) {
        super(direction, length);
    }
}