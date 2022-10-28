package org.betterx.bclib.client.models;

import net.minecraft.resources.ResourceLocation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.google.common.collect.Lists;
import org.joml.Vector3f;

import java.util.List;

@Environment(EnvType.CLIENT)
public class OBJModelBuilder {
    private static final OBJModelBuilder INSTANCE = new OBJModelBuilder();
    private final List<ResourceLocation> textures = Lists.newArrayList();
    private final Vector3f offset = new Vector3f();
    private ResourceLocation modelLocation;
    private ResourceLocation particles;
    private boolean useCulling;
    private boolean useShading;

    private OBJModelBuilder() {
    }

    /**
     * Start a new bodel building process, clears data of previous builder.
     *
     * @return {@link OBJModelBuilder} instance.
     */
    public static OBJModelBuilder start(ResourceLocation modelLocation) {
        INSTANCE.modelLocation = modelLocation;
        INSTANCE.offset.set(0, 0, 0);
        INSTANCE.useCulling = true;
        INSTANCE.useShading = true;
        INSTANCE.particles = null;
        INSTANCE.textures.clear();
        return INSTANCE;
    }

    /**
     * Add texture to the model. All textures have indexes with same order as in source OBJ model.
     *
     * @param texture {@link ResourceLocation} texture ID.
     * @return this {@link OBJModelBuilder}.
     */
    public OBJModelBuilder addTexture(ResourceLocation texture) {
        textures.add(texture);
        return this;
    }

    /**
     * Culling used to remove block faces if they are on block faces or outside of the block to reduce faces count in rendering.
     * Opaque blocks shoud have this as true to reduce geometry issues, block like plants should have this as false.
     * Default value is {@code true}.
     *
     * @param useCulling {@link Boolean}.
     * @return this {@link OBJModelBuilder}.
     */
    public OBJModelBuilder useCulling(boolean useCulling) {
        this.useCulling = useCulling;
        return this;
    }

    /**
     * Shading tints block faces in shades of gray to immitate volume in MC rendering.
     * Blocks like plants don't have shading, most full opaque blocks - have.
     * Default value is {@code true}.
     *
     * @param useShading {@link Boolean}.
     * @return this {@link OBJModelBuilder}.
     */
    public OBJModelBuilder useShading(boolean useShading) {
        this.useShading = useShading;
        return this;
    }

    /**
     * Set particle texture for this model.
     * Not required, if texture is not selected the first texture will be used instead of it.
     *
     * @param texture {@link ResourceLocation} texture ID.
     * @return this {@link OBJModelBuilder}.
     */
    public OBJModelBuilder setParticlesTexture(ResourceLocation texture) {
        this.particles = texture;
        return this;
    }

    public OBJModelBuilder setOffset(float x, float y, float z) {
        this.offset.set(x, y, z);
        return this;
    }

    /**
     * Builds model from all required data.
     *
     * @return {@link OBJBlockModel}.
     */
    public OBJBlockModel build() {
        byte particleIndex = 0;
        if (particles != null) {
            particleIndex = (byte) textures.indexOf(particles);
            if (particleIndex < 0) {
                particleIndex = (byte) textures.size();
                textures.add(particles);
            }
        }
        ResourceLocation[] sprites = textures.toArray(new ResourceLocation[textures.size()]);
        return new OBJBlockModel(modelLocation, offset, useCulling, useShading, particleIndex, sprites);
    }
}
