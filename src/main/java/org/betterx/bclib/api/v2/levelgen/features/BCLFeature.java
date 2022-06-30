package org.betterx.bclib.api.v2.levelgen.features;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.levelgen.features.config.ScatterFeatureConfig;
import org.betterx.bclib.api.v2.levelgen.features.features.ScatterFeature;
import org.betterx.bclib.api.v2.levelgen.features.features.WeightedRandomSelectorFeature;
import org.betterx.bclib.api.v3.levelgen.features.BCLConfigureFeature;
import org.betterx.bclib.api.v3.levelgen.features.BCLFeatureBuilder;
import org.betterx.bclib.api.v3.levelgen.features.UserGrowableFeature;
import org.betterx.bclib.api.v3.levelgen.features.config.ConditionFeatureConfig;
import org.betterx.bclib.api.v3.levelgen.features.config.PlaceFacingBlockConfig;
import org.betterx.bclib.api.v3.levelgen.features.config.SequenceFeatureConfig;
import org.betterx.bclib.api.v3.levelgen.features.config.TemplateFeatureConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * @param <F>
 * @param <FC>
 * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.BCLFeature} instead
 */
@Deprecated(forRemoval = true)
public class BCLFeature<F extends Feature<FC>, FC extends FeatureConfiguration> {
    /**
     * @deprecated Replace by {@link org.betterx.bclib.api.v3.levelgen.features.BCLFeature#PLACE_BLOCK}
     */
    @Deprecated(forRemoval = true)
    public static final Feature<PlaceFacingBlockConfig> PLACE_BLOCK = org.betterx.bclib.api.v3.levelgen.features.BCLFeature.PLACE_BLOCK;

    @Deprecated(forRemoval = true)
    public static final Feature<ScatterFeatureConfig.OnSolid> SCATTER_ON_SOLID = register(
            BCLib.makeID("scatter_on_solid"),
            new ScatterFeature<>(ScatterFeatureConfig.OnSolid.CODEC)
    );

    @Deprecated(forRemoval = true)
    public static final Feature<ScatterFeatureConfig.ExtendTop> SCATTER_EXTEND_TOP = register(
            BCLib.makeID("scatter_extend_top"),
            new ScatterFeature<>(ScatterFeatureConfig.ExtendTop.CODEC)
    );

    @Deprecated(forRemoval = true)
    public static final Feature<ScatterFeatureConfig.ExtendBottom> SCATTER_EXTEND_BOTTOM = register(
            BCLib.makeID("scatter_extend_bottom"),
            new ScatterFeature<>(ScatterFeatureConfig.ExtendBottom.CODEC)
    );

    @Deprecated(forRemoval = true)
    public static final Feature<RandomFeatureConfiguration> RANDOM_SELECTOR = register(
            BCLib.makeID("random_select"),
            new WeightedRandomSelectorFeature()
    );

    /**
     * @deprecated Replace by {@link org.betterx.bclib.api.v3.levelgen.features.BCLFeature#TEMPLATE}
     */
    @Deprecated(forRemoval = true)
    public static final Feature<TemplateFeatureConfig> TEMPLATE = org.betterx.bclib.api.v3.levelgen.features.BCLFeature.TEMPLATE;
    /**
     * @deprecated Replace by {@link org.betterx.bclib.api.v3.levelgen.features.BCLFeature#MARK_POSTPROCESSING}
     */
    @Deprecated(forRemoval = true)
    public static final Feature<NoneFeatureConfiguration> MARK_POSTPROCESSING = org.betterx.bclib.api.v3.levelgen.features.BCLFeature.MARK_POSTPROCESSING;
    /**
     * @deprecated Replace by {@link org.betterx.bclib.api.v3.levelgen.features.BCLFeature#SEQUENCE}
     */
    @Deprecated(forRemoval = true)
    public static final Feature<SequenceFeatureConfig> SEQUENCE = org.betterx.bclib.api.v3.levelgen.features.BCLFeature.SEQUENCE;
    /**
     * @deprecated Replace by {@link org.betterx.bclib.api.v3.levelgen.features.BCLFeature#CONDITION}
     */
    @Deprecated(forRemoval = true)
    public static final Feature<ConditionFeatureConfig> CONDITION = org.betterx.bclib.api.v3.levelgen.features.BCLFeature.CONDITION;

    public final ResourceLocation id;

    org.betterx.bclib.api.v3.levelgen.features.BCLFeature<F, FC> proxy;

    @Deprecated(forRemoval = true)
    public BCLFeature(
            ResourceLocation id,
            F feature,
            Decoration featureStep,
            FC configuration,
            PlacementModifier[] modifiers
    ) {
        this(id, feature, featureStep, configuration, buildPlacedFeature(id, feature, configuration, modifiers));
    }

