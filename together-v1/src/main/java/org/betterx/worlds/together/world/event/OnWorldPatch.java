package org.betterx.worlds.together.world.event;

import net.minecraft.world.level.storage.LevelStorageSource;

import java.util.function.Consumer;

@FunctionalInterface
public interface OnWorldPatch {
    boolean next(
            LevelStorageSource.LevelStorageAccess storageAccess,
            Consumer<Boolean> allDone
    );
}
