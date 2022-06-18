package org.betterx.bclib.api.v3.levelgen.features;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class BCLPlacedFeatureBuilder<F extends Feature<FC>, FC extends FeatureConfiguration> extends CommonPlacedFeatureBuilder<F, FC, BCLPlacedFeatureBuilder<F, FC>> {
    private final ResourceLocation featureID;
    private GenerationStep.Decoration decoration = GenerationStep.Decoration.VEGETAL_DECORATION;
    private final BCLConfigureFeature<F, FC> cFeature;

    private BCLPlacedFeatureBuilder(ResourceLocation featureID, BCLConfigureFeature<F, FC> cFeature) {
        this.featureID = featureID;
        this.cFeature = cFeature;
    }


    /**
     * Set generation step for the feature. Default is {@code VEGETAL_DECORATION}.
     *
     * @param decoration {@link GenerationStep.Decoration} step.
     * @return same {@link CommonPlacedFeatureBuilder} instance.
     */
    public BCLPlacedFeatureBuilder<F, FC> decoration(GenerationStep.Decoration decoration) {
        this.decoration = decoration;
        return this;
    }

    /**
     * Starts a new {@link BCLFeature} builder.
     *
     * @param featureID {@link ResourceLocation} feature identifier.
     * @param holder    {@link Feature} the configured Feature to start from.
     * @return {@link CommonPlacedFeatureBuilder} instance.
     */
    public static <F extends Feature<FC>, FC extends FeatureConfiguration> BCLPlacedFeatureBuilder<F, FC> place(
            ResourceLocation featureID,
            Holder<ConfiguredFeature<FC, F>> holder
    ) {
        return place(featureID, BCLConfigureFeature.create(holder));
    }


    /**
     * Starts a new {@link BCLFeature} builder.
     *
     * @param featureID {@link ResourceLocation} feature identifier.
     * @param cFeature  {@link Feature} the configured Feature to start from.
     * @return {@link CommonPlacedFeatureBuilder} instance.
     */
    static <F extends Feature<FC>, FC extends FeatureConfiguration> BCLPlacedFeatureBuilder<F, FC> place(
            ResourceLocation featureID,
            BCLConfigureFeature<F, FC> cFeature
    ) {
        return new BCLPlacedFeatureBuilder(featureID, cFeature);
    }

    /**
     * Builds a new {@link BCLFeature} instance.
     *
     * @return created {@link BCLFeature} instance.
     */
    public Holder<PlacedFeature> build() {
        Holder<PlacedFeature> p = PlacementUtils.register(
                featureID.toString(),
                cFeature.configuredFeature,
                modifications
        );
        return p;
    }


    /**
     * Builds a new {@link BCLFeature} instance.
     * Features will be registered during this process.
     *
     * @return created {@link BCLFeature} instance.
     */
    public BCLFeature<F, FC> buildAndRegister() {
        Holder<PlacedFeature> p = build();
        return new BCLFeature(cFeature, p, decoration);
    }
}
