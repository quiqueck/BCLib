package org.betterx.worlds.together.surfaceRules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.SurfaceRules;

public class AssignedSurfaceRule {
    public static final Codec<AssignedSurfaceRule> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    SurfaceRules.RuleSource.CODEC.fieldOf("ruleSource").forGetter(o -> o.ruleSource),
                    ResourceLocation.CODEC.fieldOf("biome").forGetter(o -> o.biomeID)
            )
            .apply(instance, AssignedSurfaceRule::new)
    );

    public final SurfaceRules.RuleSource ruleSource;
    public final ResourceLocation biomeID;

    AssignedSurfaceRule(SurfaceRules.RuleSource ruleSource, ResourceLocation biomeID) {
        this.ruleSource = ruleSource;
        this.biomeID = biomeID;
    }
}
