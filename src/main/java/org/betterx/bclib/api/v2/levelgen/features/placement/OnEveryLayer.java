package org.betterx.bclib.api.v2.levelgen.features.placement;

import java.util.Optional;

/**
 * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.placement.OnEveryLayer} instead
 */
@Deprecated(forRemoval = true)
public class OnEveryLayer extends org.betterx.bclib.api.v3.levelgen.features.placement.OnEveryLayer {

    protected OnEveryLayer(Optional<Integer> minHeight, Optional<Integer> maxHeight) {
        super(minHeight, maxHeight);
    }
}
