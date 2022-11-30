package org.betterx.bclib.datagen;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v3.levelgen.features.BCLFeature;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.RandomPatchFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class TestPlacedFeatures {
    static BCLFeature<RandomPatchFeature, RandomPatchConfiguration> YELLOW_PLACED = TestConfiguredFeatures
            .YELLOW_FEATURE
            .place()
            .decoration(GenerationStep.Decoration.VEGETAL_DECORATION)
            .vanillaNetherGround(8)
            .isEmptyAndOnNetherGround()
            .build();

    public static void bootstrap(BootstapContext<PlacedFeature> bootstrapContext) {
        BCLib.LOGGER.info("Bootstrap PLACEDFeatures");

        if (BCLibDatagen.ADD_TESTS && BCLib.isDevEnvironment()) {
            YELLOW_PLACED = YELLOW_PLACED.register(bootstrapContext);
        }
    }
}
