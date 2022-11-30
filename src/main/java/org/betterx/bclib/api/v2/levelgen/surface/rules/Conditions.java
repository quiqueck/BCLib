package org.betterx.bclib.api.v2.levelgen.surface.rules;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.interfaces.NumericProvider;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.world.level.levelgen.SurfaceRules;

public class Conditions {
    public static final ThresholdCondition DOUBLE_BLOCK_SURFACE_NOISE = new ThresholdCondition(
            4141,
            0,
            UniformFloat.of(-0.4f, 0.4f),
            0.1,
            0.1
    );

    public static final ThresholdCondition FORREST_FLOOR_SURFACE_NOISE_A = new ThresholdCondition(
            614,
            0,
            UniformFloat.of(-0.2f, 0f),
            0.1,
            0.1
    );

    public static final ThresholdCondition FORREST_FLOOR_SURFACE_NOISE_B = new ThresholdCondition(
            614,
            0,
            UniformFloat.of(-0.7f, -0.5f),
            0.1,
            0.1
    );

    public static final ThresholdCondition NETHER_SURFACE_NOISE = new ThresholdCondition(
            245,
            0,
            UniformFloat.of(-0.7f, -0.5f),
            0.05,
            0.05
    );

    public static final ThresholdCondition NETHER_SURFACE_NOISE_LARGE = new ThresholdCondition(
            523,
            0,
            UniformFloat.of(-0.4f, -0.3f),
            0.5,
            0.5
    );

    public static final VolumeThresholdCondition NETHER_VOLUME_NOISE = new VolumeThresholdCondition(
            245,
            0,
            UniformFloat.of(-0.1f, 0.2f),
            0.1,
            0.2,
            0.1
    );

    public static final VolumeThresholdCondition NETHER_VOLUME_NOISE_LARGE = new VolumeThresholdCondition(
            523,
            0,
            UniformFloat.of(-0.1f, 0.4f),
            0.2,
            0.2,
            0.2
    );

    public static final NumericProvider NETHER_NOISE = new NetherNoiseCondition();

    public static void register(ResourceLocation location, Codec<? extends SurfaceRules.ConditionSource> codec) {
        Registry.register(BuiltInRegistries.MATERIAL_CONDITION, location, codec);
    }

    public static void registerNumeric(ResourceLocation location, Codec<? extends NumericProvider> codec) {
        Registry.register(NumericProvider.NUMERIC_PROVIDER, location, codec);
    }

    public static void registerAll() {
        registerNumeric(BCLib.makeID("rnd_int"), RandomIntProvider.CODEC);
        registerNumeric(BCLib.makeID("nether_noise"), NetherNoiseCondition.CODEC);
        register(BCLib.makeID("threshold_condition"), ThresholdCondition.CODEC);
        register(BCLib.makeID("volume_threshold_condition"), VolumeThresholdCondition.CODEC);
        register(BCLib.makeID("rough_noise_condition"), RoughNoiseCondition.CODEC);

        Registry.register(BuiltInRegistries.MATERIAL_RULE, "bclib_switch_rule", SwitchRuleSource.CODEC);
    }
}
