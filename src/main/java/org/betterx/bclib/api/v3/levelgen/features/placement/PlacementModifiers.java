package org.betterx.bclib.api.v3.levelgen.features.placement;

import org.betterx.bclib.BCLib;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class PlacementModifiers {
    public static final PlacementModifierType<Stencil> STENCIL = register(
            "stencil",
            Stencil.CODEC
    );
    public static final PlacementModifierType<IsNextTo> IS_NEXT_TO = register(
            "is_next_to",
            IsNextTo.CODEC
    );
    public static final PlacementModifierType<NoiseFilter> NOISE_FILTER = register(
            "noise_filter",
            NoiseFilter.CODEC
    );
    public static final PlacementModifierType<Debug> DEBUG = register(
            "debug",
            Debug.CODEC
    );

    public static final PlacementModifierType<ForAll> FOR_ALL = register(
            "for_all",
            ForAll.CODEC
    );

    public static final PlacementModifierType<FindSolidInDirection> SOLID_IN_DIR = register(
            "solid_in_dir",
            FindSolidInDirection.CODEC
    );

    public static final PlacementModifierType<All> ALL = register(
            "all",
            All.CODEC
    );

    public static final PlacementModifierType<IsBasin> IS_BASIN = register(
            "is_basin",
            IsBasin.CODEC
    );

    public static final PlacementModifierType<Is> IS = register(
            "is",
            Is.CODEC
    );

    public static final PlacementModifierType<Offset> OFFSET = register(
            "offset",
            Offset.CODEC
    );

    public static final PlacementModifierType<Extend> EXTEND = register(
            "extend",
            Extend.CODEC
    );

    public static final PlacementModifierType<OnEveryLayer> ON_EVERY_LAYER = register(
            "on_every_layer",
            OnEveryLayer.CODEC
    );

    public static final PlacementModifierType<UnderEveryLayer> UNDER_EVERY_LAYER = register(
            "under_every_layer",
            UnderEveryLayer.CODEC
    );

    public static final PlacementModifierType<InBiome> IN_BIOME = register(
            "in_biome",
            InBiome.CODEC
    );


    private static <P extends PlacementModifier> PlacementModifierType<P> register(String path, Codec<P> codec) {
        return register(BCLib.makeID(path), codec);
    }

    public static <P extends PlacementModifier> PlacementModifierType<P> register(
            ResourceLocation location,
            Codec<P> codec
    ) {
        return Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, location, () -> codec);
    }

    public static void ensureStaticInitialization() {

    }
}

