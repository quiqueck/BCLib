package org.betterx.bclib.api.v3.levelgen.features;

import org.betterx.bclib.util.FullReferenceHolder;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BCLPlacedFeatureBuilder<F extends Feature<FC>, FC extends FeatureConfiguration> extends CommonPlacedFeatureBuilder<F, FC, BCLPlacedFeatureBuilder<F, FC>> {
    private final ResourceLocation featureID;
    private GenerationStep.Decoration decoration = GenerationStep.Decoration.VEGETAL_DECORATION;
    private final BCLConfigureFeature<F, FC> cFeature;

    static final ConcurrentLinkedQueue<BCLFeature.Unregistered> UNBOUND_FEATURES = new ConcurrentLinkedQueue<>();

    private BCLPlacedFeatureBuilder(
            ResourceLocation featureID,
            BCLConfigureFeature<F, FC> cFeature
    ) {
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
    public BCLFeature.Unregistered<F, FC> build() {
        final ResourceKey<PlacedFeature> key = ResourceKey.create(Registries.PLACED_FEATURE, featureID);
        PlacedFeature pFeature = new PlacedFeature(
                (Holder<ConfiguredFeature<?, ?>>) (Object) cFeature.configuredFeature,
                List.copyOf(modifications)
        );
        FullReferenceHolder<PlacedFeature> holder = FullReferenceHolder.create(
                Registries.PLACED_FEATURE,
                featureID,
                pFeature
        );
        final BCLFeature.Unregistered<F, FC> res = new BCLFeature.Unregistered<>(cFeature, holder, decoration);
        UNBOUND_FEATURES.add(res);
        return res;
    }

    public static void registerUnbound(BootstapContext<PlacedFeature> bootstrapContext) {
        UNBOUND_FEATURES.forEach(u -> u.register(bootstrapContext));
        UNBOUND_FEATURES.clear();
    }

    public static Holder<PlacedFeature> register(
            BootstapContext<PlacedFeature> bootstrapContext,
            Holder<PlacedFeature> holder
    ) {
        return bootstrapContext.register(holder.unwrapKey().orElseThrow(), holder.value());
    }


    /**
     * Builds a new {@link BCLFeature} instance.
     * Features will be registered during this process.
     *
     * @return created {@link BCLFeature} instance.
     */
    public BCLFeature<F, FC> buildAndRegister(BootstapContext<PlacedFeature> bootstapContext) {
        return build().register(bootstapContext);
    }
}
