package org.betterx.bclib.api.v2.levelgen;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.LifeCycleAPI;
import org.betterx.bclib.api.v2.dataexchange.DataExchangeAPI;
import org.betterx.bclib.api.v2.datafixer.DataFixerAPI;
import org.betterx.bclib.api.v2.levelgen.biomes.InternalBiomeAPI;
import org.betterx.bclib.api.v2.poi.PoiManager;
import org.betterx.worlds.together.tag.v3.TagManager;
import org.betterx.worlds.together.world.WorldConfig;
import org.betterx.worlds.together.world.event.WorldEvents;
import org.betterx.wover.events.api.WorldLifecycle;
import org.betterx.wover.events.api.types.OnRegistryReady;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class LevelGenEvents {
    public static void setupWorld() {
        InternalBiomeAPI.prepareNewLevel();
        DataExchangeAPI.prepareServerside();
    }

    public static void register() {
        WorldEvents.BEFORE_WORLD_LOAD.on(LevelGenEvents::beforeWorldLoad);

        WorldEvents.ON_WORLD_LOAD.on(LevelGenEvents::onWorldLoad);
        WorldLifecycle.WORLD_REGISTRY_READY.subscribe(LevelGenEvents::worldRegistryReady);
        WorldEvents.ON_FINALIZE_LEVEL_STEM.on(LevelGenEvents::finalizeStem);
        WorldEvents.ON_FINALIZED_WORLD_LOAD.on(LevelGenEvents::finalizedWorldLoad);

        WorldEvents.PATCH_WORLD.on(LevelGenEvents::patchExistingWorld);

        WorldEvents.BEFORE_ADDING_TAGS.on(LevelGenEvents::applyBiomeTags);
    }


    private static void applyBiomeTags(
            String directory,
            Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagsMap
    ) {
        if (directory.equals(TagManager.BIOMES.directory)) {
            InternalBiomeAPI._runBiomeTagAdders();
        }
    }


    private static boolean patchExistingWorld(
            LevelStorageSource.LevelStorageAccess storageAccess,
            Consumer<Boolean> allDone
    ) {
        final Path dataPath = storageAccess.getLevelPath(LevelResource.ROOT).resolve("data");
        WorldConfig.setDataDir(dataPath.toFile());
        return DataFixerAPI.fixData(storageAccess, allDone != null && BCLib.isClient(), allDone);
    }
    
    private static void worldRegistryReady(RegistryAccess registryAccess, OnRegistryReady.Stage stage) {
        InternalBiomeAPI.initRegistry(registryAccess);
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

    private static void finalizeStem(
            Registry<LevelStem> dimensionRegistry,
            ResourceKey<LevelStem> dimension,
            LevelStem levelStem
    ) {
        InternalBiomeAPI.applyModifications(levelStem.generator().getBiomeSource(), dimension);
    }

    private static void finalizedWorldLoad(Registry<LevelStem> dimensionRegistry) {
        PoiManager.updateStates();
    }
}
