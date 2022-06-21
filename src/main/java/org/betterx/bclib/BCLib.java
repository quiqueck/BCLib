package org.betterx.bclib;

import org.betterx.bclib.api.v2.dataexchange.DataExchangeAPI;
import org.betterx.bclib.api.v2.dataexchange.handler.autosync.*;
import org.betterx.bclib.api.v2.generator.BCLibEndBiomeSource;
import org.betterx.bclib.api.v2.generator.BCLibNetherBiomeSource;
import org.betterx.bclib.api.v2.generator.GeneratorOptions;
import org.betterx.bclib.api.v2.levelgen.LevelGenEvents;
import org.betterx.bclib.api.v2.levelgen.features.blockpredicates.Types;
import org.betterx.bclib.api.v2.levelgen.features.placement.PlacementModifiers;
import org.betterx.bclib.api.v2.levelgen.structures.TemplatePiece;
import org.betterx.bclib.api.v2.levelgen.surface.rules.Conditions;
import org.betterx.bclib.commands.CommandRegistry;
import org.betterx.bclib.config.Configs;
import org.betterx.bclib.recipes.AnvilRecipe;
import org.betterx.bclib.recipes.CraftingRecipes;
import org.betterx.bclib.registry.BaseBlockEntities;
import org.betterx.bclib.registry.BaseRegistry;
import org.betterx.bclib.util.Logger;
import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.tag.TagManager;
import org.betterx.worlds.together.world.WorldConfig;

import net.minecraft.resources.ResourceLocation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.util.List;

public class BCLib implements ModInitializer {
    public static final String MOD_ID = "bclib";
    public static final Logger LOGGER = new Logger(MOD_ID);

    @Override
    public void onInitialize() {
        LevelGenEvents.register();
        WorldsTogether.onInitialize();
        Types.ensureStaticInitialization();
        BaseRegistry.register();
        GeneratorOptions.init();
        BaseBlockEntities.register();
        BCLibEndBiomeSource.register();
        BCLibNetherBiomeSource.register();
        TagManager.ensureStaticallyLoaded();
        CraftingRecipes.init();
        WorldConfig.registerModCache(MOD_ID);
        DataExchangeAPI.registerMod(MOD_ID);
        AnvilRecipe.register();
        Conditions.registerAll();
        CommandRegistry.register();

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

        /*if (isDevEnvironment()) {
            Biome.BiomeBuilder builder = new Biome.BiomeBuilder()
                    .precipitation(Biome.Precipitation.NONE)
                    .temperature(1.0f)
                    .downfall(1.0f)
                    .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                    .specialEffects(new BiomeSpecialEffects.Builder().fogColor(0xff00ff)
                                                                     .waterColor(0xff00ff)
                                                                     .waterFogColor(0xff00ff)
                                                                     .skyColor(0xff00ff)
                                                                     .build())
                    .generationSettings(new BiomeGenerationSettings.Builder().build());

            Biome biome = builder.build();
            ResourceLocation loc = makeID("testbiome");
            biome = Registry.register(BuiltinRegistries.BIOME, loc, biome);
            ResourceKey<Biome> key = BuiltinRegistries.BIOME.getResourceKey(biome).orElseThrow();
            NetherBiomeData.addNetherBiome(key, Climate.parameters(-1, 1, 0, 0, 0, 0, 0));
        }*/
    }

    public static boolean isDevEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public static ResourceLocation makeID(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
