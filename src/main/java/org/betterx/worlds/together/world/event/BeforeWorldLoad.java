package org.betterx.worlds.together.world.event;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.util.Map;

public interface BeforeWorldLoad {
    void prepareWorld(
            LevelStorageSource.LevelStorageAccess storageAccess,
            Map<ResourceKey<LevelStem>, ChunkGenerator> settings,
            boolean isNewWorld,
            boolean isServer
    );
}
