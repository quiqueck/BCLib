package org.betterx.worlds.together.surfaceRules;

import net.minecraft.world.level.levelgen.SurfaceRules;

public interface SurfaceRuleProvider {
    void bclib_overwriteSurfaceRules(SurfaceRules.RuleSource surfaceRule);
    SurfaceRules.RuleSource bclib_getOriginalSurfaceRules();
}
