package org.betterx.bclib.api.v2.levelgen.features;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.levelgen.features.config.*;
import org.betterx.bclib.api.v2.levelgen.features.features.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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

import java.util.Map.Entry;
import java.util.Optional;

/**
 * @param <F>
 * @param <FC>
 * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.BCLFeature} instead
 */
@Deprecated(forRemoval = true)
public class BCLFeature<F extends Feature<FC>, FC extends FeatureConfiguration> {
    public static final Feature<PlaceFacingBlockConfig> PLACE_BLOCK = register(
            BCLib.makeID("place_block"),
            new PlaceBlockFeature<>(PlaceFacingBlockConfig.CODEC)
    );
    public static final Feature<ScatterFeatureConfig.OnSolid> SCATTER_ON_SOLID = register(
            BCLib.makeID("scatter_on_solid"),
            new ScatterFeature<>(ScatterFeatureConfig.OnSolid.CODEC)
    );

    public static final Feature<ScatterFeatureConfig.ExtendTop> SCATTER_EXTEND_TOP = register(
            BCLib.makeID("scatter_extend_top"),
            new ScatterFeature<>(ScatterFeatureConfig.ExtendTop.CODEC)
    );

    public static final Feature<ScatterFeatureConfig.ExtendBottom> SCATTER_EXTEND_BOTTOM = register(
            BCLib.makeID("scatter_extend_bottom"),
            new ScatterFeature<>(ScatterFeatureConfig.ExtendBottom.CODEC)
    );

    public static final Feature<RandomFeatureConfiguration> RANDOM_SELECTOR = register(
            BCLib.makeID("random_select"),
            new WeightedRandomSelectorFeature()
    );
    public static final Feature<TemplateFeatureConfig> TEMPLATE = register(
            BCLib.makeID("template"),
            new TemplateFeature(
                    TemplateFeatureConfig.CODEC)
    );

    public static final Feature<NoneFeatureConfiguration> MARK_POSTPROCESSING = register(
            BCLib.makeID(
                    "mark_postprocessing"),
            new MarkPostProcessingFeature()
    );

    public static final Feature<SequenceFeatureConfig> SEQUENCE = register(
            BCLib.makeID("sequence"),
            new SequenceFeature()
    );

    public static final Feature<ConditionFeatureConfig> CONDITION = register(
            BCLib.makeID("condition"),
            new ConditionFeature()
    );

    public static final Feature<PillarFeatureConfig> PILLAR = register(
            BCLib.makeID("pillar"),
            new PillarFeature()
    );
    private final Holder<PlacedFeature> placedFeature;
    private final Decoration featureStep;
    private final F feature;
    private final FC configuration;
    public final ResourceLocation id;


    public BCLFeature(
            ResourceLocation id,
            F feature,
            Decoration featureStep,
            FC configuration,
            PlacementModifier[] modifiers
    ) {
        this(id, feature, featureStep, configuration, buildPlacedFeature(id, feature, configuration, modifiers));
    }

    public BCLFeature(
            ResourceLocation id,
            F feature,
            Decoration featureStep,
            FC configuration,
            Holder<PlacedFeature> placedFeature
    ) {
        this.placedFeature = placedFeature;
        this.featureStep = featureStep;
        this.feature = feature;
        this.configuration = configuration;
        this.id = id;

        if (!BuiltinRegistries.PLACED_FEATURE.containsKey(id)) {
            Registry.register(BuiltinRegistries.PLACED_FEATURE, id, placedFeature.value());
        }
        if (!Registry.FEATURE.containsKey(id) && !containsObj(Registry.FEATURE, feature)) {
            Registry.register(Registry.FEATURE, id, feature);
        }
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

    private static <E> boolean containsObj(Registry<E> registry, E obj) {
        Optional<Entry<ResourceKey<E>, E>> optional = registry
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() == obj)
                .findAny();
        return optional.isPresent();
    }

    public static <C extends FeatureConfiguration, F extends Feature<C>> F register(
            ResourceLocation string,
            F feature
    ) {
        return Registry.register(Registry.FEATURE, string, feature);
    }

    /**
     * Get raw feature.
     *
     * @return {@link Feature}.
     */
    public F getFeature() {
        return feature;
    }

    /**
     * Get configured feature.
     *
     * @return {@link PlacedFeature}.
     */
    public Holder<PlacedFeature> getPlacedFeature() {
        return placedFeature;
    }

    /**
     * Get feature decoration step.
     *
     * @return {@link Decoration}.
     */
    public Decoration getDecoration() {
        return featureStep;
    }

    public FC getConfiguration() {
        return configuration;
    }

    public boolean place(ServerLevel level, BlockPos pos, RandomSource random) {
        return place(this.getFeature(), this.getConfiguration(), level, pos, random);
    }

    private static boolean placeUnbound(
            Feature<?> feature,
            FeatureConfiguration config,
            ServerLevel level,
            BlockPos pos,
            RandomSource random
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
            RandomSource random
    ) {
        return placeUnbound(feature, FeatureConfiguration.NONE, level, pos, random);
    }

    public static <FC extends FeatureConfiguration> boolean place(
            Feature<FC> feature,
            FC config,
            ServerLevel level,
            BlockPos pos,
            RandomSource random
    ) {
        return placeUnbound(feature, config, level, pos, random);
    }
}
