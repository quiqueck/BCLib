package org.betterx.bclib.api.v2.levelgen.features;

import org.betterx.bclib.api.v2.levelgen.features.config.PlaceFacingBlockConfig;
import org.betterx.bclib.api.v2.levelgen.features.config.ScatterFeatureConfig;
import org.betterx.bclib.api.v2.levelgen.features.features.ScatterFeature;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RandomizedIntStateProvider;

public class FastFeatures {


    public static BCLFeature<ScatterFeature<ScatterFeatureConfig.OnSolid>, ScatterFeatureConfig.OnSolid> vine(
            ResourceLocation location,
            boolean onFloor,
            boolean sparse,
            ScatterFeatureConfig.Builder builder
    ) {
        return scatter(location, onFloor, sparse, builder, BCLFeature.SCATTER_ON_SOLID);
    }

    public static BCLFeature scatter(
            ResourceLocation location,
            boolean onFloor,
            boolean sparse,
            ScatterFeatureConfig.Builder builder,
            Feature scatterFeature
    ) {
        BCLFeatureBuilder fBuilder = BCLFeatureBuilder.start(location, scatterFeature);
        if (onFloor) {
            fBuilder.findSolidFloor(3).isEmptyAbove2();
            builder.onFloor();
        } else {
            fBuilder.findSolidCeil(3).isEmptyBelow2();
            builder.onCeil();
        }
        if (sparse) {
            fBuilder.onceEvery(3);
        }

        return fBuilder
                .is(BlockPredicate.ONLY_IN_AIR_PREDICATE)
                .buildAndRegister(builder.build());
    }

    public static BCLFeature patch(ResourceLocation location, Block block) {
        return patch(location, block, 96, 7, 3);
    }

    public static BCLFeature
    patch(ResourceLocation location, Block block, int attempts, int xzSpread, int ySpread) {
        return patch(
                location,
                attempts,
                xzSpread,
                ySpread,
                Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(BlockStateProvider.simple(block))
        );
    }

    public static BCLFeature
    patch(ResourceLocation location, BlockStateProvider provider, int attempts, int xzSpread, int ySpread) {
        return patch(
                location,
                attempts,
                xzSpread,
                ySpread,
                Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(provider)
        );
    }

    public static BCLFeature patchWitRandomInt(ResourceLocation location, Block block, IntegerProperty prop) {
        return patchWitRandomInt(location, block, prop, 96, 7, 3);
    }

    public static BCLFeature
    patchWitRandomInt(
            ResourceLocation location,
            Block block,
            IntegerProperty prop,
            int attempts,
            int xzSpread,
            int ySpread
    ) {
        return patch(
                location,
                attempts,
                xzSpread,
                ySpread,
                simple(location, ySpread, false, block.defaultBlockState(), prop)
        );
    }

    public static BCLFeature
    simple(
            ResourceLocation location,
            int searchDist,
            boolean rare,
            Feature<NoneFeatureConfiguration> feature
    ) {
        return simple(location, searchDist, rare, feature, NoneFeatureConfiguration.NONE);
    }

    public static BCLFeature
    single(ResourceLocation location, Block block) {
        return single(location, BlockStateProvider.simple(block));

    }

    public static BCLFeature
    single(ResourceLocation location, BlockStateProvider provider) {
        return BCLFeatureBuilder
                .start(location, provider)
                .buildAndRegister();
    }

    public static BCLFeature
    simple(ResourceLocation location, Feature<NoneFeatureConfiguration> feature) {
        return BCLFeatureBuilder
                .start(location, feature)
                .buildAndRegister();
    }

    public static BCLFeature
    simple(
            ResourceLocation location,
            int searchDist,
            boolean rare,
            BlockState baseState,
            IntegerProperty property
    ) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (Integer i : property.getPossibleValues()) {
            if (i < min) min = i;
            if (i > max) max = i;
        }

        return simple(
                location,
                searchDist,
                rare,
                Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(new RandomizedIntStateProvider(
                        BlockStateProvider.simple(baseState),
                        property,
                        UniformInt.of(min, max)
                ))
        );
    }


    public static <FC extends FeatureConfiguration> BCLFeature<Feature<FC>, FC>
    simple(
            ResourceLocation location,
            int searchDist,
            boolean rare,
            Feature<FC> feature,
            FC config
    ) {
        BCLFeatureBuilder builder = BCLFeatureBuilder
                .start(location, feature)
                .findSolidFloor(Math.min(12, searchDist))
                .is(BlockPredicate.ONLY_IN_AIR_PREDICATE);
        if (rare) {
            builder.onceEvery(4);
        }
        return builder.buildAndRegister(config);
    }

    public static BCLFeature
    patch(ResourceLocation location, Feature<NoneFeatureConfiguration> feature) {
        return patch(location, 96, 7, 3, feature, FeatureConfiguration.NONE);
    }


    public static BCLFeature
    patch(
            ResourceLocation location,
            int attempts,
            int xzSpread,
            int ySpread,
            Feature<NoneFeatureConfiguration> feature
    ) {
        return patch(location, attempts, xzSpread, ySpread, feature, FeatureConfiguration.NONE);
    }


    public static <FC extends FeatureConfiguration> BCLFeature
    patch(
            ResourceLocation location,
            int attempts,
            int xzSpread,
            int ySpread,
            Feature<FC> feature,
            FC config
    ) {
        final BCLFeature SINGLE = simple(location, ySpread, false, feature, config);
        return patch(location, attempts, xzSpread, ySpread, SINGLE);
    }

    public static BCLFeature
    wallPatch(
            ResourceLocation location,
            Block block,
            int attempts,
            int xzSpread,
            int ySpread
    ) {
        final BCLFeature SINGLE = simple(location, ySpread, false, BCLFeature.PLACE_BLOCK,
                new PlaceFacingBlockConfig(block, PlaceFacingBlockConfig.HORIZONTAL)
        );
        return patch(location, attempts, xzSpread, ySpread, SINGLE);
    }

    public static BCLFeature
    patch(
            ResourceLocation location,
            int attempts,
            int xzSpread,
            int ySpread,
            BCLFeature single
    ) {
        ResourceLocation patchLocation = new ResourceLocation(location.getNamespace(), location.getPath() + "_patch");

        return BCLFeatureBuilder
                .start(patchLocation, Feature.RANDOM_PATCH)
                .buildAndRegister(new RandomPatchConfiguration(attempts, xzSpread, ySpread, single.getPlacedFeature()));
    }


}
