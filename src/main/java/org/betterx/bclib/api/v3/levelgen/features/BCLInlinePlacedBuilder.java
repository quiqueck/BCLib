package org.betterx.bclib.api.v3.levelgen.features;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public class BCLInlinePlacedBuilder<F extends Feature<FC>, FC extends FeatureConfiguration> extends CommonPlacedFeatureBuilder<F, FC, BCLInlinePlacedBuilder<F, FC>> {
    private final BCLConfigureFeature<F, FC> cFeature;

    private BCLInlinePlacedBuilder(BCLConfigureFeature<F, FC> cFeature) {
        this.cFeature = cFeature;
    }

    /**
     * Starts a new {@link BCLFeature} builder.
     *
     * @param holder {@link Feature} the configured Feature to start from.
     * @return {@link CommonPlacedFeatureBuilder} instance.
     */
    public static <F extends Feature<FC>, FC extends FeatureConfiguration> BCLInlinePlacedBuilder<F, FC> place(
            ResourceLocation featureID,
            Holder<ConfiguredFeature<FC, F>> holder
    ) {
        return place(BCLConfigureFeature.create(holder));
    }


    /**
     * Starts a new {@link BCLFeature} builder.
     *
     * @param cFeature {@link Feature} the configured Feature to start from.
     * @return {@link CommonPlacedFeatureBuilder} instance.
     */
    static <F extends Feature<FC>, FC extends FeatureConfiguration> BCLInlinePlacedBuilder<F, FC> place(
            BCLConfigureFeature<F, FC> cFeature
    ) {
        return new BCLInlinePlacedBuilder(cFeature);
    }

    /**
     * Builds a new inline (not registered) {@link PlacedFeature}.
     *
     * @return created {@link PlacedFeature} instance.
     */
    @Override
    public BCLFeature.Unregistered<F, FC> build() {
        return build(cFeature);
    }

    /**
     * Builds a new inline (not registered) {@link PlacedFeature}.
     *
     * @return created {@link PlacedFeature} instance.
     */
    public BCLFeature.Unregistered<F, FC> build(BCLConfigureFeature feature) {
        PlacementModifier[] modifiers = modifications.toArray(new PlacementModifier[modifications.size()]);
        Holder<PlacedFeature> holder = PlacementUtils.inlinePlaced(
                feature.configuredFeature,
                modifiers
        );
        return new BCLFeature.Unregistered<>(feature, holder, GenerationStep.Decoration.VEGETAL_DECORATION);
    }

    /**
     * Builds a new inline (not registered) {@link PlacedFeature}.
     *
     * @return created {@link PlacedFeature} instance.
     */
    public Holder<PlacedFeature> build(F feature, FC configuration) {
        PlacementModifier[] modifiers = modifications.toArray(new PlacementModifier[modifications.size()]);
        return PlacementUtils.inlinePlaced(feature, configuration, modifiers);
    }


}
