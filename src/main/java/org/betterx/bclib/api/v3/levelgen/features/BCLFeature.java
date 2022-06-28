package org.betterx.bclib.api.v3.levelgen.features;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.levelgen.features.config.*;
import org.betterx.bclib.api.v2.levelgen.features.features.*;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import org.jetbrains.annotations.ApiStatus;

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
    public final BCLConfigureFeature<F, FC> configuredFeature;
    public final Holder<PlacedFeature> placedFeature;
    public final GenerationStep.Decoration decoration;

    @ApiStatus.Internal
    @Deprecated(forRemoval = true)
    public BCLFeature(
            BCLConfigureFeature<F, FC> configuredFeature,
            Holder<PlacedFeature> placed,
            GenerationStep.Decoration decoration
    ) {
        this.configuredFeature = configuredFeature;
        this.placedFeature = placed;
        this.decoration = decoration;
    }

    /**
     * Get raw feature.
     *
     * @return {@link Feature}.
     */
    public F getFeature() {
        return configuredFeature.getFeature();
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
     * @return {@link GenerationStep.Decoration}.
     */
    public GenerationStep.Decoration getDecoration() {
        return decoration;
    }

    public FC getConfiguration() {
        return configuredFeature.getConfiguration();
    }


    public static <C extends FeatureConfiguration, F extends Feature<C>> F register(
            ResourceLocation location,
            F feature
    ) {
        return Registry.register(Registry.FEATURE, location, feature);
    }
}
