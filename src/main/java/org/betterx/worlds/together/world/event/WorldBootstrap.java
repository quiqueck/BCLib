package org.betterx.worlds.together.world.event;

import org.betterx.bclib.BCLib;
import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.mixin.common.RegistryOpsAccessor;
import org.betterx.worlds.together.world.WorldConfig;
import org.betterx.worlds.together.world.WorldGenUtil;
import org.betterx.worlds.together.worldPreset.TogetherWorldPreset;
import org.betterx.worlds.together.worldPreset.WorldGenSettingsComponentAccessor;
import org.betterx.worlds.together.worldPreset.WorldPresets;
import org.betterx.worlds.together.worldPreset.settings.VanillaWorldPresetSettings;
import org.betterx.worlds.together.worldPreset.settings.WorldPresetSettings;

import net.minecraft.client.gui.screens.worldselection.WorldGenSettingsComponent;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class WorldBootstrap {
    private static RegistryAccess LAST_REGISTRY_ACCESS = null;

    public static RegistryAccess getLastRegistryAccess() {
        return LAST_REGISTRY_ACCESS;
    }

    public static class Helpers {
        private static void initializeWorldDataAPI(
                LevelStorageSource.LevelStorageAccess levelStorageAccess,
                boolean newWorld
        ) {
            File levelPath = levelStorageAccess.getLevelPath(LevelResource.ROOT).toFile();
            initializeWorldDataAPI(levelPath, newWorld);
        }

        private static void initializeWorldDataAPI(File levelBaseDir, boolean newWorld) {
            WorldConfig.load(new File(levelBaseDir, "data"));

            if (newWorld) {
                WorldConfig.saveFile(BCLib.MOD_ID);
            }
        }

        private static void onRegistryReady(RegistryAccess a) {
            if (a != LAST_REGISTRY_ACCESS) {
                LAST_REGISTRY_ACCESS = a;
                WorldEventsImpl.WORLD_REGISTRY_READY.emit(e -> e.initRegistry(a));
            }
        }

        private static Holder<WorldPreset> defaultServerPreset() {
            return WorldPresets.get(
                    LAST_REGISTRY_ACCESS,
                    WorldPresets.DEFAULT.orElseThrow()
            );
        }

        private static WorldPresetSettings defaultServerSettings() {
            final Holder<WorldPreset> defaultPreset = defaultServerPreset();
            return defaultServerSettings(defaultPreset);
        }

        private static WorldPresetSettings defaultServerSettings(Holder<WorldPreset> defaultPreset) {
            final WorldPresetSettings settings;
            if (defaultPreset.value() instanceof TogetherWorldPreset t) {
                settings = t.settings;
            } else {
                settings = VanillaWorldPresetSettings.DEFAULT;
            }
            return settings;
        }

        private static Optional<Holder<WorldPreset>> presetFromDatapack(Optional<Holder<WorldPreset>> currentPreset) {
            if (currentPreset.isPresent() && LAST_REGISTRY_ACCESS != null) {
                Optional<Holder<WorldPreset>> newPreset = LAST_REGISTRY_ACCESS
                        .registryOrThrow(Registry.WORLD_PRESET_REGISTRY)
                        .getHolder(currentPreset.map(h -> h.unwrapKey()).map(h -> h.orElseThrow()).orElseThrow());
                if (newPreset.isPresent()) currentPreset = newPreset;
            }
            return currentPreset;
        }
    }

    public static class DedicatedServer {
        public static void registryReady(RegistryOps<Tag> regOps) {
            if (regOps instanceof RegistryOpsAccessor acc) {
                Helpers.onRegistryReady(acc.bcl_getRegistryAccess());
            }
        }

        public static void setupWorld(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
            File levelDat = levelStorageAccess.getLevelPath(LevelResource.LEVEL_DATA_FILE).toFile();
            if (!levelDat.exists()) {
                BCLib.LOGGER.info("Creating a new World, no fixes needed");
                final WorldPresetSettings settings = Helpers.defaultServerSettings();

                Helpers.initializeWorldDataAPI(levelStorageAccess, true);
                WorldEventsImpl.BEFORE_SERVER_WORLD_LOAD.emit(e -> e.prepareWorld(
                        levelStorageAccess, settings, true
                ));
            } else {
                Helpers.initializeWorldDataAPI(levelStorageAccess, false);
                WorldEventsImpl.BEFORE_SERVER_WORLD_LOAD.emit(e -> e.prepareWorld(
                        levelStorageAccess,
                        WorldGenUtil.getWorldSettings(),
                        false
                ));
                WorldEventsImpl.ON_WORLD_LOAD.emit(OnWorldLoad::onLoad);
            }
        }

        //Needs to get called after setupWorld
        public static void applyDatapackChangesOnNewWorld(WorldGenSettings worldGenSettings) {
            Optional<Holder<WorldPreset>> currentPreset = Optional.of(Helpers.defaultServerPreset());
            var settings = Helpers.defaultServerSettings(currentPreset.orElseThrow());

            currentPreset = WorldEventsImpl.ADAPT_WORLD_PRESET.emit(currentPreset, worldGenSettings);
            if (currentPreset.map(h -> h.value()).orElse(null) instanceof TogetherWorldPreset t) {
                settings = t.settings;
            }
            TogetherWorldPreset.writeWorldPresetSettings(settings);
            WorldEventsImpl.ON_WORLD_LOAD.emit(OnWorldLoad::onLoad);
        }
    }

    public static class InGUI {
        public static void registryReadyOnNewWorld(WorldGenSettingsComponent worldGenSettingsComponent) {
            Helpers.onRegistryReady(worldGenSettingsComponent.registryHolder());
        }

        public static void registryReadyOnLoadedWorld(Optional<RegistryOps<Tag>> registryOps) {
            if (registryOps.orElse(null) instanceof RegistryOpsAccessor acc) {
                Helpers.onRegistryReady(acc.bcl_getRegistryAccess());
            }
        }

        public static void setupNewWorld(
                Optional<LevelStorageSource.LevelStorageAccess> levelStorageAccess,
                WorldGenSettingsComponent worldGenSettingsComponent
        ) {
            if (levelStorageAccess.isPresent()) {
                if (worldGenSettingsComponent instanceof WorldGenSettingsComponentAccessor acc) {
                    Optional<Holder<WorldPreset>> currentPreset = acc.bcl_getPreset();
                    currentPreset = Helpers.presetFromDatapack(currentPreset);
                    Optional<Holder<WorldPreset>> newPreset = setupNewWorldCommon(
                            levelStorageAccess.get(),
                            currentPreset,
                            worldGenSettingsComponent.settings().worldGenSettings()
                    );
                    if (newPreset != currentPreset) {
                        acc.bcl_setPreset(newPreset);
                    }
                } else {
                    WorldsTogether.LOGGER.error("Unable to access WorldGenSettingsComponent.");
                }
            } else {
                WorldsTogether.LOGGER.error("Unable to access Level Folder.");
            }

        }

        static Optional<Holder<WorldPreset>> setupNewWorldCommon(
                LevelStorageSource.LevelStorageAccess levelStorageAccess,
                Optional<Holder<WorldPreset>> currentPreset,
                WorldGenSettings worldgenSettings
        ) {
            Helpers.initializeWorldDataAPI(levelStorageAccess, true);


            final WorldPresetSettings settings;
            if (currentPreset.map(Holder::value).orElse(null) instanceof TogetherWorldPreset t) {
                settings = t.settings;
            } else {
                settings = VanillaWorldPresetSettings.DEFAULT;
            }

            // Helpers.setupWorld();
            // DataFixerAPI.initializePatchData();
            WorldEventsImpl.BEFORE_WORLD_LOAD.emit(e -> e.prepareWorld(
                    levelStorageAccess,
                    settings,
                    true
            ));

            currentPreset = WorldEventsImpl.ADAPT_WORLD_PRESET.emit(currentPreset, worldgenSettings);

            TogetherWorldPreset.writeWorldPresetSettings(currentPreset);

            //LifeCycleAPI._runBeforeLevelLoad();
            WorldEventsImpl.ON_WORLD_LOAD.emit(OnWorldLoad::onLoad);

            return currentPreset;
        }

        /**
         * Does not call {@link WorldEventsImpl#ON_WORLD_LOAD}
         */
        public static void setupLoadedWorld(
                String levelID,
                LevelStorageSource levelSource
        ) {
            try {
                var levelStorageAccess = levelSource.createAccess(levelID);
                Helpers.initializeWorldDataAPI(levelStorageAccess, false);

                //Helpers.setupWorld();
                WorldEventsImpl.BEFORE_WORLD_LOAD.emit(e -> e.prepareWorld(
                        levelStorageAccess,
                        WorldGenUtil.getWorldSettings(),
                        false
                ));
                levelStorageAccess.close();
            } catch (Exception e) {
                BCLib.LOGGER.error("Failed to initialize data in world", e);
            }
        }

        public static boolean applyWorldPatches(
                LevelStorageSource levelSource,
                String levelID,
                Consumer<Boolean> onResume
        ) {
            boolean result = false;
            try {
                var levelStorageAccess = levelSource.createAccess(levelID);
                result = WorldEventsImpl.PATCH_WORLD.applyPatches(levelStorageAccess, onResume);
                levelStorageAccess.close();
            } catch (Exception e) {
                BCLib.LOGGER.error("Failed to initialize data in world", e);
            }

            return result;
        }

        public static void finishedWorldLoad(
                String levelID,
                LevelStorageSource levelSource
        ) {
            //LifeCycleAPI._runBeforeLevelLoad();
            WorldEventsImpl.ON_WORLD_LOAD.emit(OnWorldLoad::onLoad);
        }
    }

    public static class InFreshLevel {
        public static void setupNewWorld(
                String levelID,
                WorldGenSettings worldGenSettings,
                LevelStorageSource levelSource,
                Optional<Holder<WorldPreset>> worldPreset
        ) {
            try {
                var levelStorageAccess = levelSource.createAccess(levelID);
                InGUI.setupNewWorldCommon(levelStorageAccess, worldPreset, worldGenSettings);
                levelStorageAccess.close();
            } catch (Exception e) {
                BCLib.LOGGER.error("Failed to initialize data in world", e);
            }
        }
    }

    public static WorldGenSettings enforceInNewWorld(WorldGenSettings worldGenSettings) {
        worldGenSettings = WorldGenUtil
                .getWorldSettings()
                .repairSettingsOnLoad(LAST_REGISTRY_ACCESS, worldGenSettings);
        return worldGenSettings;
    }

    public static WorldGenSettings enforceInLoadedWorld(
            Optional<RegistryOps<Tag>> registryOps,
            WorldGenSettings worldGenSettings
    ) {
        if (registryOps.orElse(null) instanceof RegistryOpsAccessor acc) {
            return WorldGenUtil
                    .getWorldSettings()
                    .repairSettingsOnLoad(acc.bcl_getRegistryAccess(), worldGenSettings);
            //.repairSettingsOnLoad(LAST_REGISTRY_ACCESS, worldGenSettings);
        } else {
            BCLib.LOGGER.error("Unable to obtain registryAccess when enforcing generators.");
        }
        return worldGenSettings;
    }

}
