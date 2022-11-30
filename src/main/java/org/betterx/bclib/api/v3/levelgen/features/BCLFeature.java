package org.betterx.bclib.api.v3.levelgen.features;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v3.levelgen.features.config.*;
import org.betterx.bclib.api.v3.levelgen.features.features.*;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import org.jetbrains.annotations.ApiStatus;

public class BCLFeature<F extends Feature<FC>, FC extends FeatureConfiguration> {
    public static final Feature<PlaceFacingBlockConfig> PLACE_BLOCK = register(
            BCLib.makeID("place_block"),
            new PlaceBlockFeature<>(PlaceFacingBlockConfig.CODEC)
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
    BCLFeature(
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
        return Registry.register(BuiltInRegistries.FEATURE, location, feature);
    }
}
