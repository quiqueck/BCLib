package org.betterx.datagen.bclib.tests;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiome;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeBuilder;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeContainer;
import org.betterx.bclib.api.v3.datagen.TagDataProvider;
import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.tag.v3.TagManager;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TestBiomes extends TagDataProvider<Biome> {
    static BCLBiomeContainer<BCLBiome> THE_YELLOW = BCLBiomeBuilder
            .start(BCLib.makeID("the_yellow"))
            .hasPrecipitation(false)
            .temperature(1.0f)
            .wetness(1.0f)
            .fogColor(0xFFFF00)
            .waterColor(0x777700)
            .waterFogColor(0xFFFF00)
            .skyColor(0xAAAA00)
            .feature(TestPlacedFeatures.YELLOW_PLACED)
            .addNetherClimateParamater(-1, 1)
            .surface(Blocks.YELLOW_CONCRETE)
            .endLandBiome()
            .build();

    static BCLBiomeContainer<BCLBiome> THE_BLUE = BCLBiomeBuilder
            .start(BCLib.makeID("the_blue"))
            .hasPrecipitation(false)
            .temperature(1.0f)
            .wetness(1.0f)
            .fogColor(0x0000FF)
            .waterColor(0x000077)
            .waterFogColor(0x0000FF)
            .skyColor(0x0000AA)
            .addNetherClimateParamater(-1, 1)
            .surface(Blocks.LIGHT_BLUE_CONCRETE)
            .structure(TestStructure.TEST.biomeTag)
            .endLandBiome()
            .build();

    static BCLBiomeContainer<BCLBiome> THE_GRAY = BCLBiomeBuilder
            .start(BCLib.makeID("the_gray"))
            .hasPrecipitation(false)
            .temperature(1.0f)
            .wetness(1.0f)
            .fogColor(0xFFFFFF)
            .waterColor(0x777777)
            .waterFogColor(0xFFFFFF)
            .skyColor(0xAAAAAA)
            .addNetherClimateParamater(-1, 1)
            .surface(Blocks.GRAY_CONCRETE)
            .endVoidBiome()
            .build();
    static BCLBiomeContainer<BCLBiome> THE_ORANGE = BCLBiomeBuilder
            .start(BCLib.makeID("the_orange"))
            .hasPrecipitation(false)
            .temperature(1.0f)
            .wetness(1.0f)
            .fogColor(0xFF7700)
            .waterColor(0x773300)
            .waterFogColor(0xFF7700)
            .skyColor(0xAA7700)
            .addNetherClimateParamater(-1, 1.1f)
            .surface(Blocks.ORANGE_CONCRETE)
            .netherBiome()
            .build();
    static BCLBiomeContainer<BCLBiome> THE_PURPLE = BCLBiomeBuilder
            .start(BCLib.makeID("the_purple"))
            .hasPrecipitation(false)
            .temperature(1.0f)
            .wetness(1.0f)
            .fogColor(0xFF00FF)
            .waterColor(0x770077)
            .waterFogColor(0xFF00FF)
            .skyColor(0xAA00AA)
            .addNetherClimateParamater(-1.1f, 1)
            .surface(Blocks.PURPLE_CONCRETE)
            .netherBiome()
            .build();

    /**
     * Constructs a new {@link FabricTagProvider} with the default computed path.
     *
     * <p>Common implementations of this class are provided.
     *
     * @param output           the {@link FabricDataOutput} instance
     * @param registriesFuture the backing registry for the tag type
     */
    public TestBiomes(
            FabricDataOutput output,
            CompletableFuture<HolderLookup.Provider> registriesFuture
    ) {
        super(TagManager.BIOMES, List.of(BCLib.MOD_ID, WorldsTogether.MOD_ID, "c"), output, registriesFuture);
    }

    public static void bootstrap(BootstapContext<Biome> bootstrapContext) {
        if (BCLib.ADD_TEST_DATA && BCLib.isDevEnvironment()) {
            BCLib.LOGGER.info("Bootstrap Biomes");

            THE_YELLOW = THE_YELLOW.register(bootstrapContext);
            THE_BLUE = THE_BLUE.register(bootstrapContext);
            THE_GRAY = THE_GRAY.register(bootstrapContext);
            THE_ORANGE = THE_ORANGE.register(bootstrapContext);
            THE_PURPLE = THE_PURPLE.register(bootstrapContext);
        }
    }

    public static void ensureStaticallyLoaded() {
    }
}
