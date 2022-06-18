package org.betterx.bclib.api.v3.levelgen.features;

import org.betterx.bclib.api.v2.levelgen.features.config.*;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class BCLFeature<F extends Feature<FC>, FC extends FeatureConfiguration> {
    public static final Feature<PlaceFacingBlockConfig> PLACE_BLOCK = org.betterx.bclib.api.v2.levelgen.features.BCLFeature.PLACE_BLOCK;
    public static final Feature<ScatterFeatureConfig.OnSolid> SCATTER_ON_SOLID = org.betterx.bclib.api.v2.levelgen.features.BCLFeature.SCATTER_ON_SOLID;
    public static final Feature<ScatterFeatureConfig.ExtendTop> SCATTER_EXTEND_TOP = org.betterx.bclib.api.v2.levelgen.features.BCLFeature.SCATTER_EXTEND_TOP;
    public static final Feature<ScatterFeatureConfig.ExtendBottom> SCATTER_EXTEND_BOTTOM = org.betterx.bclib.api.v2.levelgen.features.BCLFeature.SCATTER_EXTEND_BOTTOM;
    public static final Feature<RandomFeatureConfiguration> RANDOM_SELECTOR = org.betterx.bclib.api.v2.levelgen.features.BCLFeature.RANDOM_SELECTOR;
    public static final Feature<TemplateFeatureConfig> TEMPLATE = org.betterx.bclib.api.v2.levelgen.features.BCLFeature.TEMPLATE;
    public static final Feature<NoneFeatureConfiguration> MARK_POSTPROCESSING = org.betterx.bclib.api.v2.levelgen.features.BCLFeature.MARK_POSTPROCESSING;
    public static final Feature<SequenceFeatureConfig> SEQUENCE = org.betterx.bclib.api.v2.levelgen.features.BCLFeature.SEQUENCE;
    public static final Feature<ConditionFeatureConfig> CONDITION = org.betterx.bclib.api.v2.levelgen.features.BCLFeature.CONDITION;
    public final BCLConfigureFeature<F, FC> configuredFeature;
    public final Holder<PlacedFeature> placedFeature;
    public final GenerationStep.Decoration decoration;

    BCLFeature(
            BCLConfigureFeature<F, FC> configuredFeature,
            Holder<PlacedFeature> placed,
            GenerationStep.Decoration decoration
    ) {
        this.configuredFeature = configuredFeature;
        this.placedFeature = placed;
        this.decoration = decoration;
    }

    public Holder<PlacedFeature> getPlacedFeature() {
        return placedFeature;
    }

    public GenerationStep.Decoration getDecoration() {
        return decoration;
    }
}
