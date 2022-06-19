package org.betterx.bclib.presets.worldgen;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.LifeCycleAPI;
import org.betterx.bclib.api.v2.WorldDataAPI;
import org.betterx.bclib.api.v2.dataexchange.DataExchangeAPI;
import org.betterx.bclib.api.v2.datafixer.DataFixerAPI;
import org.betterx.bclib.api.v2.levelgen.LevelGenUtil;
import org.betterx.bclib.api.v2.levelgen.biomes.InternalBiomeAPI;
import org.betterx.bclib.interfaces.WorldGenSettingsComponentAccessor;
import org.betterx.bclib.mixin.common.RegistryOpsAccessor;

import net.minecraft.client.gui.screens.worldselection.WorldGenSettingsComponent;
import net.minecraft.core.Holder;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.io.File;
import java.util.Optional;

public class WorldBootstrap {
    private static class Helpers {

        private static void initializeWorldDataAPI(
                LevelStorageSource.LevelStorageAccess levelStorageAccess,
                boolean newWorld
        ) {
            File levelPath = levelStorageAccess.getLevelPath(LevelResource.ROOT).toFile();
            initializeWorldDataAPI(levelPath, newWorld);
        }

        private static void setupWorld() {
            InternalBiomeAPI.prepareNewLevel();
            DataExchangeAPI.prepareServerside();
        }

        private static void initializeWorldDataAPI(File levelBaseDir, boolean newWorld) {
            WorldDataAPI.load(new File(levelBaseDir, "data"));

            if (newWorld) {
                WorldDataAPI.saveFile(BCLib.MOD_ID);
            }
        }
    }

    public static class DedicatedServer {
        public static void registryReady(RegistryOps<Tag> regOps) {
            InternalBiomeAPI.initRegistry(regOps);
        }

        public static void setupWorld(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
            Helpers.setupWorld();

            File levelDat = levelStorageAccess.getLevelPath(LevelResource.LEVEL_DATA_FILE).toFile();
            if (!levelDat.exists()) {
                BCLib.LOGGER.info("Creating a new World, no fixes needed");
                Helpers.initializeWorldDataAPI(levelStorageAccess, true);
                BCLWorldPreset.writeWorldPresetSettings(Optional.empty());
                DataFixerAPI.initializePatchData();
            } else {
                Helpers.initializeWorldDataAPI(levelStorageAccess, false);
                DataFixerAPI.fixData(levelStorageAccess, false, (didFix) -> {/* not called when showUI==false */});
            }


            LifeCycleAPI._runBeforeLevelLoad();
        }
    }

    public static class InGUI {
        public static void registryReady(WorldGenSettingsComponent worldGenSettingsComponent) {
            InternalBiomeAPI.initRegistry(worldGenSettingsComponent.registryHolder());
        }

        public static void registryReady(Optional<RegistryOps<Tag>> registryOps) {
            if (registryOps.orElse(null) instanceof RegistryOpsAccessor acc) {
                InternalBiomeAPI.initRegistry(acc.bcl_getRegistryAccess());
            }
        }

        public static void setupNewWorld(
                Optional<LevelStorageSource.LevelStorageAccess> levelStorageAccess,
                WorldGenSettingsComponent worldGenSettingsComponent
        ) {
            if (levelStorageAccess.isPresent()) {
                Helpers.setupWorld();

                Helpers.initializeWorldDataAPI(levelStorageAccess.get(), true);

                if (worldGenSettingsComponent instanceof WorldGenSettingsComponentAccessor acc) {
                    BCLWorldPreset.writeWorldPresetSettings(acc.bcl_getPreset());
                }

                DataFixerAPI.initializePatchData();
//                DataFixerAPI.createWorldData(
//                        levelStorageAccess.get(),
//                        worldGenSettingsComponent.settings().worldGenSettings()
//                );

                LifeCycleAPI._runBeforeLevelLoad();
            }
        }

        /**
         * Does not call {@link LifeCycleAPI#_runBeforeLevelLoad()}
         */
        public static void setupLoadedWorld(
                String levelID,
                LevelStorageSource levelSource
        ) {
            Helpers.setupWorld();
            try {
                var levelStorageAccess = levelSource.createAccess(levelID);
                Helpers.initializeWorldDataAPI(levelStorageAccess, true);
                levelStorageAccess.close();
            } catch (Exception e) {
                BCLib.LOGGER.error("Failed to initialize data in world", e);
            }
        }
    }

    public static class InFreshLevel {
        public static void setupNewWorld(
                String levelID,
                WorldGenSettings worldGenSettings,
                LevelStorageSource levelSource,
                Optional<Holder<WorldPreset>> worldPreset
        ) {
            InGUI.setupLoadedWorld(levelID, levelSource);

            BCLWorldPreset.writeWorldPresetSettings(worldPreset);
            DataFixerAPI.initializePatchData();
            LifeCycleAPI._runBeforeLevelLoad();
        }
    }

    public static WorldGenSettings enforceInNewWorld(WorldGenSettings worldGenSettings) {
        worldGenSettings = LevelGenUtil
                .getWorldSettings()
                .repairSettingsOnLoad(InternalBiomeAPI.worldRegistryAccess(), worldGenSettings);
        return worldGenSettings;
    }

    public static WorldGenSettings enforceInLoadedWorld(
            Optional<RegistryOps<Tag>> registryOps,
            WorldGenSettings worldGenSettings
    ) {
        if (registryOps.orElse(null) instanceof RegistryOpsAccessor acc) {
            return LevelGenUtil
                    .getWorldSettings()
                    .repairSettingsOnLoad(acc.bcl_getRegistryAccess(), worldGenSettings);
            //.repairSettingsOnLoad(InternalBiomeAPI.worldRegistryAccess(), worldGenSettings);
        } else {
            BCLib.LOGGER.error("Unable to obtain registryAccess when enforcing generators.");
        }
        return worldGenSettings;
    }
}
