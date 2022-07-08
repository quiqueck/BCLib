package org.betterx.worlds.together.world.event;

import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.levelgen.WorldGenUtil;
import org.betterx.worlds.together.mixin.common.RegistryOpsAccessor;
import org.betterx.worlds.together.mixin.common.WorldPresetAccessor;
import org.betterx.worlds.together.surfaceRules.SurfaceRuleUtil;
import org.betterx.worlds.together.world.WorldConfig;
import org.betterx.worlds.together.worldPreset.TogetherWorldPreset;
import org.betterx.worlds.together.worldPreset.WorldGenSettingsComponentAccessor;
import org.betterx.worlds.together.worldPreset.WorldPreset;
import org.betterx.worlds.together.worldPreset.WorldPresets;

import net.minecraft.client.gui.screens.worldselection.WorldGenSettingsComponent;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class WorldBootstrap {
    private static RegistryAccess LAST_REGISTRY_ACCESS = null;

    public static RegistryAccess getLastRegistryAccess() {
        return LAST_REGISTRY_ACCESS;
    }

    public static RegistryAccess getLastRegistryAccessOrElseBuiltin() {
        if (LAST_REGISTRY_ACCESS == null) return BuiltinRegistries.ACCESS;
        return LAST_REGISTRY_ACCESS;
    }

    public static class Helpers {
        private static void initializeWorldConfig(
                LevelStorageSource.LevelStorageAccess levelStorageAccess,
                boolean newWorld
        ) {
            File levelPath = levelStorageAccess.getLevelPath(LevelResource.ROOT).toFile();
            initializeWorldConfig(levelPath, newWorld);
        }

        private static void initializeWorldConfig(File levelBaseDir, boolean newWorld) {
            WorldConfig.load(new File(levelBaseDir, "data"));
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
                    WorldPresets.getDEFAULT()
            );
        }

        private static Map<ResourceKey<LevelStem>, ChunkGenerator> defaultServerDimensions() {
            final Holder<WorldPreset> defaultPreset = defaultServerPreset();
            return defaultServerDimensions(defaultPreset);
        }

        private static Map<ResourceKey<LevelStem>, ChunkGenerator> defaultServerDimensions(Holder<WorldPreset> defaultPreset) {
            final Map<ResourceKey<LevelStem>, ChunkGenerator> dimensions;
            if (defaultPreset.value() instanceof TogetherWorldPreset t) {
                dimensions = t.getDimensionsMap();
            } else {
                dimensions = TogetherWorldPreset.getDimensionsMap(net.minecraft.world.level.levelgen.presets.WorldPresets.NORMAL);
            }
            return dimensions;
        }

        private static Optional<Holder<WorldPreset>> presetFromDatapack(Optional<Holder<WorldPreset>> currentPreset) {
            if (currentPreset.isPresent() && LAST_REGISTRY_ACCESS != null) {
                var presetKey = currentPreset.get().unwrapKey();
                if (presetKey.isPresent()) {
                    Optional<Holder<WorldPreset>> newPreset = LAST_REGISTRY_ACCESS
                            .registryOrThrow(WorldPresets.WORLD_PRESET_REGISTRY)
                            .getHolder(presetKey.get());
                    if (newPreset.isPresent()) currentPreset = newPreset;
                }
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
                WorldsTogether.LOGGER.info("Creating a new World, no fixes needed");
                final Map<ResourceKey<LevelStem>, ChunkGenerator> settings = Helpers.defaultServerDimensions();

                Helpers.initializeWorldConfig(levelStorageAccess, true);
                WorldEventsImpl.BEFORE_SERVER_WORLD_LOAD.emit(e -> e.prepareWorld(
                        levelStorageAccess, settings, true
                ));
            } else {
                Helpers.initializeWorldConfig(levelStorageAccess, false);
                WorldEventsImpl.BEFORE_SERVER_WORLD_LOAD.emit(e -> e.prepareWorld(
                        levelStorageAccess,
                        TogetherWorldPreset.loadWorldDimensions(),
                        false
                ));
                WorldEventsImpl.ON_WORLD_LOAD.emit(OnWorldLoad::onLoad);
            }
        }

        //Needs to get called after setupWorld
        public static void applyDatapackChangesOnNewWorld(WorldGenSettings worldGenSettings) {
            Optional<Holder<WorldPreset>> currentPreset = Optional.of(Helpers.defaultServerPreset());
            currentPreset = WorldEventsImpl.ADAPT_WORLD_PRESET.emit(currentPreset, worldGenSettings);

            if (currentPreset.map(Holder::value).orElse(null) instanceof WorldPresetAccessor acc) {
                TogetherWorldPreset.writeWorldPresetSettings(acc.bcl_getDimensions());
            } else {
                WorldsTogether.LOGGER.error("Failed writing together File");
                TogetherWorldPreset.writeWorldPresetSettings(worldGenSettings.dimensions());
            }
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

        public static void registryReady(RegistryAccess access) {
            Helpers.onRegistryReady(access);
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
                WorldGenSettings worldGenSettings
        ) {
            Helpers.initializeWorldConfig(levelStorageAccess, true);


            final Map<ResourceKey<LevelStem>, ChunkGenerator> dimensions;
            if (currentPreset.map(Holder::value).orElse(null) instanceof TogetherWorldPreset t) {
                dimensions = t.getDimensionsMap();
            } else {
                dimensions = TogetherWorldPreset.getDimensionsMap(net.minecraft.world.level.levelgen.presets.WorldPresets.NORMAL);
            }

            // Helpers.setupWorld();
            // DataFixerAPI.initializePatchData();
            WorldEventsImpl.BEFORE_WORLD_LOAD.emit(e -> e.prepareWorld(
                    levelStorageAccess,
                    dimensions,
                    true
            ));

            currentPreset = WorldEventsImpl.ADAPT_WORLD_PRESET.emit(currentPreset, worldGenSettings);

            if (currentPreset.map(Holder::value).orElse(null) instanceof WorldPresetAccessor acc) {
                TogetherWorldPreset.writeWorldPresetSettings(acc.bcl_getDimensions());
            } else {
                WorldsTogether.LOGGER.error("Failed writing together File");
                TogetherWorldPreset.writeWorldPresetSettings(worldGenSettings.dimensions());
            }

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
                try {
                    Helpers.initializeWorldConfig(levelStorageAccess, false);

                    //Helpers.setupWorld();
                    WorldEventsImpl.BEFORE_WORLD_LOAD.emit(e -> e.prepareWorld(
                            levelStorageAccess,
                            TogetherWorldPreset.loadWorldDimensions(),
                            false
                    ));
                } catch (Exception e) {
                    WorldsTogether.LOGGER.error("Failed to initialize data in world", e);
                }
                levelStorageAccess.close();
            } catch (Exception e) {
                WorldsTogether.LOGGER.error("Failed to acquire storage access", e);
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
                WorldsTogether.LOGGER.error("Failed to initialize data in world", e);
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
                WorldsTogether.LOGGER.error("Failed to initialize data in world", e);
            }
        }
    }

    public static void finalizeWorldGenSettings(WorldGenSettings worldGenSettings) {
        for (var entry : worldGenSettings.dimensions().entrySet()) {
            WorldEventsImpl.ON_FINALIZE_LEVEL_STEM.emit(e -> e.now(
                    worldGenSettings,
                    entry.getKey(),
                    entry.getValue()
            ));
        }
        SurfaceRuleUtil.injectSurfaceRulesToAllDimensions(worldGenSettings);
    }

    public static WorldGenSettings enforceInNewWorld(WorldGenSettings worldGenSettings) {
        return WorldGenUtil.repairBiomeSourceInAllDimensions(LAST_REGISTRY_ACCESS, worldGenSettings);
    }

    public static WorldGenSettings enforceInLoadedWorld(
            Optional<RegistryOps<Tag>> registryOps,
            WorldGenSettings worldGenSettings
    ) {
        if (registryOps.orElse(null) instanceof RegistryOpsAccessor acc) {
            return WorldGenUtil.repairBiomeSourceInAllDimensions(acc.bcl_getRegistryAccess(), worldGenSettings);
            //.repairSettingsOnLoad(LAST_REGISTRY_ACCESS, worldGenSettings);
        } else {
            WorldsTogether.LOGGER.error("Unable to obtain registryAccess when enforcing generators.");
        }
        return worldGenSettings;
    }

}
