package org.betterx.bclib.sdf.operator;


import com.mojang.math.Axis;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class SDFRotation extends SDFUnary {
    private final Vector3f pos = new Vector3f();
    private Quaternionf rotation;

    public SDFRotation setRotation(Axis axis, float rotationAngle) {
        rotation = axis.rotation(rotationAngle);
        return this;
    }

    public SDFRotation setRotation(Vector3f axis, float rotationAngle) {
        rotation = new Quaternionf().setAngleAxis(rotationAngle, axis.x, axis.y, axis.z);
        return this;
    }

    @Override
    public float getDistance(float x, float y, float z) {
        pos.set(x, y, z);
        pos.rotate(rotation);
        return source.getDistance(pos.x(), pos.y(), pos.z());
    }
}
