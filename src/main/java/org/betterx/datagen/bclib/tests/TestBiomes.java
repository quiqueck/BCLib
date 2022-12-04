package org.betterx.datagen.bclib.tests;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiome;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeBuilder;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeContainer;
import org.betterx.datagen.bclib.BCLibDatagen;
import org.betterx.worlds.together.tag.v3.TagManager;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;

import java.util.concurrent.CompletableFuture;

public class TestBiomes extends FabricTagProvider<Biome> {
    static BCLBiomeContainer<BCLBiome> THE_YELLOW = BCLBiomeBuilder
            .start(BCLib.makeID("the_yellow"))
            .precipitation(Biome.Precipitation.NONE)
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
            .precipitation(Biome.Precipitation.NONE)
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
        super(output, Registries.BIOME, registriesFuture);
    }

    public static void bootstrap(BootstapContext<Biome> bootstrapContext) {
        BCLib.LOGGER.info("Bootstrap Biomes");
        if (BCLibDatagen.ADD_TESTS && BCLib.isDevEnvironment()) {
            THE_YELLOW = THE_YELLOW.register(bootstrapContext);
            THE_BLUE = THE_BLUE.register(bootstrapContext);

            BCLBiome theGray = BCLBiomeBuilder
                    .start(BCLib.makeID("the_gray"))
                    .precipitation(Biome.Precipitation.NONE)
                    .temperature(1.0f)
                    .wetness(1.0f)
                    .fogColor(0xFFFFFF)
                    .waterColor(0x777777)
                    .waterFogColor(0xFFFFFF)
                    .skyColor(0xAAAAAA)
                    .addNetherClimateParamater(-1, 1)
                    .surface(Blocks.GRAY_CONCRETE)
                    .endVoidBiome()
                    .build()
                    .register(bootstrapContext).biome();

            BCLBiome theOrange = BCLBiomeBuilder
                    .start(BCLib.makeID("the_orange"))
                    .precipitation(Biome.Precipitation.NONE)
                    .temperature(1.0f)
                    .wetness(1.0f)
                    .fogColor(0xFF7700)
                    .waterColor(0x773300)
                    .waterFogColor(0xFF7700)
                    .skyColor(0xAA7700)
                    .addNetherClimateParamater(-1, 1.1f)
                    .surface(Blocks.ORANGE_CONCRETE)
                    .netherBiome()
                    .build()
                    .register(bootstrapContext).biome();

            BCLBiome thePurple = BCLBiomeBuilder
                    .start(BCLib.makeID("the_purple"))
                    .precipitation(Biome.Precipitation.NONE)
                    .temperature(1.0f)
                    .wetness(1.0f)
                    .fogColor(0xFF00FF)
                    .waterColor(0x770077)
                    .waterFogColor(0xFF00FF)
                    .skyColor(0xAA00AA)
                    .addNetherClimateParamater(-1.1f, 1)
                    .surface(Blocks.PURPLE_CONCRETE)
                    .netherBiome()
                    .build()
                    .register(bootstrapContext).biome();
        }
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        TagManager.BIOMES.forEachTag((tag, locs, tags) -> {
            final FabricTagProvider<Biome>.FabricTagBuilder builder = getOrCreateTagBuilder(tag);
            boolean modTag = tag.location().getNamespace().equals(BCLib.MOD_ID);
            locs.stream().filter(l -> modTag || l.getNamespace().equals(BCLib.MOD_ID)).forEach(builder::add);
            tags.stream()
                .filter(t -> modTag || t.location().getNamespace().equals(BCLib.MOD_ID))
                .forEach(builder::addTag);
        });
    }
}
