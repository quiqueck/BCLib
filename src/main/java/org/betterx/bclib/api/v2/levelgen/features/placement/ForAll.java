package org.betterx.bclib.api.v2.levelgen.features.placement;

import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.List;

/**
 * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.placement.ForAll} instead.
 */
@Deprecated(forRemoval = true)
public class ForAll extends org.betterx.bclib.api.v3.levelgen.features.placement.ForAll {

    public ForAll(List<PlacementModifier> modifiers) {
        super(modifiers);
    }
}
