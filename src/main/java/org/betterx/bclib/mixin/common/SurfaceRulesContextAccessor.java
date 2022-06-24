package org.betterx.bclib.mixin.common;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Supplier;

@Mixin(SurfaceRules.Context.class)
public interface SurfaceRulesContextAccessor {
    @Accessor("blockX")
    int getBlockX();

    @Accessor("blockY")
    int getBlockY();

    @Accessor("blockZ")
    int getBlockZ();

    @Accessor("surfaceDepth")
    int getSurfaceDepth();

    @Accessor("biome")
    Supplier<Holder<Biome>> getBiome();

    @Accessor("chunk")
    ChunkAccess getChunk();

    @Accessor("noiseChunk")
    NoiseChunk getNoiseChunk();

    @Accessor("stoneDepthAbove")
    int getStoneDepthAbove();

    @Accessor("stoneDepthBelow")
    int getStoneDepthBelow();

    @Accessor("lastUpdateY")
    long getLastUpdateY();

    @Accessor("lastUpdateXZ")
    long getLastUpdateXZ();

    @Accessor("randomState")
    RandomState getRandomState();
}