    private static <E> boolean containsObj(Registry<E> registry, E obj) {
        Optional<Map.Entry<ResourceKey<E>, E>> optional = registry
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() == obj)
                .findAny();
        return optional.isPresent();
    }

    private static <F extends Feature<FC>, FC extends FeatureConfiguration> org.betterx.bclib.api.v3.levelgen.features.BCLFeature<F, FC> build(
            ResourceLocation id,
            F feature,
            Decoration featureStep,
            FC configuration,
            Holder<PlacedFeature> placedFeature
    ) {
        BCLConfigureFeature<F, FC> cfg = BCLFeatureBuilder.start(id, feature)
                                                          .configuration(configuration)
                                                          .build();
        if (!BuiltinRegistries.PLACED_FEATURE.containsKey(id)) {
            Registry.register(BuiltinRegistries.PLACED_FEATURE, id, placedFeature.value());
        }
        if (!Registry.FEATURE.containsKey(id) && !containsObj(Registry.FEATURE, feature)) {
            Registry.register(Registry.FEATURE, id, feature);
        }
        return new org.betterx.bclib.api.v3.levelgen.features.BCLFeature<>(cfg, placedFeature, featureStep);
    }

    @Deprecated(forRemoval = true)
    public BCLFeature(
            ResourceLocation id,
            F feature,
            Decoration featureStep,
            FC configuration,
            Holder<PlacedFeature> placedFeature
    ) {
        this(build(id, feature, featureStep, configuration, placedFeature));
    }

    @Deprecated(forRemoval = true)
    public BCLFeature(org.betterx.bclib.api.v3.levelgen.features.BCLFeature proxy) {
        this.proxy = proxy;
        this.id = proxy.configuredFeature.id;
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> Holder<PlacedFeature> buildPlacedFeature(
            ResourceLocation id,
            F feature,
            FC configuration,
            PlacementModifier[] modifiers
    ) {
        Holder<ConfiguredFeature<?, ?>> configuredFeature;
        if (!BuiltinRegistries.CONFIGURED_FEATURE.containsKey(id)) {
            configuredFeature = (Holder<ConfiguredFeature<?, ?>>) (Object) FeatureUtils.register(
                    id.toString(),
                    feature,
                    configuration
            );
        } else {
            configuredFeature = BuiltinRegistries.CONFIGURED_FEATURE
                    .getHolder(ResourceKey.create(
                            BuiltinRegistries.CONFIGURED_FEATURE.key(),
                            id
                    ))
                    .orElseThrow();
        }

        if (!BuiltinRegistries.PLACED_FEATURE.containsKey(id)) {
            return PlacementUtils.register(id.toString(), configuredFeature, modifiers);
        } else {
            return BuiltinRegistries.PLACED_FEATURE.getHolder(ResourceKey.create(
                    BuiltinRegistries.PLACED_FEATURE.key(),
                    id
            )).orElseThrow();
        }
    }

    /**
     * @param string
     * @param feature
     * @param <C>
     * @param <F>
     * @return
     * @deprecated Use {@link org.betterx.bclib.api.v3.levelgen.features.BCLFeature#register(ResourceLocation, Feature)} instead
     */
    @Deprecated(forRemoval = true)
    public static <C extends FeatureConfiguration, F extends Feature<C>> F register(
            ResourceLocation string,
            F feature
    ) {
        return org.betterx.bclib.api.v3.levelgen.features.BCLFeature.register(string, feature);
    }

    /**
     * Get raw feature.
     *
     * @return {@link Feature}.
     */
    public F getFeature() {
        return proxy.getFeature();
    }

    public BCLConfigureFeature<F, FC> getConfFeature() {
        return proxy.configuredFeature;
    }

    /**
     * Get configured feature.
     *
     * @return {@link PlacedFeature}.
     */
    public Holder<PlacedFeature> getPlacedFeature() {
        return proxy.getPlacedFeature();
    }

    /**
     * Get feature decoration step.
     *
     * @return {@link Decoration}.
     */
    public Decoration getDecoration() {
        return proxy.getDecoration();
    }

    public FC getConfiguration() {
        return proxy.getConfiguration();
    }

    public boolean place(ServerLevel level, BlockPos pos, Random random) {
        return place(this.getFeature(), this.getConfiguration(), level, pos, random);
    }

    private static boolean placeUnbound(
            Feature<?> feature,
            FeatureConfiguration config,
            ServerLevel level,
            BlockPos pos,
            Random random
    ) {
        if (config instanceof RandomPatchConfiguration rnd) {
            var configured = rnd.feature().value().feature().value();
            feature = configured.feature();
            config = configured.config();
        }

        if (feature instanceof UserGrowableFeature growable) {
            return growable.grow(level, pos, random, config);
        }

        FeaturePlaceContext context = new FeaturePlaceContext(
                Optional.empty(),
                level,
                level.getChunkSource().getGenerator(),
                random,
                pos,
                config
        );
        return feature.place(context);
    }

    public static boolean place(
            Feature<NoneFeatureConfiguration> feature,
            ServerLevel level,
            BlockPos pos,
            Random random
    ) {
        return placeUnbound(feature, FeatureConfiguration.NONE, level, pos, random);
    }

    public static <FC extends FeatureConfiguration> boolean place(
            Feature<FC> feature,
            FC config,
            ServerLevel level,
            BlockPos pos,
            Random random
    ) {
        return placeUnbound(feature, config, level, pos, random);
    }
}
