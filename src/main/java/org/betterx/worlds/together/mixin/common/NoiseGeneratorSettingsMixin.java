package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.surfaceRules.SurfaceRuleProvider;

import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NoiseGeneratorSettings.class)
public abstract class NoiseGeneratorSettingsMixin implements SurfaceRuleProvider {
    @Mutable
    @Final
    @Shadow
    private SurfaceRules.RuleSource surfaceRule;

    public void bclib_overwriteSurfaceRules(SurfaceRules.RuleSource surfaceRule) {
        if (surfaceRule == null || surfaceRule == this.surfaceRule) return;
        if (this.bcl_containsOverride) {
            WorldsTogether.LOGGER.warning("Overwriting an overwritten set of Surface Rules.");
        }
        this.bcl_containsOverride = true;
        this.surfaceRule = surfaceRule;
    }

    public SurfaceRules.RuleSource bclib_getOriginalSurfaceRules() {
        return this.surfaceRule;
    }

    private boolean bcl_containsOverride = false;

}
