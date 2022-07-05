package org.betterx.bclib.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;

public interface BiomeSetter {
    void bclib_setBiome(Biome biome, BlockPos pos);
}
