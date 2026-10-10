package org.betterx.bclib.mixin.common;

import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Targets vanilla's {@link MaterialRuleContext}, which replaced {@code SurfaceRules.Context} in Minecraft 26.3.
 * The new context no longer carries the chunk or the noise chunk, so {@code getChunk()} and
 * {@code getNoiseChunk()} are gone.
 * <p>
 * {@code getBiome()} is deliberately <b>not</b> declared here. The {@code biome} field stays {@code null}
 * until {@code MaterialRuleContext#getBiome()} populates it from the biome getter, so a field accessor would
 * hand out nulls. Call the public vanilla {@code getBiome()} method instead (or use the
 * {@code SurfaceRulesContextAccessor} in {@code wover-surface-api}).
 */
@Mixin(MaterialRuleContext.class)
public interface SurfaceRulesContextAccessor {
    @Accessor("blockX")
    int getBlockX();

    @Accessor("blockY")
    int getBlockY();

    @Accessor("blockZ")
    int getBlockZ();

    @Accessor("surfaceDepth")
    int getSurfaceDepth();

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
