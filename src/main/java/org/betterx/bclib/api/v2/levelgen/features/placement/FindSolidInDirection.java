package org.betterx.bclib.api.v2.levelgen.features.placement;

import net.minecraft.core.Direction;

import java.util.List;

/**
 * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.placement.FindSolidInDirection} instead.
 */
@Deprecated(forRemoval = true)
public class FindSolidInDirection extends org.betterx.bclib.api.v3.levelgen.features.placement.FindSolidInDirection {


    public FindSolidInDirection(Direction direction, int maxSearchDistance) {
        super(direction, maxSearchDistance);
    }

    public FindSolidInDirection(List<Direction> direction, int maxSearchDistance) {
        super(direction, maxSearchDistance);
    }

    public FindSolidInDirection(List<Direction> direction, int maxSearchDistance, boolean randomSelect) {
        super(direction, maxSearchDistance, randomSelect);
    }
}
