package org.betterx.bclib.api.v2.generator.map;

import org.betterx.bclib.api.v2.generator.BiomePicker;
import org.betterx.bclib.api.v2.generator.config.MapBuilderFunction;
import org.betterx.bclib.interfaces.BiomeChunk;
import org.betterx.bclib.interfaces.BiomeMap;
import org.betterx.bclib.interfaces.TriConsumer;
import org.betterx.bclib.noise.OpenSimplexNoise;

import net.minecraft.util.Mth;

import java.util.Random;

public class MapStack implements BiomeMap {
    private final OpenSimplexNoise noise;
    private final BiomeMap[] maps;
    private final double layerDistortion;
    private final int worldHeight;
    private final int minValue;
    private final int maxValue;
    private final int maxIndex;

    public MapStack(
            long seed,
            int size,
            BiomePicker picker,
            int mapHeight,
            int worldHeight,
            MapBuilderFunction mapConstructor
    ) {
        final int mapCount = Mth.ceil((float) worldHeight / mapHeight);
        this.maxIndex = mapCount - 1;
        this.worldHeight = worldHeight;
        this.layerDistortion = mapHeight * 0.1;
        minValue = Mth.floor(mapHeight * 0.5F + 0.5F);
        maxValue = Mth.floor(worldHeight - mapHeight * 0.5F + 0.5F);
        maps = new BiomeMap[mapCount];
        Random random = new Random(seed);
        for (int i = 0; i < mapCount; i++) {
            maps[i] = mapConstructor.create(random.nextLong(), size, picker);
            maps[i].setChunkProcessor(this::onChunkCreation);
        }
        noise = new OpenSimplexNoise(random.nextInt());
    }

    @Override
    public void clearCache() {
        for (BiomeMap map : maps) {
            map.clearCache();
        }
    }

    @Override
    public void setChunkProcessor(TriConsumer<Integer, Integer, Integer> processor) {
    }

    @Override
    public BiomeChunk getChunk(int cx, int cz, boolean update) {
        return null;
    }

    @Override
    public BiomePicker.ActualBiome getBiome(double x, double y, double z) {
        int mapIndex;

        if (y < minValue) {
            mapIndex = 0;
        } else if (y > maxValue) {
            mapIndex = maxIndex;
        } else {
            mapIndex = Mth.floor((y + noise.eval(
                    x * 0.03,
                    z * 0.03
            ) * layerDistortion) / worldHeight * maxIndex + 0.5F);
            mapIndex = Mth.clamp(mapIndex, 0, maxIndex);
        }

        return maps[mapIndex].getBiome(x, y, z);
    }

    private void onChunkCreation(int cx, int cz, int side) {
        BiomePicker.ActualBiome[][] biomeMap = new BiomePicker.ActualBiome[side][side];
        BiomeChunk[] chunks = new BiomeChunk[maps.length];

        boolean isNoEmpty = false;
        for (int i = 0; i < maps.length; i++) {
            chunks[i] = maps[i].getChunk(cx, cz, false);
            for (int x = 0; x < side; x++) {
                for (int z = 0; z < side; z++) {
                    if (biomeMap[x][z] == null) {
                        BiomePicker.ActualBiome biome = chunks[i].getBiome(x, z);
                        if (biome == null) {
                            biome = chunks[i].getBiome(x, z);
                        }
                        if (biome.bclBiome.settings.isVertical()) {
                            biomeMap[x][z] = biome;
                            isNoEmpty = true;
                        }
                    }
                }
            }
        }

        if (isNoEmpty) {
            for (int i = 0; i < maps.length; i++) {
                for (int x = 0; x < side; x++) {
                    for (int z = 0; z < side; z++) {
                        if (biomeMap[x][z] != null) {
                            chunks[i].setBiome(x, z, biomeMap[x][z]);
                        }
                    }
                }
            }
        }
    }
}
