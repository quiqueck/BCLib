package org.betterx.worlds.together.world.event;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.config.Configs;
import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.levelgen.WorldGenUtil;
import org.betterx.worlds.together.mixin.common.WorldPresetAccessor;
import org.betterx.worlds.together.surfaceRules.SurfaceRuleUtil;
import org.betterx.worlds.together.world.WorldConfig;
import org.betterx.worlds.together.worldPreset.TogetherWorldPreset;
import org.betterx.worlds.together.worldPreset.WorldPresets;

import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.Holder;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.io.File;
import java.util.List;
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

        private static Holder<WorldPreset> defaultServerPreset() {
            return WorldPresets.get(
                    LAST_REGISTRY_ACCESS,
                    WorldPresets.getDEFAULT()
            );
        }

        private static WorldDimensions defaultServerDimensions() {
            final Holder<WorldPreset> defaultPreset = defaultServerPreset();
            return defaultServerDimensions(defaultPreset);
        }

        private static WorldDimensions defaultServerDimensions(Holder<WorldPreset> defaultPreset) {
            final WorldDimensions dimensions;
            if (defaultPreset.value() instanceof TogetherWorldPreset t) {
                dimensions = t.getWorldDimensions();
            } else {
                dimensions = TogetherWorldPreset.getWorldDimensions(net.minecraft.world.level.levelgen.presets.WorldPresets.NORMAL);
            }
            return dimensions;
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
            File levelDat = levelStorageAccess.getLevelPath(LevelResource.LEVEL_DATA_FILE).toFile();
            if (!levelDat.exists()) {
                WorldsTogether.LOGGER.info("Creating a new World, no fixes needed");
                final WorldDimensions dimensions = Helpers.defaultServerDimensions();

                WorldBootstrap.setupWorld(
                        levelStorageAccess, TogetherWorldPreset.getDimensionMap(dimensions),
                        true, true
                );

                Holder<WorldPreset> currentPreset = Helpers.defaultServerPreset();
                writeWorldPresets(dimensions, currentPreset);
                finishedWorldLoad();
            } else {
                WorldBootstrap.setupWorld(
                        levelStorageAccess, TogetherWorldPreset.loadWorldDimensions(),
                        false, true
                );
                finishedWorldLoad();
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
                        currentPreset,
                        uiState.getSettings().selectedDimensions(),
                        recreated
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
                Holder<WorldPreset> currentPreset,
                WorldDimensions worldDims,
                boolean recreated
        ) {
            final WorldDimensions dimensions;
            if (currentPreset != null && currentPreset.value() instanceof TogetherWorldPreset t) {
                dimensions = t.getWorldDimensions();
            } else if (recreated) {
                dimensions = worldDims;
            } else {
                dimensions = TogetherWorldPreset.getWorldDimensions(net.minecraft.world.level.levelgen.presets.WorldPresets.NORMAL);
            }

            setupWorld(levelStorageAccess, TogetherWorldPreset.getDimensionMap(dimensions), true, false);
            writeWorldPresets(worldDims, currentPreset);
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
                        TogetherWorldPreset.loadWorldDimensions(),
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
                InGUI.setupNewWorldCommon(levelStorageAccess, worldPreset, worldDims, false);
                levelStorageAccess.close();
            } catch (Exception e) {
                WorldsTogether.LOGGER.error("Failed to initialize data in world", e);
            }
        }
    }

    private static void setupWorld(
            LevelStorageSource.LevelStorageAccess levelStorageAccess,
            Map<ResourceKey<LevelStem>, ChunkGenerator> dimensions,
            boolean newWorld, boolean isServer
    ) {
        try {
            Helpers.initializeWorldConfig(levelStorageAccess, newWorld);
            WorldEventsImpl.BEFORE_WORLD_LOAD.emit(e -> e.prepareWorld(
                    levelStorageAccess,
                    dimensions,
                    newWorld, isServer
            ));
        } catch (Exception e) {
            WorldsTogether.LOGGER.error("Failed to initialize data in world", e);
        }
    }

    private static void writeWorldPresets(WorldDimensions dimensions, Holder<WorldPreset> currentPreset) {
        currentPreset = WorldEventsImpl.ADAPT_WORLD_PRESET.emit(currentPreset, dimensions);

        if (currentPreset != null && currentPreset.value() instanceof WorldPresetAccessor acc) {
            TogetherWorldPreset.writeWorldPresetSettings(acc.bcl_getDimensions());
        } else {
            WorldsTogether.LOGGER.error("Failed writing together File");
            TogetherWorldPreset.writeWorldPresetSettings(dimensions);
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
        final Registry<LevelStem> dimensions = access.registryOrThrow(Registries.LEVEL_STEM);
        final Registry<LevelStem> changedDimensions = WorldGenUtil.repairBiomeSourceInAllDimensions(access, dimensions);
        if (dimensions != changedDimensions) {
            if (Configs.MAIN_CONFIG.verboseLogging()) {
                WorldsTogether.LOGGER.info("Loading originally configured Dimensions in World.");
            }
            registries = registries.replaceFrom(
                    RegistryLayer.DIMENSIONS,
                    new RegistryAccess.ImmutableRegistryAccess(List.of(changedDimensions)).freeze()
            );
            //this will generate a new access object we have to use from now on...
            Helpers.onRegistryReady(registries.compositeAccess());
        }
        return registries;
    }


}
