package org.betterx.bclib.api.v2.levelgen.features;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;

public class BCLCommonFeatures {
    /**
     * Will create a basic plant feature.
     *
     * @param id      {@link ResourceLocation} feature ID.
     * @param feature {@link Feature} with {@link NoneFeatureConfiguration} config.
     * @param density iterations per chunk.
     * @return new BCLFeature instance.
     */
    public static BCLFeature makeVegetationFeature(
            ResourceLocation id,
            Feature<NoneFeatureConfiguration> feature,
            int density
    ) {
        return makeVegetationFeature(id, feature, density, false);
    }

    /**
     * Will create a basic plant feature.
     *
     * @param id        {@link ResourceLocation} feature ID.
     * @param feature   {@link Feature} with {@link NoneFeatureConfiguration} config.
     * @param density   iterations per chunk.
     * @param allHeight if {@code true} will generate plant on all layers, if {@code false} - only on surface.
     * @return new BCLFeature instance.
     */
    public static BCLFeature makeVegetationFeature(
            ResourceLocation id,
            Feature<NoneFeatureConfiguration> feature,
            int density,
            boolean allHeight
    ) {
        if (allHeight) {
            return BCLFeatureBuilder
                    .start(id, feature)
                    .countLayers(density)
                    .squarePlacement()
                    .onlyInBiome()
                    .buildAndRegister();
        } else {
            return BCLFeatureBuilder
                    .start(id, feature)
                    .countMax(density)
                    .squarePlacement()
                    .heightmap()
                    .onlyInBiome()
                    .buildAndRegister();
        }
    }

    /**
     * Will create feature which will be generated once in each chunk.
     *
     * @param id         {@link ResourceLocation} feature ID.
     * @param decoration {@link Decoration} feature step.
     * @param feature    {@link Feature} with {@link NoneFeatureConfiguration} config.
     * @return new BCLFeature instance.
     */
    public static BCLFeature makeChunkFeature(
            ResourceLocation id,
            Decoration decoration,
            Feature<NoneFeatureConfiguration> feature
    ) {
        return BCLFeatureBuilder.start(id, feature).decoration(decoration).count(1).onlyInBiome().buildAndRegister();
    }

    /**
     * Will create feature with chanced decoration, chance for feature to generate per chunk is 1 / chance.
     *
     * @param id         {@link ResourceLocation} feature ID.
     * @param decoration {@link Decoration} feature step.
     * @param feature    {@link Feature} with {@link NoneFeatureConfiguration} config.
     * @param chance     chance for feature to be generated in.
     * @return new BCLFeature instance.
     */
    public static BCLFeature makeChancedFeature(
            ResourceLocation id,
            Decoration decoration,
            Feature<NoneFeatureConfiguration> feature,
            int chance
    ) {
        return BCLFeatureBuilder.start(id, feature)
                                .decoration(decoration)
                                .onceEvery(chance)
                                .squarePlacement()
                                .onlyInBiome()
                                .buildAndRegister();
    }

    /**
     * Will create feature with specified generation iterations per chunk.
     *
     * @param id         {@link ResourceLocation} feature ID.
     * @param decoration {@link Decoration} feature step.
     * @param feature    {@link Feature} with {@link NoneFeatureConfiguration} config.
     * @param count      iterations steps.
     * @return new BCLFeature instance.
     */
    public static BCLFeature makeCountFeature(
            ResourceLocation id,
            Decoration decoration,
            Feature<NoneFeatureConfiguration> feature,
            int count
    ) {
        return BCLFeatureBuilder.start(id, feature)
                                .decoration(decoration)
                                .count(count)
                                .squarePlacement()
                                .onlyInBiome()
                                .buildAndRegister();
    }

    /**
     * Will create a basic ore feature.
     *
     * @param id               {@link ResourceLocation} feature ID.
     * @param blockOre         {@link Decoration} feature step.
     * @param hostBlock        {@link Block} to generate feature in.
     * @param veins            iterations per chunk.
     * @param veinSize         size of ore vein.
     * @param airDiscardChance chance that this orge gets discarded when it is exposed to air
     * @param placement        {@link net.minecraft.world.level.levelgen.placement.PlacementModifier} for the ore distribution,
     *                         for example {@code PlacementUtils.FULL_RANGE}, {@code PlacementUtils.RANGE_10_10}
     * @param rare             when true, this is placed as a rare resource
     * @return new BCLFeature instance.
     */
    public static BCLFeature makeOreFeature(
            ResourceLocation id,
            Block blockOre,
            Block hostBlock,
            int veins,
            int veinSize,
            float airDiscardChance,
            PlacementModifier placement,
            boolean rare
    ) {
        BCLFeatureBuilder builder = BCLFeatureBuilder.start(id, Feature.ORE).decoration(Decoration.UNDERGROUND_ORES);

        if (rare) {
            builder.onceEvery(veins);
        } else {
            builder.count(veins);
        }

        builder.modifier(placement).squarePlacement().onlyInBiome();

        return builder.buildAndRegister(new OreConfiguration(
                new BlockMatchTest(hostBlock),
                blockOre.defaultBlockState(),
                veinSize,
                airDiscardChance
        ));
    }
}
