package org.betterx.bclib.api.v2.levelgen;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.LifeCycleAPI;
import org.betterx.bclib.api.v2.dataexchange.DataExchangeAPI;
import org.betterx.bclib.api.v2.datafixer.DataFixerAPI;
import org.betterx.bclib.api.v2.poi.PoiManager;
import org.betterx.worlds.together.world.WorldConfig;
import org.betterx.worlds.together.world.event.WorldEvents;

import net.minecraft.core.Registry;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.nio.file.Path;
import java.util.function.Consumer;

public class LevelGenEvents {
    public static void setupWorld() {
        DataExchangeAPI.prepareServerside();
    }

    public static void register() {
        WorldEvents.BEFORE_WORLD_LOAD.on(LevelGenEvents::beforeWorldLoad);

        WorldEvents.ON_WORLD_LOAD.on(LevelGenEvents::onWorldLoad);
        WorldEvents.ON_FINALIZED_WORLD_LOAD.on(LevelGenEvents::finalizedWorldLoad);

        WorldEvents.PATCH_WORLD.on(LevelGenEvents::patchExistingWorld);
    }


    private static boolean patchExistingWorld(
            LevelStorageSource.LevelStorageAccess storageAccess,
            Consumer<Boolean> allDone
    ) {
        final Path dataPath = storageAccess.getLevelPath(LevelResource.ROOT).resolve("data");
        WorldConfig.setDataDir(dataPath.toFile());
        return DataFixerAPI.fixData(storageAccess, allDone != null && BCLib.isClient(), allDone);
    }


    private static void beforeWorldLoad(
            LevelStorageSource.LevelStorageAccess storageAccess,
            boolean isNewWorld,
            boolean isServer
    ) {
        setupWorld();
        if (isNewWorld) {
            WorldConfig.saveFile(BCLib.MOD_ID);
            DataFixerAPI.initializePatchData();
        }
    }

    private static void onWorldLoad() {
        LifeCycleAPI._runBeforeLevelLoad();
    }


    private static void finalizedWorldLoad(Registry<LevelStem> dimensionRegistry) {
        PoiManager.updateStates();
    }
}
