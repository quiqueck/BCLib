package org.betterx.bclib.mixin.common;

import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(MobSpawnSettings.class)
public interface MobSpawnSettingsAccessor {
    @Accessor("spawners")
    Map<MobCategory, WeightedList<SpawnerData>> bcl_getSpawners();

    @Accessor("spawners")
    @Mutable
    void bcl_setSpawners(Map<MobCategory, WeightedList<SpawnerData>> spawners);
}
