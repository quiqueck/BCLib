package org.betterx.bclib.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;

@FunctionalInterface
public interface SpawnRule<M extends Mob> {
    boolean canSpawn(
            EntityType<M> type,
            LevelAccessor world,
            MobSpawnType spawnReason,
            BlockPos pos,
            RandomSource random
    );
}
