package org.betterx.bclib.interfaces;

import org.betterx.bclib.api.v2.generator.BiomePicker;

public interface BiomeMap {
    void setChunkProcessor(TriConsumer<Integer, Integer, Integer> processor);
    BiomeChunk getChunk(int cx, int cz, boolean update);
    BiomePicker.ActualBiome getBiome(double x, double y, double z);
    void clearCache();
}
