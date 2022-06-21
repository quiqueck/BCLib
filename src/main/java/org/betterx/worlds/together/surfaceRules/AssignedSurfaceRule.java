package org.betterx.worlds.together.surfaceRules;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.SurfaceRules;

public class AssignedSurfaceRule {
    public final SurfaceRules.RuleSource ruleSource;
    public final ResourceLocation biomeID;

    AssignedSurfaceRule(SurfaceRules.RuleSource ruleSource, ResourceLocation biomeID) {
        this.ruleSource = ruleSource;
        this.biomeID = biomeID;
    }

}
