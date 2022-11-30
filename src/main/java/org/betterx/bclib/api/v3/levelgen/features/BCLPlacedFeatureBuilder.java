package org.betterx.bclib.api.v3.levelgen.features;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
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
    protected final BCLPlacedFeatureBuilder.Context ctx;

    private BCLPlacedFeatureBuilder(
            Context ctx,
            ResourceLocation featureID,
            BCLConfigureFeature<F, FC> cFeature
    ) {
        this.ctx = ctx;
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
            Context ctx,
            ResourceLocation featureID,
            Holder<ConfiguredFeature<FC, F>> holder
    ) {
        return place(ctx, featureID, BCLConfigureFeature.create(holder));
    }


    /**
     * Starts a new {@link BCLFeature} builder.
     *
     * @param featureID {@link ResourceLocation} feature identifier.
     * @param cFeature  {@link Feature} the configured Feature to start from.
     * @return {@link CommonPlacedFeatureBuilder} instance.
     */
    static <F extends Feature<FC>, FC extends FeatureConfiguration> BCLPlacedFeatureBuilder<F, FC> place(
            Context ctx,
            ResourceLocation featureID,
            BCLConfigureFeature<F, FC> cFeature
    ) {
        return new BCLPlacedFeatureBuilder(ctx, featureID, cFeature);
    }

    public static <F extends Feature<FC>, FC extends FeatureConfiguration> BCLPlacedFeatureBuilder<F, FC> place(
            BCLPlacedFeatureBuilder.Context ctx,
            ResourceLocation configuredFeature
    ) {
        ResourceKey<ConfiguredFeature<?, ?>> key = ResourceKey.create(
                Registries.CONFIGURED_FEATURE,
                configuredFeature
        );
        Holder<ConfiguredFeature<FC, F>> holder = (Holder<ConfiguredFeature<FC, F>>) (Object) ctx.bootstrapContext
                .lookup(Registries.CONFIGURED_FEATURE)
                .get(key);
        var cFeature = new BCLConfigureFeature<F, FC>(configuredFeature, holder, false);
        return new BCLPlacedFeatureBuilder<F, FC>(ctx, configuredFeature, cFeature);
    }


    /**
     * Builds a new {@link BCLFeature} instance.
     *
     * @return created {@link BCLFeature} instance.
     */
    public Holder<PlacedFeature> build() {
        final ResourceKey<PlacedFeature> key = ResourceKey.create(Registries.PLACED_FEATURE, featureID);
        PlacementUtils.register(
                ctx.bootstrapContext,
                key,
                (Holder<ConfiguredFeature<?, ?>>) (Object) cFeature.configuredFeature,
                modifications
        );
        return ctx.bootstrapContext.lookup(Registries.PLACED_FEATURE).get(key).orElseThrow();
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

    public record Context(BootstapContext<PlacedFeature> bootstrapContext) {
    }
}
