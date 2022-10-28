package org.betterx.worlds.together.world.event;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.config.Configs;
import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.levelgen.WorldGenUtil;
import org.betterx.worlds.together.mixin.common.RegistryOpsAccessor;
import org.betterx.worlds.together.mixin.common.WorldPresetAccessor;
import org.betterx.worlds.together.surfaceRules.SurfaceRuleUtil;
import org.betterx.worlds.together.world.WorldConfig;
import org.betterx.worlds.together.worldPreset.TogetherWorldPreset;
import org.betterx.worlds.together.worldPreset.WorldGenSettingsComponentAccessor;
import org.betterx.worlds.together.worldPreset.WorldPresets;

import net.minecraft.client.gui.screens.worldselection.WorldGenSettingsComponent;
import net.minecraft.core.Holder;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
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

    public static RegistryAccess getLastRegistryAccessOrElseBuiltin() {
        if (LAST_REGISTRY_ACCESS == null) {
            WorldsTogether.LOGGER.error("Tried to read from global registry!");
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

        private static Optional<Holder<WorldPreset>> presetFromDatapack(Optional<Holder<WorldPreset>> currentPreset) {
            if (currentPreset.isPresent() && LAST_REGISTRY_ACCESS != null) {
                var presetKey = currentPreset.get().unwrapKey();
                if (presetKey.isPresent()) {
                    Optional<Holder.Reference<WorldPreset>> newPreset = LAST_REGISTRY_ACCESS
                            .registryOrThrow(Registry.WORLD_PRESET_REGISTRY)
                            .getHolder(presetKey.get());
                    if (newPreset.isPresent()) currentPreset = (Optional<Holder<WorldPreset>>) (Optional<?>) newPreset;
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
                final WorldDimensions dimensions = Helpers.defaultServerDimensions();

                WorldBootstrap.setupWorld(
                        levelStorageAccess, TogetherWorldPreset.getDimensionMap(dimensions),
                        true, true
                );

                Optional<Holder<WorldPreset>> currentPreset = Optional.of(Helpers.defaultServerPreset());
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
        public static void registryReadyOnNewWorld(WorldGenSettingsComponent worldGenSettingsComponent) {
            Helpers.onRegistryReady(worldGenSettingsComponent.registryHolder());
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
                            worldGenSettingsComponent.settings().selectedDimensions()
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
                WorldDimensions worldDims
        ) {
            final WorldDimensions dimensions;
            if (currentPreset.map(Holder::value).orElse(null) instanceof TogetherWorldPreset t) {
                dimensions = t.getWorldDimensions();
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
                Optional<Holder<WorldPreset>> worldPreset
        ) {
            try {
                var levelStorageAccess = levelSource.createAccess(levelID);
                InGUI.setupNewWorldCommon(levelStorageAccess, worldPreset, worldDims);
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

    private static void writeWorldPresets(WorldDimensions dimensions, Optional<Holder<WorldPreset>> currentPreset) {
        currentPreset = WorldEventsImpl.ADAPT_WORLD_PRESET.emit(currentPreset, dimensions);

        if (currentPreset.map(Holder::value).orElse(null) instanceof WorldPresetAccessor acc) {
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
        final Registry<LevelStem> dimensions = access.registryOrThrow(Registry.LEVEL_STEM_REGISTRY);
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
