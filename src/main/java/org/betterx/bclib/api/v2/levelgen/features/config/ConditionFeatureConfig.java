package org.betterx.bclib.api.v2.levelgen.features.config;

import org.betterx.bclib.api.v2.levelgen.features.BCLFeature;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.config.ConditionFeatureConfig} instead
 */
@Deprecated(forRemoval = true)
public class ConditionFeatureConfig extends org.betterx.bclib.api.v3.levelgen.features.config.ConditionFeatureConfig {

    public ConditionFeatureConfig(@NotNull PlacementFilter filter, @NotNull BCLFeature okFeature) {
        super(filter, okFeature);
    }

    public ConditionFeatureConfig(
            @NotNull PlacementFilter filter,
            @NotNull BCLFeature okFeature,
            @NotNull BCLFeature failFeature
    ) {
        super(filter, okFeature, failFeature);
    }

    public ConditionFeatureConfig(@NotNull PlacementFilter filter, @NotNull Holder<PlacedFeature> okFeature) {
        super(filter, okFeature);
    }

    public ConditionFeatureConfig(
            @NotNull PlacementFilter filter,
            @NotNull Holder<PlacedFeature> okFeature,
            @NotNull Holder<PlacedFeature> failFeature
    ) {
        super(filter, okFeature, failFeature);
    }

    protected ConditionFeatureConfig(
            @NotNull PlacementModifier filter,
            @NotNull Holder<PlacedFeature> okFeature,
            @NotNull Optional<Holder<PlacedFeature>> failFeature
    ) {
        super(filter, okFeature, failFeature);
    }
}
