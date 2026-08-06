package org.betterx.bclib.mixin.common;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * {@code getBiome()} is deliberately <b>not</b> declared here any more. Up to 26.1 the {@code biome} field
 * held a {@code Supplier<Holder<Biome>>} that did the lazy lookup itself, so a field accessor was enough.
 * In 26.2 the field is a plain {@code Holder<Biome>} that stays {@code null} until
 * {@code SurfaceRules.Context#getBiome()} populates it from {@code biomeGetter} - the old accessor would not
 * even have applied (Mixin rejects an {@code @Accessor} whose type does not match the field), and a
 * retyped one would hand out nulls. Use the {@code SurfaceRulesContextAccessor} in {@code wover-surface-api},
 * which widens the real {@code getBiome()} method through its access widener.
 */
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
