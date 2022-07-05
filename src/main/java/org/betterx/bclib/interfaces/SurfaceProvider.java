package org.betterx.bclib.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

public interface SurfaceProvider {
    BlockState bclib_getSurface(BlockPos pos, Holder<Biome> biome, ServerLevel level);
}
