package org.betterx.bclib.client.models;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Environment(EnvType.CLIENT)
public class UnbakedQuad {
    private static final Vector4f POS = new Vector4f();
    private final float[] data = new float[20]; // 4 points with 3 positions and 2 uvs, 4 * (3 + 2)
    private Direction dir = Direction.UP;
    private boolean useShading = false;
    private int spriteIndex;

    public void addData(int index, float value) {
        data[index] = value;
    }

    public void setSpriteIndex(int index) {
        spriteIndex = index;
    }

    public void setDirection(Direction dir) {
        this.dir = dir;
    }

    public void setShading(boolean useShading) {
        this.useShading = useShading;
    }

    public Vector3f getPos(int index, Vector3f result) {
        int dataIndex = index * 5;
        float x = data[dataIndex++];
        float y = data[dataIndex++];
        float z = data[dataIndex];
        result.set(x, y, z);
        return result;
    }

    public BakedQuad bake(TextureAtlasSprite[] sprites, ModelState modelState) {
        Matrix4f matrix = modelState.getRotation().getMatrix();
        TextureAtlasSprite sprite = sprites[spriteIndex];
        int[] vertexData = new int[32];
        for (int i = 0; i < 4; i++) {
            int index = i << 3;
            int dataIndex = i * 5;
            float x = data[dataIndex++]; // X
            float y = data[dataIndex++]; // Y
            float z = data[dataIndex++]; // Z
            POS.set(x, y, z, 0);
            POS.mul(matrix);
            vertexData[index] = Float.floatToIntBits(POS.x());     // X
            vertexData[index | 1] = Float.floatToIntBits(POS.y()); // Y
            vertexData[index | 2] = Float.floatToIntBits(POS.z()); // Z
            vertexData[index | 3] = -1; // Unknown constant
            vertexData[index | 4] = Float.floatToIntBits(sprite.getU(data[dataIndex++])); // U
            vertexData[index | 5] = Float.floatToIntBits(sprite.getV(data[dataIndex]));   // V
        }
        // vertices, tint index, direction, sprite, shade
        return new BakedQuad(vertexData, 0, dir, sprites[spriteIndex], useShading);
    }
}
