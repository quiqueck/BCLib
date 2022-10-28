package org.betterx.bclib.client.models;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.util.BlocksHelper;
import org.betterx.bclib.util.MHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class OBJBlockModel implements UnbakedModel, BakedModel {
    private static final Vector3f[] POSITIONS = new Vector3f[]{new Vector3f(), new Vector3f(), new Vector3f()};

    protected final Map<Direction, List<UnbakedQuad>> quadsUnbakedMap = Maps.newEnumMap(Direction.class);
    protected final Map<Direction, List<BakedQuad>> quadsBakedMap = Maps.newEnumMap(Direction.class);
    protected final List<UnbakedQuad> quadsUnbaked = Lists.newArrayList();
    protected final List<BakedQuad> quadsBaked = Lists.newArrayList();

    protected TextureAtlasSprite[] sprites;
    protected ItemTransforms transforms;
    protected ItemOverrides overrides;

    protected List<Material> materials;
    protected boolean useCulling;
    protected boolean useShading;
    protected byte particleIndex;

    public OBJBlockModel(
            ResourceLocation location,
            Vector3f offset,
            boolean useCulling,
            boolean useShading,
            byte particleIndex,
            ResourceLocation... textureIDs
    ) {
        for (Direction dir : BlocksHelper.DIRECTIONS) {
            quadsUnbakedMap.put(dir, Lists.newArrayList());
            quadsBakedMap.put(dir, Lists.newArrayList());
        }

        transforms = ItemTransforms.NO_TRANSFORMS;
        overrides = ItemOverrides.EMPTY;
        materials = new ArrayList<>(textureIDs.length);
        sprites = new TextureAtlasSprite[textureIDs.length];
        this.particleIndex = particleIndex;
        this.useCulling = useCulling;
        this.useShading = useShading;
        loadModel(location, offset, (byte) (textureIDs.length - 1));

        for (int i = 0; i < textureIDs.length; i++) {
            materials.add(new Material(TextureAtlas.LOCATION_BLOCKS, textureIDs[i]));
        }
    }

    // UnbakedModel //

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> function) {
    }

    @Nullable
    @Override
    public BakedModel bake(
            ModelBaker modelBakery,
            Function<Material, TextureAtlasSprite> textureGetter,
            ModelState modelState,
            ResourceLocation resourceLocation
    ) {
        for (int i = 0; i < sprites.length; i++) {
            sprites[i] = textureGetter.apply(materials.get(i));
        }
        quadsBaked.clear();
        quadsUnbaked.forEach(quad -> quadsBaked.add(quad.bake(sprites, modelState)));
        for (Direction dir : BlocksHelper.DIRECTIONS) {
            List<UnbakedQuad> unbaked = quadsUnbakedMap.get(dir);
            List<BakedQuad> baked = quadsBakedMap.get(dir);
            baked.clear();
            unbaked.forEach(quad -> baked.add(quad.bake(sprites, modelState)));
        }
        return this;
    }

    // Baked Model //

    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState blockState,
            @Nullable Direction direction,
            RandomSource random
    ) {
        return direction == null ? quadsBaked : quadsBakedMap.get(direction);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return sprites[particleIndex];
    }

    @Override
    public ItemTransforms getTransforms() {
        return transforms;
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }

    private Resource getResource(ResourceLocation location) {
        return Minecraft.getInstance().getResourceManager().getResource(location).orElse(null);
    }

    private void loadModel(ResourceLocation location, Vector3f offset, byte maxIndex) {
        Resource resource = getResource(location);
        if (resource == null) {
            return;
        }
        InputStream input = null;
        try {
            input = resource.open();
        } catch (IOException e) {
            BCLib.LOGGER.error("Unable to load Model", e);
            throw new RuntimeException(e);
        }

        List<Float> vertecies = new ArrayList<>(12);
        List<Float> uvs = new ArrayList<>(8);

        List<Integer> vertexIndex = new ArrayList<>(4);
        List<Integer> uvIndex = new ArrayList<>(4);

        byte materialIndex = -1;

        try {
            InputStreamReader streamReader = new InputStreamReader(input);
            BufferedReader reader = new BufferedReader(streamReader);
            String string;

            while ((string = reader.readLine()) != null) {
                if (string.startsWith("usemtl")) {
                    materialIndex++;
                    if (materialIndex > maxIndex) {
                        materialIndex = maxIndex;
                    }
                } else if (string.startsWith("vt")) {
                    String[] uv = string.split(" ");
                    uvs.add(Float.parseFloat(uv[1]));
                    uvs.add(Float.parseFloat(uv[2]));
                } else if (string.startsWith("v")) {
                    String[] vert = string.split(" ");
                    for (int i = 1; i < 4; i++) {
                        vertecies.add(Float.parseFloat(vert[i]));
                    }
                } else if (string.startsWith("f")) {
                    String[] members = string.split(" ");
                    if (members.length != 5) {
                        System.out.println("Only quads in OBJ are supported! Model [" + location + "] has n-gons or triangles!");
                        continue;
                    }
                    vertexIndex.clear();
                    uvIndex.clear();

                    for (int i = 1; i < members.length; i++) {
                        String member = members[i];

                        if (member.contains("/")) {
                            String[] sub = member.split("/");
                            vertexIndex.add(Integer.parseInt(sub[0]) - 1); // Vertex
                            uvIndex.add(Integer.parseInt(sub[1]) - 1);     // UV
                        } else {
                            vertexIndex.add(Integer.parseInt(member) - 1); // Vertex
                        }
                    }

                    boolean hasUV = !uvIndex.isEmpty();
                    UnbakedQuad quad = new UnbakedQuad();
                    for (int i = 0; i < 4; i++) {
                        int index = vertexIndex.get(i) * 3;
                        int quadIndex = i * 5;
                        quad.addData(quadIndex++, vertecies.get(index++) + offset.x()); // X
                        quad.addData(quadIndex++, vertecies.get(index++) + offset.y()); // Y
                        quad.addData(quadIndex++, vertecies.get(index) + offset.z());   // Z
                        if (hasUV) {
                            index = uvIndex.get(i) * 2;
                            quad.addData(quadIndex++, uvs.get(index++) * 16F);   // U
                            quad.addData(quadIndex, (1 - uvs.get(index)) * 16F); // V
                        }
                    }
                    quad.setSpriteIndex(materialIndex);
                    if (useShading) {
                        Direction dir = getNormalDirection(quad);
                        quad.setDirection(dir);
                        quad.setShading(true);
                    }
                    if (useCulling) {
                        Direction dir = getCullingDirection(quad);
                        if (dir == null) {
                            quadsUnbaked.add(quad);
                        } else {
                            quadsUnbakedMap.get(dir).add(quad);
                        }
                    } else {
                        quadsUnbaked.add(quad);
                    }
                }
            }

            reader.close();
            streamReader.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (materialIndex < 0) {
            quadsUnbaked.forEach(quad -> quad.setSpriteIndex(0));
            quadsUnbakedMap.values().forEach(list -> list.forEach(quad -> quad.setSpriteIndex(0)));
        }
    }

    private Direction getNormalDirection(UnbakedQuad quad) {
        Vector3f pos = quad.getPos(0, POSITIONS[0]);
        Vector3f dirA = quad.getPos(1, POSITIONS[1]);
        Vector3f dirB = quad.getPos(2, POSITIONS[2]);
        dirA.sub(pos);
        dirB.sub(pos);
        pos = MHelper.cross(dirA, dirB);
        return Direction.getNearest(pos.x(), pos.y(), pos.z());
    }

    @Nullable
    private Direction getCullingDirection(UnbakedQuad quad) {
        Direction dir = null;
        for (int i = 0; i < 4; i++) {
            Vector3f pos = quad.getPos(i, POSITIONS[0]);
            if (pos.x() < 1 && pos.x() > 0 && pos.y() < 1 && pos.y() > 0 && pos.z() < 1 && pos.z() > 0) {
                return null;
            }
            Direction newDir = Direction.getNearest(pos.x() - 0.5F, pos.y() - 0.5F, pos.z() - 0.5F);
            if (dir == null) {
                dir = newDir;
            } else if (newDir != dir) {
                return null;
            }
        }
        return dir;
    }
}
