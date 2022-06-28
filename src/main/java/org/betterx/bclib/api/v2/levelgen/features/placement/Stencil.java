package org.betterx.bclib.api.v2.levelgen.features.placement;

import java.util.List;


/**
 * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.placement.Stencil} instead
 */
@Deprecated(forRemoval = true)
public class Stencil extends org.betterx.bclib.api.v3.levelgen.features.placement.Stencil {

    public Stencil(Boolean[] stencil, int selectOneIn) {
        super(stencil, selectOneIn);
    }

    public Stencil(List<Boolean> stencil, int selectOneIn) {
        super(stencil, selectOneIn);
    }
}
