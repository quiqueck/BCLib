package org.betterx.bclib.api.v2.levelgen.features.placement;

import net.minecraft.core.Vec3i;

/**
 * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.placement.Offset} instead.
 */
@Deprecated(forRemoval = true)
public class Offset extends org.betterx.bclib.api.v3.levelgen.features.placement.Offset {

    public Offset(Vec3i offset) {
        super(offset);
    }
}
