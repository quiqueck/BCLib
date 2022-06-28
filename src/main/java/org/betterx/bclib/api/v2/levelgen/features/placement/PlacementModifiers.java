package org.betterx.bclib.api.v2.levelgen.features.placement;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v3.levelgen.features.placement.All;
import org.betterx.bclib.api.v3.levelgen.features.placement.Debug;
import org.betterx.bclib.api.v3.levelgen.features.placement.Extend;
import org.betterx.bclib.api.v3.levelgen.features.placement.FindSolidInDirection;
import org.betterx.bclib.api.v3.levelgen.features.placement.ForAll;
import org.betterx.bclib.api.v3.levelgen.features.placement.Is;
import org.betterx.bclib.api.v3.levelgen.features.placement.IsBasin;
import org.betterx.bclib.api.v3.levelgen.features.placement.Offset;
import org.betterx.bclib.api.v3.levelgen.features.placement.OnEveryLayer;
import org.betterx.bclib.api.v3.levelgen.features.placement.Stencil;
import org.betterx.bclib.api.v3.levelgen.features.placement.UnderEveryLayer;
import org.betterx.bclib.api.v3.levelgen.features.placement.*;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers} instead
 */
@Deprecated(forRemoval = true)
public class PlacementModifiers {
    /**
     * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers#NOISE_FILTER} instead
     */
    @Deprecated(forRemoval = true)
    public static final PlacementModifierType<NoiseFilter> NOISE_FILTER = org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers.NOISE_FILTER;
    /**
     * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers#DEBUG} instead
     */
    @Deprecated(forRemoval = true)
    public static final PlacementModifierType<Debug> DEBUG = org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers.DEBUG;
    @Deprecated(forRemoval = true)
    public static final PlacementModifierType<IsEmptyAboveSampledFilter> IS_EMPTY_ABOVE_SAMPLED_FILTER = register(
            "is_empty_above_sampled_filter",
            IsEmptyAboveSampledFilter.CODEC
    );

    public static final PlacementModifierType<MinEmptyFilter> MIN_EMPTY_FILTER = register(
            "min_empty_filter",
            MinEmptyFilter.CODEC
    );

    /**
     * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers#FOR_ALL} instead
     */
    @Deprecated(forRemoval = true)
    public static final PlacementModifierType<ForAll> FOR_ALL = org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers.FOR_ALL;

    /**
     * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers#SOLID_IN_DIR} instead
     */
    @Deprecated(forRemoval = true)
    public static final PlacementModifierType<FindSolidInDirection> SOLID_IN_DIR = org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers.SOLID_IN_DIR;

    /**
     * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers#STENCIL} instead
     */
    @Deprecated(forRemoval = true)
    public static final PlacementModifierType<Stencil> STENCIL = org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers.STENCIL;

    /**
     * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers#ALL} instead
     */
    @Deprecated(forRemoval = true)
    public static final PlacementModifierType<All> ALL = org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers.ALL;

    /**
     * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers#IS_BASIN} instead
     */
    @Deprecated(forRemoval = true)
    public static final PlacementModifierType<IsBasin> IS_BASIN = org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers.IS_BASIN;

    /**
     * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers#IS} instead
     */
    @Deprecated(forRemoval = true)
    public static final PlacementModifierType<Is> IS = org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers.IS;

    /**
     * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers#OFFSET} instead
     */
    @Deprecated(forRemoval = true)
    public static final PlacementModifierType<Offset> OFFSET = org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers.OFFSET;

    /**
     * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers#EXTEND} instead
     */
    @Deprecated(forRemoval = true)
    public static final PlacementModifierType<Extend> EXTEND = org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers.EXTEND;

    /**
     * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers#ON_EVERY_LAYER} instead
     */
    @Deprecated(forRemoval = true)
    public static final PlacementModifierType<OnEveryLayer> ON_EVERY_LAYER = org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers.ON_EVERY_LAYER;

    /**
     * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers#UNDER_EVERY_LAYER} instead
     */
    @Deprecated(forRemoval = true)
    public static final PlacementModifierType<UnderEveryLayer> UNDER_EVERY_LAYER = org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers.UNDER_EVERY_LAYER;

    private static <P extends PlacementModifier> PlacementModifierType<P> register(String path, Codec<P> codec) {
        return register(BCLib.makeID(path), codec);
    }


    /**
     * @param location
     * @param codec
     * @param <P>
     * @return
     * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers#register(ResourceLocation, Codec)} instead
     */
    @Deprecated(forRemoval = true)
    public static <P extends PlacementModifier> PlacementModifierType<P> register(
            ResourceLocation location,
            Codec<P> codec
    ) {
        return org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers.register(location, codec);
    }

    @Deprecated(forRemoval = true)
    public static void ensureStaticInitialization() {

    }
}

