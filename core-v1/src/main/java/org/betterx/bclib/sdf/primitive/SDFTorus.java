package org.betterx.bclib.sdf.primitive;

import org.betterx.bclib.util.MHelper;

public class SDFTorus extends SDFPrimitive {
    private float radiusSmall;
    private float radiusBig;

    public SDFTorus setBigRadius(float radius) {
        this.radiusBig = radius;
        return this;
    }

    public SDFTorus setSmallRadius(float radius) {
        this.radiusSmall = radius;
        return this;
    }

    @Override
    public float getDistance(float x, float y, float z) {
        float nx = MHelper.length(x, z) - radiusBig;
        return MHelper.length(nx, y) - radiusSmall;
    }
}
