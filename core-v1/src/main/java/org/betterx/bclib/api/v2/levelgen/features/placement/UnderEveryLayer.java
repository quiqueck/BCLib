package org.betterx.bclib.api.v2.levelgen.features.placement;

import java.util.Optional;

/**
 * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.placement.UnderEveryLayer} instead
 */
@Deprecated(forRemoval = true)
public class UnderEveryLayer
        extends org.betterx.bclib.api.v3.levelgen.features.placement.UnderEveryLayer {

    protected UnderEveryLayer(Optional<Integer> minHeight, Optional<Integer> maxHeight) {
        super(minHeight, maxHeight);
    }
}
