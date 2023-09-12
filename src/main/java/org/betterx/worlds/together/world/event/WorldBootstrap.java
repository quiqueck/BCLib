package org.betterx.worlds.together.world.event;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.config.Configs;
import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.surfaceRules.SurfaceRuleUtil;
import org.betterx.worlds.together.world.WorldConfig;

import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.Holder;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
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

    private static byte WARN_COUNT_GLOBAL_REGISTRY = 0;

    public static RegistryAccess getLastRegistryAccessOrElseBuiltin() {
        if (WARN_COUNT_GLOBAL_REGISTRY < 10 && LAST_REGISTRY_ACCESS == null && Configs.MAIN_CONFIG.verboseLogging()) {
            WorldsTogether.LOGGER.error("Tried to read from global registry!");
            WARN_COUNT_GLOBAL_REGISTRY++;
        }
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


        private static Holder<WorldPreset> presetFromDatapack(Holder<WorldPreset> currentPreset) {
            if (currentPreset != null && LAST_REGISTRY_ACCESS != null) {
                Optional<ResourceKey<WorldPreset>> presetKey = currentPreset.unwrapKey();
                if (presetKey.isPresent()) {
                    Optional<Holder.Reference<WorldPreset>> newPreset = LAST_REGISTRY_ACCESS
                            .registryOrThrow(Registries.WORLD_PRESET)
                            .getHolder(presetKey.get());
                    currentPreset = newPreset.orElse(null);
                }
            }
            return currentPreset;
        }
    }

    public static class DedicatedServer {
        public static void registryReady(RegistryAccess acc) {
            Helpers.onRegistryReady(acc);
        }

        public static void setupWorld(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
            if (levelStorageAccess != null && levelStorageAccess.hasWorldData()) {
                File levelDat = levelStorageAccess.getLevelPath(LevelResource.LEVEL_DATA_FILE).toFile();
                if (!levelDat.exists()) {
                    WorldsTogether.LOGGER.info("Creating a new World, no fixes needed");

                    WorldBootstrap.setupWorld(levelStorageAccess, true, true);
                    finishedWorldLoad();
                } else {
                    WorldBootstrap.setupWorld(levelStorageAccess, false, true);
                    finishedWorldLoad();
                }
            }
        }

        public static boolean applyWorldPatches(
                LevelStorageSource.LevelStorageAccess levelStorageAccess
        ) {
            boolean result = false;
            if (levelStorageAccess.getLevelPath(LevelResource.LEVEL_DATA_FILE).toFile().exists()) {
                try {
                    result = WorldEventsImpl.PATCH_WORLD.applyPatches(levelStorageAccess, null);
                } catch (Exception e) {
                    WorldsTogether.LOGGER.error("Failed to initialize data in world", e);
                }
            }

            return result;
        }
    }

    public static class InGUI {
        public static void registryReadyOnNewWorld(WorldCreationContext worldGenSettingsComponent) {
            Helpers.onRegistryReady(worldGenSettingsComponent.worldgenLoadContext());
        }

        public static void registryReady(RegistryAccess access) {
            Helpers.onRegistryReady(access);
        }

        public static void setupNewWorld(
                Optional<LevelStorageSource.LevelStorageAccess> levelStorageAccess,
                WorldCreationUiState uiState,
                boolean recreated
        ) {

            if (levelStorageAccess.isPresent()) {
                Holder<WorldPreset> currentPreset = uiState.getWorldType().preset();
                currentPreset = Helpers.presetFromDatapack(currentPreset);
                Holder<WorldPreset> newPreset = setupNewWorldCommon(
                        levelStorageAccess.get(),
                        currentPreset
                );
                if (newPreset != null && newPreset != currentPreset) {
                    uiState.setWorldType(new WorldCreationUiState.WorldTypeEntry(newPreset));
                }
            } else {
                WorldsTogether.LOGGER.error("Unable to access Level Folder.");
            }

        }

        static Holder<WorldPreset> setupNewWorldCommon(
                LevelStorageSource.LevelStorageAccess levelStorageAccess,
                Holder<WorldPreset> currentPreset
        ) {
            setupWorld(levelStorageAccess, true, false);
            finishedWorldLoad();

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
                WorldBootstrap.setupWorld(
                        levelStorageAccess,
                        false, false
                );
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

    }

    public static class InFreshLevel {
        public static void setupNewWorld(
                String levelID,
                WorldDimensions worldDims,
                LevelStorageSource levelSource,
                Holder<WorldPreset> worldPreset
        ) {
            try {
                var levelStorageAccess = levelSource.createAccess(levelID);
                InGUI.setupNewWorldCommon(levelStorageAccess, worldPreset);
                levelStorageAccess.close();
            } catch (Exception e) {
                WorldsTogether.LOGGER.error("Failed to initialize data in world", e);
            }
        }
    }

    private static void setupWorld(
            LevelStorageSource.LevelStorageAccess levelStorageAccess,
            boolean newWorld, boolean isServer
    ) {
        try {
            Helpers.initializeWorldConfig(levelStorageAccess, newWorld);
            WorldEventsImpl.BEFORE_WORLD_LOAD.emit(e -> e.prepareWorld(
                    levelStorageAccess,
                    newWorld, isServer
            ));
        } catch (Exception e) {
            WorldsTogether.LOGGER.error("Failed to initialize data in world", e);
        }
    }

    public static void finishedWorldLoad() {
        WorldEventsImpl.ON_WORLD_LOAD.emit(OnWorldLoad::onLoad);
    }

    public static void finalizeWorldGenSettings(Registry<LevelStem> dimensionRegistry) {
        String output = "World Dimensions: ";
        for (var entry : dimensionRegistry.entrySet()) {
            WorldEventsImpl.ON_FINALIZE_LEVEL_STEM.emit(e -> e.now(
                    dimensionRegistry,
                    entry.getKey(),
                    entry.getValue()
            ));

            if (Configs.MAIN_CONFIG.verboseLogging())
                output += "\n - " + entry.getKey().location().toString() + ": " +
                        "\n     " + entry.getValue().generator().toString() + " " +
                        entry.getValue()
                             .generator()
                             .getBiomeSource()
                             .toString()
                             .replace("\n", "\n     ");
        }
        if (Configs.MAIN_CONFIG.verboseLogging())
            BCLib.LOGGER.info(output);
        SurfaceRuleUtil.injectSurfaceRulesToAllDimensions(dimensionRegistry);

        WorldEventsImpl.ON_FINALIZED_WORLD_LOAD.emit(e -> e.done(dimensionRegistry));
    }

    public static LayeredRegistryAccess<RegistryLayer> enforceInLayeredRegistry(LayeredRegistryAccess<RegistryLayer> registries) {
        RegistryAccess access = registries.compositeAccess();
        Helpers.onRegistryReady(access);
        return registries;
    }


}
