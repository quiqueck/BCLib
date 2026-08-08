package org.betterx.bclib.mixin.common;

import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * 26.3 renamed {@code MobSpawnSettings.spawners} to {@code spawnsByCategory} (same type,
 * {@code Map<MobCategory, WeightedList<SpawnerData>>}, still {@code private final}). The accessor <em>method</em>
 * names are deliberately left alone so downstream mods keep compiling.
 */
@Mixin(MobSpawnSettings.class)
public interface MobSpawnSettingsAccessor {
    @Accessor("spawnsByCategory")
    Map<MobCategory, WeightedList<SpawnerData>> bcl_getSpawners();

    @Accessor("spawnsByCategory")
    @Mutable
    void bcl_setSpawners(Map<MobCategory, WeightedList<SpawnerData>> spawners);
}
