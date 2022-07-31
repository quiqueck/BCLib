package org.betterx.bclib.api.v3.levelgen.features.config;

import org.betterx.bclib.api.v3.levelgen.features.BCLFeature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class ConditionFeatureConfig implements FeatureConfiguration {
    public static final Codec<ConditionFeatureConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    PlacementModifier.CODEC.fieldOf("filter").forGetter(p -> p.filter),
                    PlacedFeature.CODEC.fieldOf("filter_pass").forGetter(p -> p.okFeature),
                    PlacedFeature.CODEC.optionalFieldOf("filter_fail").forGetter(p -> p.failFeature)
            ).apply(instance, ConditionFeatureConfig::new)
    );

    public final PlacementModifier filter;
    public final Holder<PlacedFeature> okFeature;
    public final Optional<Holder<PlacedFeature>> failFeature;

    public ConditionFeatureConfig(
            @NotNull PlacementFilter filter,
            @NotNull BCLFeature okFeature
    ) {
        this(filter, okFeature.getPlacedFeature(), Optional.empty());

    }

    public ConditionFeatureConfig(
            @NotNull PlacementFilter filter,
            @NotNull BCLFeature okFeature,
            @NotNull BCLFeature failFeature
    ) {
        this(filter, okFeature.getPlacedFeature(), Optional.of(failFeature.getPlacedFeature()));
    }

    public ConditionFeatureConfig(
            @NotNull PlacementFilter filter,
            @NotNull Holder<PlacedFeature> okFeature
    ) {
        this(filter, okFeature, Optional.empty());

    }

    public ConditionFeatureConfig(
            @NotNull PlacementFilter filter,
            @NotNull Holder<PlacedFeature> okFeature,
            @NotNull Holder<PlacedFeature> failFeature
    ) {
        this(filter, okFeature, Optional.of(failFeature));
    }

    protected ConditionFeatureConfig(
            @NotNull PlacementModifier filter,
            @NotNull Holder<PlacedFeature> okFeature,
            @NotNull Optional<Holder<PlacedFeature>> failFeature
    ) {
        this.filter = filter;
        this.okFeature = okFeature;
        this.failFeature = failFeature;
    }
}
