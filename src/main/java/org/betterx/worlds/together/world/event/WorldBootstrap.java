package org.betterx.worlds.together.world.event;

import org.betterx.bclib.BCLib;
import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.world.WorldConfig;
import org.betterx.wover.state.api.WorldState;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class WorldBootstrap {
    public static RegistryAccess getLastRegistryAccess() {
        return WorldState.allStageRegistryAccess();
        //return LAST_REGISTRY_ACCESS;
    }

    private static byte WARN_COUNT_GLOBAL_REGISTRY = 0;

    public static RegistryAccess getLastRegistryAccessOrElseBuiltin() {
        if (WARN_COUNT_GLOBAL_REGISTRY < 10 && getLastRegistryAccess() == null) {
            BCLib.LOGGER.verboseWarning("Tried to read from global registry!");
            WARN_COUNT_GLOBAL_REGISTRY++;
        }
        return getLastRegistryAccess();
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
    }

    public static class DedicatedServer {
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
        public static void setupNewWorld(
                Optional<LevelStorageSource.LevelStorageAccess> levelStorageAccess
        ) {

            if (levelStorageAccess.isPresent()) {
                setupNewWorldCommon(levelStorageAccess.get());
            } else {
                WorldsTogether.LOGGER.error("Unable to access Level Folder.");
            }

        }

        public static void setupNewWorldCommon(
                LevelStorageSource.LevelStorageAccess levelStorageAccess
        ) {
            setupWorld(levelStorageAccess, true, false);
            finishedWorldLoad();
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
        for (var entry : dimensionRegistry.entrySet()) {
            WorldEventsImpl.ON_FINALIZE_LEVEL_STEM.emit(e -> e.now(
                    dimensionRegistry,
                    entry.getKey(),
                    entry.getValue()
            ));
        }

        WorldEventsImpl.ON_FINALIZED_WORLD_LOAD.emit(e -> e.done(dimensionRegistry));
    }


}
