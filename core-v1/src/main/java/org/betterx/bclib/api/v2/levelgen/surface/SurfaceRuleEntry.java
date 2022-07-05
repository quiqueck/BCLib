package org.betterx.bclib.api.v2.levelgen.surface;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.levelgen.SurfaceRules;

import org.jetbrains.annotations.NotNull;

public class SurfaceRuleEntry<M extends Mob> implements Comparable<SurfaceRuleEntry> {
    private final SurfaceRules.RuleSource rule;
    private final byte priority;

    public SurfaceRuleEntry(int priority, SurfaceRules.RuleSource rule) {
        this.priority = (byte) priority;
        this.rule = rule;
    }

    protected SurfaceRules.RuleSource getRule() {
        return rule;
    }

    @Override
    public int compareTo(@NotNull SurfaceRuleEntry entry) {
        return Integer.compare(priority, entry.priority);
    }
}
