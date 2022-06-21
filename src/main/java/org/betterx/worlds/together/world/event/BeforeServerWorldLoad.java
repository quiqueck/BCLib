package org.betterx.worlds.together.world.event;

import org.betterx.worlds.together.worldPreset.settings.WorldPresetSettings;

import net.minecraft.world.level.storage.LevelStorageSource;

@FunctionalInterface
public interface BeforeServerWorldLoad {
    void prepareWorld(
            LevelStorageSource.LevelStorageAccess storageAccess,
            WorldPresetSettings settings,
            boolean isNewWorld
    );
}
