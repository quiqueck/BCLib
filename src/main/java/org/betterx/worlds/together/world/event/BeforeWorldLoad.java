package org.betterx.worlds.together.world.event;

import net.minecraft.world.level.storage.LevelStorageSource;

public interface BeforeWorldLoad {
    void prepareWorld(
            LevelStorageSource.LevelStorageAccess storageAccess,
            boolean isNewWorld,
            boolean isServer
    );
}
