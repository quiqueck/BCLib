package org.betterx.bclib.api.v2.spawning;

import org.betterx.bclib.interfaces.SpawnRule;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;

import org.jetbrains.annotations.NotNull;

public class SpawnRuleEntry<M extends Mob> implements Comparable<SpawnRuleEntry> {
    private final SpawnRule rule;
    private final byte priority;
    //public final String debugName;

    public SpawnRuleEntry(int priority, SpawnRule rule, String debugName) {
        this.priority = (byte) priority;
        this.rule = rule;
        //this.debugName = debugName;
    }

    protected boolean canSpawn(
            EntityType<M> type,
            LevelAccessor world,
            MobSpawnType spawnReason,
            BlockPos pos,
            RandomSource random
    ) {
        return rule.canSpawn(type, world, spawnReason, pos, random);
    }

    @Override
    public int compareTo(@NotNull SpawnRuleEntry entry) {
        return Integer.compare(priority, entry.priority);
    }
}
