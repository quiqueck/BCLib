package org.betterx.bclib.api.v3.levelgen.features;

import org.betterx.bclib.api.v2.levelgen.features.BCLFeature;
import org.betterx.bclib.api.v2.poi.BCLPoiType;

import net.minecraft.core.Holder;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiFunction;
import org.jetbrains.annotations.NotNull;

public abstract class BCLFeatureBuilder<F extends Feature<FC>, FC extends FeatureConfiguration> {
    public static class NetherForrestVegetation<FF extends Feature<NetherForestVegetationConfig>> extends BCLFeatureBuilder<FF, NetherForestVegetationConfig> {
        private SimpleWeightedRandomList.Builder<BlockState> blocks;
        private WeightedStateProvider stateProvider;
        private int spreadWidth = 8;
        private int spreadHeight = 4;

        private NetherForrestVegetation(ResourceLocation featureID, FF feature) {
            super(featureID, feature);
        }

        public NetherForrestVegetation spreadWidth(int v) {
            spreadWidth = v;
            return this;
        }

        public NetherForrestVegetation spreadHeight(int v) {
            spreadHeight = v;
            return this;
        }

        public NetherForrestVegetation addAllStates(Block block, int weight) {
            Set<BlockState> states = BCLPoiType.getBlockStates(block);
            states.forEach(s -> add(block.defaultBlockState(), Math.max(1, weight / states.size())));
            return this;
        }

        public NetherForrestVegetation addAllStatesFor(IntegerProperty prop, Block block, int weight) {
            Collection<Integer> values = prop.getPossibleValues();
            values.forEach(s -> add(block.defaultBlockState().setValue(prop, s), Math.max(1, weight / values.size())));
            return this;
        }

        public NetherForrestVegetation add(Block block, int weight) {
            return add(block.defaultBlockState(), weight);
        }

        public NetherForrestVegetation add(BlockState state, int weight) {
            if (stateProvider != null) {
                throw new IllegalStateException("You can not add new state once a WeightedStateProvider was built. (" + state + ", " + weight + ")");
            }
            if (blocks == null) {
                blocks = SimpleWeightedRandomList.<BlockState>builder();
            }
            blocks.add(state, weight);
            return this;
        }

        public NetherForrestVegetation provider(WeightedStateProvider provider) {
            if (blocks != null) {
                throw new IllegalStateException(
                        "You can not set a WeightedStateProvider after states were added manually.");
            }
            stateProvider = provider;
            return this;
        }

        @Override
        public NetherForestVegetationConfig createConfiguration() {
            if (stateProvider == null && blocks == null) {
                throw new IllegalStateException("NetherForestVegetationConfig needs at least one BlockState");
            }
            if (stateProvider == null) stateProvider = new WeightedStateProvider(blocks.build());
            return new NetherForestVegetationConfig(stateProvider, spreadWidth, spreadHeight);
        }
    }

    public static class RandomPatch<FF extends Feature<RandomPatchConfiguration>> extends BCLFeatureBuilder<FF, RandomPatchConfiguration> {
        private int tries = 96;
        private int xzSpread = 7;
        private int ySpread = 3;
        private final Holder<PlacedFeature> featureToPlace;

        private RandomPatch(
                @NotNull ResourceLocation featureID,
                @NotNull FF feature,
                @NotNull Holder<PlacedFeature> featureToPlace
        ) {
            super(featureID, feature);
            this.featureToPlace = featureToPlace;
        }

        public RandomPatch tries(int v) {
            tries = v;
            return this;
        }

        public RandomPatch spreadXZ(int v) {
            xzSpread = v;
            return this;
        }

        public RandomPatch spreadY(int v) {
            ySpread = v;
            return this;
        }


        @Override
        public RandomPatchConfiguration createConfiguration() {
            return new RandomPatchConfiguration(tries, xzSpread, ySpread, featureToPlace);
        }
    }

    public static class WithConfiguration<F extends Feature<FC>, FC extends FeatureConfiguration> extends BCLFeatureBuilder<F, FC> {
        private FC configuration;

        private WithConfiguration(@NotNull ResourceLocation featureID, @NotNull F feature) {
            super(featureID, feature);
        }

        public WithConfiguration configuration(FC config) {
            this.configuration = config;
            return this;
        }


        @Override
        public FC createConfiguration() {
            if (configuration == null) return (FC) NoneFeatureConfiguration.NONE;
            return configuration;
        }
    }

    public static class ForSimpleBlock<FF extends Feature<SimpleBlockConfiguration>> extends BCLFeatureBuilder<FF, SimpleBlockConfiguration> {
        private final BlockStateProvider provider;

