package org.betterx.bclib.api.v2.generator.map.square;

import org.betterx.bclib.api.v2.generator.BiomePicker;
import org.betterx.bclib.interfaces.BiomeChunk;
import org.betterx.bclib.interfaces.BiomeMap;
import org.betterx.bclib.interfaces.TriConsumer;
import org.betterx.bclib.noise.OpenSimplexNoise;
import org.betterx.bclib.util.MHelper;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;

import com.google.common.collect.Maps;

import java.util.Map;

public class SquareBiomeMap implements BiomeMap {
    private final Map<ChunkPos, SquareBiomeChunk> maps = Maps.newHashMap();
    private final OpenSimplexNoise noiseX;
    private final OpenSimplexNoise noiseZ;
    private final WorldgenRandom random;
    private final BiomePicker picker;

    private final int sizeXZ;
    private final int depth;
    private final int size;

    private TriConsumer<Integer, Integer, Integer> processor;

    public SquareBiomeMap(long seed, int size, BiomePicker picker) {
        random = new WorldgenRandom(new LegacyRandomSource(seed));
        noiseX = new OpenSimplexNoise(random.nextLong());
        noiseZ = new OpenSimplexNoise(random.nextLong());
        this.sizeXZ = size;
        depth = (int) Math.ceil(Math.log(size) / Math.log(2)) - 2;
        this.size = 1 << depth;
        this.picker = picker;
    }

    @Override
    public void clearCache() {
        if (maps.size() > 32) {
            maps.clear();
        }
    }

    @Override
    public BiomePicker.ActualBiome getBiome(double x, double y, double z) {
        BiomePicker.ActualBiome biome = getRawBiome(x, z);

        if (biome.getEdge() != null || (biome.getParentBiome() != null && biome.getParentBiome().getEdge() != null)) {
            BiomePicker.ActualBiome search = biome;
            if (biome.getParentBiome() != null) {
                search = biome.getParentBiome();
            }

            int size = search.bclBiome.settings.getEdgeSize();
            boolean edge = !search.isSame(getRawBiome(x + size, z));
            edge = edge || !search.isSame(getRawBiome(x - size, z));
            edge = edge || !search.isSame(getRawBiome(x, z + size));
            edge = edge || !search.isSame(getRawBiome(x, z - size));
            edge = edge || !search.isSame(getRawBiome(x - 1, z - 1));
            edge = edge || !search.isSame(getRawBiome(x - 1, z + 1));
            edge = edge || !search.isSame(getRawBiome(x + 1, z - 1));
            edge = edge || !search.isSame(getRawBiome(x + 1, z + 1));

            if (edge) {
                biome = search.getEdge();
            }
        }

        return biome;
    }

    @Override
    public void setChunkProcessor(TriConsumer<Integer, Integer, Integer> processor) {
        this.processor = processor;
    }

    @Override
    public BiomeChunk getChunk(int cx, int cz, boolean update) {
        ChunkPos cpos = new ChunkPos(cx, cz);
        SquareBiomeChunk chunk = maps.get(cpos);
        if (chunk == null) {
            synchronized (random) {
                random.setLargeFeatureWithSalt(0, cpos.x, cpos.z, 0);
                chunk = new SquareBiomeChunk(random, picker);
            }
            maps.put(cpos, chunk);

            if (update && processor != null) {
                processor.accept(cx, cz, chunk.getSide());
            }
        }

        return chunk;
    }

    private BiomePicker.ActualBiome getRawBiome(double bx, double bz) {
        double x = bx * size / sizeXZ;
        double z = bz * size / sizeXZ;

        double px = bx * 0.2;
        double pz = bz * 0.2;

        for (int i = 0; i < depth; i++) {
            double nx = (x + noiseX.eval(px, pz)) / 2F;
            double nz = (z + noiseZ.eval(px, pz)) / 2F;

            x = nx;
            z = nz;

            px = px / 2 + i;
            pz = pz / 2 + i;
        }

        int ix = MHelper.floor(x);
        int iz = MHelper.floor(z);

        if ((ix & SquareBiomeChunk.MASK_WIDTH) == SquareBiomeChunk.MASK_WIDTH) {
            x += (iz / 2) & 1;
        }
        if ((iz & SquareBiomeChunk.MASK_WIDTH) == SquareBiomeChunk.MASK_WIDTH) {
            z += (ix / 2) & 1;
        }

        ChunkPos cpos = new ChunkPos(
                MHelper.floor(x / SquareBiomeChunk.WIDTH),
                MHelper.floor(z / SquareBiomeChunk.WIDTH)
        );
        SquareBiomeChunk chunk = maps.get(cpos);
        if (chunk == null) {
            synchronized (random) {
                random.setLargeFeatureWithSalt(0, cpos.x, cpos.z, 0);
                chunk = new SquareBiomeChunk(random, picker);
            }
            maps.put(cpos, chunk);
        }

        return chunk.getBiome(MHelper.floor(x), MHelper.floor(z));
    }
}
