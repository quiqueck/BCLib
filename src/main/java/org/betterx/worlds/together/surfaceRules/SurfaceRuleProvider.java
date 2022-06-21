package org.betterx.worlds.together.surfaceRules;

import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.SurfaceRules;

public interface SurfaceRuleProvider {
    void bclib_addBiomeSource(BiomeSource source);
    void bclib_clearBiomeSources();
    void bclib_overwrite(SurfaceRules.RuleSource surfaceRule);
}
