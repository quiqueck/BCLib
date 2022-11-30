package org.betterx.bclib.datagen;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiome;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class TestBiomes {

    public static void bootstrap(BootstapContext<Biome> bootstrapContext) {
        BCLib.LOGGER.info("Bootstrap Biomes");
        if (BCLibDatagen.ADD_TESTS && BCLib.isDevEnvironment()) {
            HolderGetter<PlacedFeature> holderGetter = bootstrapContext.lookup(Registries.PLACED_FEATURE);
            Holder.Reference<PlacedFeature> reference = holderGetter.getOrThrow(ResourceKey.create(
                    Registries.PLACED_FEATURE,
                    BCLib.makeID("yellow_feature")
            ));
            BCLib.LOGGER.info("REF Biome:" + reference);

            BCLBiomeBuilder.Context biomeBuilder = new BCLBiomeBuilder.Context(bootstrapContext);
            BCLBiome theYellow = biomeBuilder
                    .start(BCLib.makeID("the_yellow"))
                    .precipitation(Biome.Precipitation.NONE)
                    .temperature(1.0f)
                    .wetness(1.0f)
                    .fogColor(0xFFFF00)
                    .waterColor(0x777700)
                    .waterFogColor(0xFFFF00)
                    .skyColor(0xAAAA00)
                    .feature(GenerationStep.Decoration.VEGETAL_DECORATION, reference)
                    .addNetherClimateParamater(-1, 1)
                    .surface(Blocks.YELLOW_CONCRETE)
                    .build()
                    .registerEndLandBiome();

            BCLBiome theBlue = biomeBuilder
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
                    .build()
                    .registerEndLandBiome();

            BCLBiome theGray = biomeBuilder
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
                    .build()
                    .registerEndVoidBiome();

            BCLBiome theOrange = biomeBuilder
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
                    .build()
                    .registerNetherBiome();

            BCLBiome thePurple = biomeBuilder
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
                    .build()
                    .registerNetherBiome();
        }
    }
}