        private ForSimpleBlock(
                @NotNull ResourceLocation featureID,
                @NotNull FF feature,
                @NotNull BlockStateProvider provider
        ) {
            super(featureID, feature);
            this.provider = provider;
        }

        @Override
        public SimpleBlockConfiguration createConfiguration() {
            return new SimpleBlockConfiguration(provider);
        }
    }

    private final ResourceLocation featureID;
    private final F feature;

    private BCLFeatureBuilder(ResourceLocation featureID, F feature) {
        this.featureID = featureID;
        this.feature = feature;
    }

    /**
     * Starts a new {@link BCLFeature} builder.
     *
     * @param featureID {@link ResourceLocation} feature identifier.
     * @param feature   {@link Feature} to construct.
     * @return {@link org.betterx.bclib.api.v2.levelgen.features.BCLFeatureBuilder} instance.
     */
    public static <F extends Feature<FC>, FC extends FeatureConfiguration> WithConfiguration<F, FC> start(
            ResourceLocation featureID,
            F feature
    ) {
        return new WithConfiguration(featureID, feature);
    }

    public static ForSimpleBlock start(
            ResourceLocation featureID,
            Block block
    ) {
        return start(featureID, BlockStateProvider.simple(block));
    }

    public static ForSimpleBlock start(
            ResourceLocation featureID,
            BlockState state
    ) {
        return start(featureID, BlockStateProvider.simple(state));
    }

    public static ForSimpleBlock start(
            ResourceLocation featureID,
            BlockStateProvider provider
    ) {
        ForSimpleBlock builder = new ForSimpleBlock(
                featureID,
                Feature.SIMPLE_BLOCK,
                provider
        );
        return builder;
    }

    public static RandomPatch startRandomPatch(
            ResourceLocation featureID,
            Holder<PlacedFeature> featureToPlace
    ) {
        RandomPatch builder = new RandomPatch(
                featureID,
                Feature.RANDOM_PATCH,
                featureToPlace
        );
        return builder;
    }

    public static NetherForrestVegetation startNetherVegetation(
            ResourceLocation featureID
    ) {
        NetherForrestVegetation builder = new NetherForrestVegetation(
                featureID,
                Feature.NETHER_FOREST_VEGETATION
        );
        return builder;
    }

    public abstract FC createConfiguration();

    protected BCLConfigureFeature<F, FC> buildAndRegister(BiFunction<ResourceLocation, ConfiguredFeature<FC, F>, Holder<ConfiguredFeature<FC, F>>> holderBuilder) {
        FC config = createConfiguration();
        if (config == null) {
            throw new IllegalStateException("Feature configuration for " + featureID + " can not be null!");
        }
        ConfiguredFeature<FC, F> cFeature = new ConfiguredFeature<FC, F>(feature, config);
        Holder<ConfiguredFeature<FC, F>> holder = holderBuilder.apply(featureID, cFeature);
        return new BCLConfigureFeature<>(featureID, holder, true);
    }

    public BCLConfigureFeature<F, FC> buildAndRegister() {
        return buildAndRegister(BCLFeatureBuilder::register);
    }

    public BCLConfigureFeature<F, FC> build() {
        return buildAndRegister((id, cFeature) -> Holder.direct(cFeature));
    }

    public BCLInlinePlacedBuilder<F, FC> inlinePlace() {
        BCLConfigureFeature<F, FC> f = build();
        return BCLInlinePlacedBuilder.place(f);
    }

    public Holder<PlacedFeature> inlinePlace(BCLInlinePlacedBuilder<F, FC> placer) {
        BCLConfigureFeature<F, FC> f = build();
        return placer.build(f);
    }

    /**
     * Internally used by the builder. Normally you should not have to call this method directly as it is
     * handled by {@link #buildAndRegister()}
     *
     * @param id       The ID to register this feature with
     * @param cFeature The configured Feature
     * @param <F>      The Feature Class
     * @param <FC>     The FeatureConfiguration Class
     * @return The Holder for the new Feature
     */
    public static <F extends Feature<FC>, FC extends FeatureConfiguration> Holder<ConfiguredFeature<FC, F>> register(
            ResourceLocation id,
            ConfiguredFeature<FC, F> cFeature
    ) {
        return (Holder<ConfiguredFeature<FC, F>>) (Object) BuiltinRegistries.register(
                BuiltinRegistries.CONFIGURED_FEATURE,
                id,
                cFeature
        );
    }
}


