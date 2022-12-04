package org.betterx.bclib.api.v2.generator.map.hex;

import org.betterx.bclib.api.v2.generator.BiomePicker;
import org.betterx.bclib.interfaces.BiomeChunk;
import org.betterx.bclib.interfaces.BiomeMap;
import org.betterx.bclib.interfaces.TriConsumer;
import org.betterx.bclib.noise.OpenSimplexNoise;
import org.betterx.bclib.util.MHelper;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.WorldgenRandom;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Random;

public class HexBiomeMap implements BiomeMap {
    private static final float RAD_INNER = (float) Math.sqrt(3.0) * 0.5F;
    private static final float COEF = 0.25F * (float) Math.sqrt(3.0);
    private static final float COEF_HALF = COEF * 0.5F;
    private static final float SIN = (float) Math.sin(0.4);
    private static final float COS = (float) Math.cos(0.4);
    private static final float[] EDGE_CIRCLE_X;
    private static final float[] EDGE_CIRCLE_Z;

    private final Map<ChunkPos, HexBiomeChunk> chunks = Maps.newConcurrentMap();
    private final BiomePicker picker;

    private final OpenSimplexNoise[] noises = new OpenSimplexNoise[2];
    private TriConsumer<Integer, Integer, Integer> processor;
    private final byte noiseIterations;
    private final float scale;
    private final int seed;

    public HexBiomeMap(long seed, int size, BiomePicker picker) {
        this.picker = picker;
        this.scale = HexBiomeChunk.scaleMap(size);
        Random random = new Random(seed);

        noises[0] = new OpenSimplexNoise(random.nextInt());
        noises[1] = new OpenSimplexNoise(random.nextInt());
        noiseIterations = (byte) Math.min(Math.ceil(Math.log(scale) / Math.log(2)), 5);
        this.seed = random.nextInt();
    }

    @Override
    public void clearCache() {
        if (chunks.size() > 127) {
            chunks.clear();
        }
    }

    @Override
    public BiomePicker.ActualBiome getBiome(double x, double y, double z) {
        BiomePicker.ActualBiome biome = getRawBiome(x, z);
        BiomePicker.ActualBiome edge = biome.getEdge();
        int size = biome.bclBiome.settings.getEdgeSize();

        if (edge == null && biome.getParentBiome() != null) {
            edge = biome.getParentBiome().getEdge();
            size = biome.getParentBiome().bclBiome.settings.getEdgeSize();
        }

        if (edge == null) {
            return biome;
        }

        for (byte i = 0; i < 8; i++) {
            if (!getRawBiome(x + size * EDGE_CIRCLE_X[i], z + size * EDGE_CIRCLE_Z[i]).isSame(biome)) {
                return edge;
            }
        }

        return biome;
    }

    @Override
    public BiomeChunk getChunk(final int cx, final int cz, final boolean update) {
        final ChunkPos pos = new ChunkPos(cx, cz);
        HexBiomeChunk chunk = chunks.get(pos);
        if (chunk == null) {
            WorldgenRandom random = new WorldgenRandom(RandomSource.create(MHelper.getSeed(seed, cx, cz)));
            chunk = new HexBiomeChunk(random, picker);
            if (update && processor != null) {
                processor.accept(cx, cz, chunk.getSide());
            }
            chunks.put(pos, chunk);
        }
        return chunk;
    }

    @Override
    public void setChunkProcessor(TriConsumer<Integer, Integer, Integer> processor) {
        this.processor = processor;
    }

    private BiomePicker.ActualBiome getRawBiome(double x, double z) {
        double px = x / scale * RAD_INNER;
        double pz = z / scale;
        double dx = rotateX(px, pz);
        double dz = rotateZ(px, pz);
        px = dx;
        pz = dz;

        dx = getNoise(px, pz, (byte) 0) * 0.2F;
        dz = getNoise(pz, px, (byte) 1) * 0.2F;
        px += dx;
        pz += dz;

        int cellZ = (int) Math.floor(pz);
        boolean offset = (cellZ & 1) == 1;

        if (offset) {
            px += 0.5;
        }

        int cellX = (int) Math.floor(px);

        float pointX = (float) (px - cellX - 0.5);
        float pointZ = (float) (pz - cellZ - 0.5);

        if (Math.abs(pointZ) < 0.3333F) {
            return getChunkBiome(cellX, cellZ);
        }

        if (insideHexagon(0, 0, 1.1555F, pointZ * RAD_INNER, pointX)) {
            return getChunkBiome(cellX, cellZ);
        }

        cellX = pointX < 0 ? (offset ? cellX - 1 : cellX) : (offset ? cellX : cellX + 1);
        cellZ = pointZ < 0 ? cellZ - 1 : cellZ + 1;

        return getChunkBiome(cellX, cellZ);
    }

    private BiomePicker.ActualBiome getChunkBiome(int x, int z) {
        int cx = HexBiomeChunk.scaleCoordinate(x);
        int cz = HexBiomeChunk.scaleCoordinate(z);

        if (((z >> 2) & 1) == 0 && HexBiomeChunk.isBorder(x)) {
            x = 0;
            cx += 1;
        } else if (((x >> 2) & 1) == 0 && HexBiomeChunk.isBorder(z)) {
            z = 0;
            cz += 1;
        }

        return getChunk(cx, cz, true).getBiome(x, z);
    }

    private boolean insideHexagon(float centerX, float centerZ, float radius, float x, float z) {
        double dx = Math.abs(x - centerX) / radius;
        double dy = Math.abs(z - centerZ) / radius;
        return (dy <= COEF) && (COEF * dx + 0.25F * dy <= COEF_HALF);
    }

    private double getNoise(double x, double z, byte state) {
        double result = 0;
        for (byte i = 1; i <= noiseIterations; i++) {
            OpenSimplexNoise noise = noises[state];
            state = (byte) ((state + 1) & 1);
            result += noise.eval(x * i, z * i) / i;
        }
        return result;
    }

    private double rotateX(double x, double z) {
        return x * COS - z * SIN;
    }

    private double rotateZ(double x, double z) {
        return x * SIN + z * COS;
    }

    static {
        EDGE_CIRCLE_X = new float[8];
        EDGE_CIRCLE_Z = new float[8];

        for (byte i = 0; i < 8; i++) {
            float angle = i / 4F * (float) Math.PI;
            EDGE_CIRCLE_X[i] = (float) Math.sin(angle);
            EDGE_CIRCLE_Z[i] = (float) Math.cos(angle);
        }
    }
}
