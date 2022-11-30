package org.betterx.bclib.datagen;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v3.levelgen.features.BCLConfigureFeature;
import org.betterx.bclib.api.v3.levelgen.features.BCLFeatureBuilder;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.RandomPatchFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;

public class TestConfiguredFeatures {
    static BCLConfigureFeature<RandomPatchFeature, RandomPatchConfiguration> YELLOW_FEATURE;

    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> bootstrapContext) {
        BCLib.LOGGER.info("Bootstrap CONFIGUREDFeatures");
        if (BCLibDatagen.ADD_TESTS && BCLib.isDevEnvironment()) {
            BCLFeatureBuilder.Context builder = new BCLFeatureBuilder.Context(bootstrapContext);

            YELLOW_FEATURE = builder
                    .startBonemealPatch(BCLib.makeID("yellow_feature"))
                    .add(Blocks.YELLOW_STAINED_GLASS, 30)
                    .add(Blocks.YELLOW_CONCRETE_POWDER, 30)
                    .add(Blocks.YELLOW_GLAZED_TERRACOTTA, 5)
                    .buildAndRegister();
        }
    }
}
