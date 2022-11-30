package org.betterx.bclib.api.v3.levelgen.features;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public class BCLInlinePlacedBuilder<F extends Feature<FC>, FC extends FeatureConfiguration> extends CommonPlacedFeatureBuilder<F, FC, BCLInlinePlacedBuilder<F, FC>> {
    private final BCLConfigureFeature<F, FC> cFeature;
    protected final BCLFeatureBuilder.Context ctx;

    private BCLInlinePlacedBuilder(BCLFeatureBuilder.Context ctx, BCLConfigureFeature<F, FC> cFeature) {
        this.ctx = ctx;
        this.cFeature = cFeature;
    }

    /**
     * Starts a new {@link BCLFeature} builder.
     *
     * @param holder {@link Feature} the configured Feature to start from.
     * @return {@link CommonPlacedFeatureBuilder} instance.
     */
    public static <F extends Feature<FC>, FC extends FeatureConfiguration> BCLInlinePlacedBuilder<F, FC> place(
            BCLFeatureBuilder.Context ctx,
            ResourceLocation featureID,
            Holder<ConfiguredFeature<FC, F>> holder
    ) {
        return place(ctx, BCLConfigureFeature.create(holder));
    }


    /**
     * Starts a new {@link BCLFeature} builder.
     *
     * @param cFeature {@link Feature} the configured Feature to start from.
     * @return {@link CommonPlacedFeatureBuilder} instance.
     */
    static <F extends Feature<FC>, FC extends FeatureConfiguration> BCLInlinePlacedBuilder<F, FC> place(
            BCLFeatureBuilder.Context ctx,
            BCLConfigureFeature<F, FC> cFeature
    ) {
        return new BCLInlinePlacedBuilder(ctx, cFeature);
    }

    /**
     * Builds a new inline (not registered) {@link PlacedFeature}.
     *
     * @return created {@link PlacedFeature} instance.
     */
    @Override
    public Holder<PlacedFeature> build() {
        return build(cFeature);
    }

    /**
     * Builds a new inline (not registered) {@link PlacedFeature}.
     *
     * @return created {@link PlacedFeature} instance.
     */
    public Holder<PlacedFeature> build(BCLConfigureFeature feature) {
        return build(feature.configuredFeature);
    }

    /**
     * Builds a new inline (not registered) {@link PlacedFeature}.
     *
     * @return created {@link PlacedFeature} instance.
     */
    public Holder<PlacedFeature> build(Holder<ConfiguredFeature<FC, F>> feature) {
        PlacementModifier[] modifiers = modifications.toArray(new PlacementModifier[modifications.size()]);
        return PlacementUtils.inlinePlaced((Holder<ConfiguredFeature<?, ?>>) (Object) feature, modifiers);
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


    public BCLFeatureBuilder.RandomPatch inRandomPatch(ResourceLocation id) {
        return ctx.startRandomPatch(id, build());
    }

    public BCLFeatureBuilder.RandomPatch randomBonemealDistribution(ResourceLocation id) {
        return inRandomPatch(id)
                .tries(9)
                .spreadXZ(3)
                .spreadY(1);
    }
}
