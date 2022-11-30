package org.betterx.bclib.datagen;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiome;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeBuilder;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeContainer;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;

public class TestBiomes {
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

    public static void bootstrap(BootstapContext<Biome> bootstrapContext) {
        BCLib.LOGGER.info("Bootstrap Biomes");
        if (BCLibDatagen.ADD_TESTS && BCLib.isDevEnvironment()) {
            BCLBiomeContainer<BCLBiome> theYellow = THE_YELLOW
                    .register(bootstrapContext);

            BCLBiome theBlue = BCLBiomeBuilder
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
                    .endLandBiome()
                    .build()
                    .register(bootstrapContext).biome();

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
}
