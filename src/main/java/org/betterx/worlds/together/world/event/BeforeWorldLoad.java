package org.betterx.worlds.together.world.event;

import org.betterx.worlds.together.worldPreset.settings.WorldPresetSettings;

import net.minecraft.world.level.storage.LevelStorageSource;

public interface BeforeWorldLoad {
    void prepareWorld(
            LevelStorageSource.LevelStorageAccess storageAccess,
            WorldPresetSettings settings,
            boolean isNewWorld
    );
}
