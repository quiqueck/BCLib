package org.betterx.bclib.api.v2.levelgen;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.dataexchange.DataExchangeAPI;
import org.betterx.bclib.api.v2.datafixer.DataFixerAPI;
import org.betterx.wover.core.api.ModCore;
import org.betterx.wover.events.api.WorldLifecycle;
import org.betterx.wover.events.api.client.ClientWorldLifecycle;
import org.betterx.wover.events.api.types.client.BeforeClientLoadScreen;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.storage.LevelStorageSource;

public class LevelGenEvents {
    public static void setupWorld() {
        DataExchangeAPI.prepareServerside();
    }

    public static void register() {
        WorldLifecycle.WORLD_FOLDER_READY.subscribe(LevelGenEvents::beforeWorldLoad);
        WorldLifecycle.CREATED_NEW_WORLD_FOLDER.subscribe(LevelGenEvents::afterWorldCreation, 10000);

        ClientWorldLifecycle.BEFORE_CLIENT_LOAD_SCREEN.subscribe(LevelGenEvents::patchExistingWorldOnClient, 10000);
        WorldLifecycle.WORLD_FOLDER_READY.subscribe(LevelGenEvents::patchExistingWorldOnServer, 10000);
        WorldLifecycle.WORLD_FOLDER_READY.subscribe(LevelGenEvents::initializeWorldConfig, 10100);
    }

    private static void initializeWorldConfig(LevelStorageSource.LevelStorageAccess storageAccess) {

    }

    private static void patchExistingWorldOnServer(LevelStorageSource.LevelStorageAccess storageAccess) {
        if (ModCore.isServer()) {
            DataFixerAPI.fixData(storageAccess, false, (b) -> {
            });
        }
    }

    private static void patchExistingWorldOnClient(
            LevelStorageSource.LevelStorageAccess storageAccess,
            BeforeClientLoadScreen.ContinueWith continueWith
    ) {
        if (!DataFixerAPI.fixData(storageAccess, BCLib.isClient(), (b) -> continueWith.loadingScreen())) {
            continueWith.loadingScreen();
        }
    }

    private static void afterWorldCreation(
            LevelStorageSource.LevelStorageAccess levelStorageAccess,
            RegistryAccess registryAccess,
            Holder<WorldPreset> worldPresetHolder,
            WorldDimensions worldDimensions,
            boolean b
    ) {
        beforeWorldLoad(levelStorageAccess);
        DataFixerAPI.initializePatchData();
    }

    private static void beforeWorldLoad(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
        setupWorld();
    }
}
