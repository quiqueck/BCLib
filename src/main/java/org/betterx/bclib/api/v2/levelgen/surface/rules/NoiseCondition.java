package org.betterx.bclib.api.v2.levelgen.surface.rules;

import org.betterx.bclib.mixin.common.SurfaceRulesContextAccessor;

import net.minecraft.world.level.levelgen.SurfaceRules;

public interface NoiseCondition extends SurfaceRules.ConditionSource {
    boolean test(SurfaceRulesContextAccessor context);
}
