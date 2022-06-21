package org.betterx.bclib.api.v2.levelgen;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.LifeCycleAPI;
import org.betterx.bclib.api.v2.dataexchange.DataExchangeAPI;
import org.betterx.bclib.api.v2.datafixer.DataFixerAPI;
import org.betterx.bclib.api.v2.generator.BCLibEndBiomeSource;
import org.betterx.bclib.api.v2.levelgen.biomes.InternalBiomeAPI;
import org.betterx.bclib.presets.worldgen.BCLWorldPresetSettings;
import org.betterx.bclib.registry.PresetsRegistry;
import org.betterx.worlds.together.world.event.WorldEvents;
import org.betterx.worlds.together.worldPreset.TogetherWorldPreset;
import org.betterx.worlds.together.worldPreset.settings.WorldPresetSettings;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.util.Optional;
import java.util.function.Consumer;

public class LevelGenEvents {
    public static void setupWorld() {
        InternalBiomeAPI.prepareNewLevel();
        DataExchangeAPI.prepareServerside();
    }

    public static void register() {
        WorldEvents.BEFORE_WORLD_LOAD.on(LevelGenEvents::prepareWorld);
        WorldEvents.BEFORE_SERVER_WORLD_LOAD.on(LevelGenEvents::prepareServerWorld);

        WorldEvents.ON_WORLD_LOAD.on(LevelGenEvents::onWorldLoad);
        WorldEvents.WORLD_REGISTRY_READY.on(LevelGenEvents::onRegistryReady);

        WorldEvents.PATCH_WORLD.on(LevelGenEvents::patchExistingWorld);
        WorldEvents.ADAPT_WORLD_PRESET.on(LevelGenEvents::adaptWorldPresetSettings);
    }

    public static boolean patchExistingWorld(
            LevelStorageSource.LevelStorageAccess storageAccess,
            Consumer<Boolean> allDone
    ) {
        return DataFixerAPI.fixData(storageAccess, true, allDone);
    }

    public static Optional<Holder<WorldPreset>> adaptWorldPresetSettings(
            Optional<Holder<WorldPreset>> currentPreset,
            WorldGenSettings worldGenSettings
    ) {
        LevelStem endStem = worldGenSettings.dimensions().get(LevelStem.END);

        //We probably loaded a Datapack for the End
        if (!(endStem.generator().getBiomeSource() instanceof BCLibEndBiomeSource)) {


            if (currentPreset.isPresent()) {
                if (currentPreset.get().value() instanceof TogetherWorldPreset worldPreset) {
                    ResourceKey key = currentPreset.get().unwrapKey().orElse(null);
                    //user did not configure the Preset!
                    if (PresetsRegistry.BCL_WORLD.equals(key) || PresetsRegistry.BCL_WORLD_17.equals(key)) {
                        BCLib.LOGGER.info("Detected Datapack for END.");
                        
                        if (worldPreset.settings instanceof BCLWorldPresetSettings settings) {
                            BCLib.LOGGER.info("Changing Default WorldPreset Settings for Datapack use.");

                            worldPreset = worldPreset.withSettings(new BCLWorldPresetSettings(
                                    settings.netherVersion,
                                    settings.endVersion,
                                    false,
                                    false
                            ));
                            currentPreset = Optional.of(Holder.direct(worldPreset));
                        }
                    }
                }
            }
        }
        return currentPreset;
    }

    public static void onRegistryReady(RegistryAccess a) {
        InternalBiomeAPI.initRegistry(a);
    }

    public static WorldPresetSettings prepareWorld(
            LevelStorageSource.LevelStorageAccess storageAccess,
            WorldPresetSettings settings,
            boolean isNewWorld
    ) {
        setupWorld();
        if (isNewWorld) {
            DataFixerAPI.initializePatchData();
        }
        return settings;
    }

    public static WorldPresetSettings prepareServerWorld(
            LevelStorageSource.LevelStorageAccess storageAccess,
            WorldPresetSettings settings,
            boolean isNewWorld
    ) {
        setupWorld();

        if (isNewWorld) {
            DataFixerAPI.initializePatchData();
        } else {
            DataFixerAPI.fixData(storageAccess, false, (didFix) -> {/* not called when showUI==false */});
        }


        return settings;
    }

    public static void onWorldLoad() {
        LifeCycleAPI._runBeforeLevelLoad();
    }
}
