package org.betterx.bclib.api.v3.levelgen.features;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.HashMap;
import java.util.Map;

public class BCLConfigureFeature<F extends Feature<FC>, FC extends FeatureConfiguration> {
    private static final Map<Holder<ConfiguredFeature<?, ?>>, BCLConfigureFeature<?, ?>> KNOWN = new HashMap<>();

    public final ResourceLocation id;
    public final Holder<ConfiguredFeature<FC, F>> configuredFeature;
    public final boolean registered;

    BCLConfigureFeature(ResourceLocation id, Holder<ConfiguredFeature<FC, F>> configuredFeature, boolean registered) {
        this.id = id;
        this.configuredFeature = configuredFeature;
        this.registered = registered;
    }

    public F getFeature() {
        return configuredFeature.value().feature();
    }

    public FC getConfiguration() {
        return configuredFeature.value().config();
    }


    public BCLPlacedFeatureBuilder<F, FC> place() {
        return place(this.id);
    }

    public BCLPlacedFeatureBuilder<F, FC> place(ResourceLocation id) {
        return BCLPlacedFeatureBuilder.place(id, this);
    }

    static <F extends Feature<FC>, FC extends FeatureConfiguration> BCLConfigureFeature<F, FC> create(Holder<ConfiguredFeature<FC, F>> registeredFeature) {
        return (BCLConfigureFeature<F, FC>) KNOWN.computeIfAbsent(
                (Holder<ConfiguredFeature<?, ?>>) (Object) registeredFeature,
                holder -> new BCLConfigureFeature<>(holder.unwrapKey().orElseThrow()
                                                          .location(), registeredFeature, false)
        );
    }
}
