package org.betterx.bclib.datagen;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v3.levelgen.features.BCLFeature;
import org.betterx.bclib.api.v3.levelgen.features.BCLPlacedFeatureBuilder;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.RandomPatchFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class TestPlacedFeatures {
    static BCLFeature<RandomPatchFeature, RandomPatchConfiguration> YELLOW_PLACED;

    public static void bootstrap(BootstapContext<PlacedFeature> bootstrapContext) {
        BCLib.LOGGER.info("Bootstrap PLACEDFeatures");

        if (BCLibDatagen.ADD_TESTS && BCLib.isDevEnvironment()) {
            final BCLPlacedFeatureBuilder.Context buildContext = new BCLPlacedFeatureBuilder.Context(bootstrapContext);
            YELLOW_PLACED = BCLPlacedFeatureBuilder.<RandomPatchFeature, RandomPatchConfiguration>place(
                                                           buildContext,
                                                           BCLib.makeID("yellow_feature")
                                                   )
                                                   .decoration(GenerationStep.Decoration.VEGETAL_DECORATION)
                                                   .vanillaNetherGround(8)
                                                   .isEmptyAndOnNetherGround()
                                                   .buildAndRegister();
        }
    }
}
