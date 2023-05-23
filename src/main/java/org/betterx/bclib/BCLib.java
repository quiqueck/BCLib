package org.betterx.bclib;

import org.betterx.bclib.api.v2.dataexchange.DataExchangeAPI;
import org.betterx.bclib.api.v2.dataexchange.handler.autosync.*;
import org.betterx.bclib.api.v2.generator.BCLibEndBiomeSource;
import org.betterx.bclib.api.v2.generator.BCLibNetherBiomeSource;
import org.betterx.bclib.api.v2.generator.GeneratorOptions;
import org.betterx.bclib.api.v2.levelgen.LevelGenEvents;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeRegistry;
import org.betterx.bclib.api.v2.levelgen.structures.TemplatePiece;
import org.betterx.bclib.api.v2.levelgen.surface.rules.Conditions;
import org.betterx.bclib.api.v2.poi.PoiManager;
import org.betterx.bclib.api.v3.levelgen.features.blockpredicates.BlockPredicates;
import org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers;
import org.betterx.bclib.api.v3.tag.BCLBlockTags;
import org.betterx.bclib.commands.CommandRegistry;
import org.betterx.bclib.config.Configs;
import org.betterx.bclib.networking.VersionChecker;
import org.betterx.bclib.recipes.AlloyingRecipe;
import org.betterx.bclib.recipes.AnvilRecipe;
import org.betterx.bclib.recipes.CraftingRecipes;
import org.betterx.bclib.registry.BaseBlockEntities;
import org.betterx.bclib.registry.BaseRegistry;
import org.betterx.bclib.registry.PresetsRegistry;
import org.betterx.datagen.bclib.tests.TestStructure;
import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.util.Logger;
import org.betterx.worlds.together.world.WorldConfig;

import net.minecraft.resources.ResourceLocation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.util.List;

public class BCLib implements ModInitializer {
    public static final String MOD_ID = "bclib";
    public static final Logger LOGGER = new Logger(MOD_ID);

    public static final boolean RUNS_NULLSCAPE = FabricLoader.getInstance()
                                                             .getModContainer("nullscape")
                                                             .isPresent();
    public static final boolean ADD_TEST_DATA = false;

    private void onDatagen() {

    }


    @Override
    public void onInitialize() {
        WorldsTogether.onInitialize();
        PresetsRegistry.register();
        LevelGenEvents.register();
        BlockPredicates.ensureStaticInitialization();
        BCLBiomeRegistry.register();
        BaseRegistry.register();
        GeneratorOptions.init();
        BaseBlockEntities.register();
        BCLibEndBiomeSource.register();
        BCLibNetherBiomeSource.register();
        CraftingRecipes.init();
        WorldConfig.registerModCache(MOD_ID);
        DataExchangeAPI.registerMod(MOD_ID);
        AnvilRecipe.register();
        AlloyingRecipe.register();
        Conditions.registerAll();
        CommandRegistry.register();
        BCLBlockTags.ensureStaticallyLoaded();
        PoiManager.registerAll();
        if (isDevEnvironment()) {
            TestStructure.registerBase();
        }

        DataExchangeAPI.registerDescriptors(List.of(
                        HelloClient.DESCRIPTOR,
                        HelloServer.DESCRIPTOR,
                        RequestFiles.DESCRIPTOR,
                        SendFiles.DESCRIPTOR,
                        Chunker.DESCRIPTOR
                )
        );

        BCLibPatch.register();
        TemplatePiece.ensureStaticInitialization();
        PlacementModifiers.ensureStaticInitialization();
        Configs.save();

        WorldsTogether.FORCE_SERVER_TO_BETTERX_PRESET = Configs.SERVER_CONFIG.forceBetterXPreset();
        VersionChecker.registerMod(MOD_ID);

        if (isDatagen()) {
            onDatagen();
        }
    }

    public static boolean isDevEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static boolean isDatagen() {
        return System.getProperty("fabric-api.datagen") != null;
    }

    public static boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public static ResourceLocation makeID(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
