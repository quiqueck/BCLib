package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.levelgen.WorldGenUtil;
import org.betterx.worlds.together.worldPreset.WorldPreset;
import org.betterx.worlds.together.worldPreset.WorldPresets;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldPreset.class)
public abstract class WorldPresetsBootstrapMixin {

    //see WorldPresets.register

    @Inject(method = "<clinit>", at = @At(value = "TAIL"))
    private static void bcl_getOverworldStem(CallbackInfo ci) {
        Registry<org.betterx.worlds.together.worldPreset.WorldPreset> presets = WorldPresets.WORLD_PRESET;
        Registry<DimensionType> dimensionTypes = BuiltinRegistries.ACCESS.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<Biome> biomes = BuiltinRegistries.BIOME;
        Registry<StructureSet> structureSets = BuiltinRegistries.STRUCTURE_SETS;
        Registry<NoiseGeneratorSettings> noiseSettings = BuiltinRegistries.NOISE_GENERATOR_SETTINGS;
        Registry<NormalNoise.NoiseParameters> noises = BuiltinRegistries.NOISE;
        Holder<DimensionType> overworldDimensionType = dimensionTypes.getOrCreateHolder(DimensionType.OVERWORLD_LOCATION);
        Holder<DimensionType> netherDimensionType = dimensionTypes.getOrCreateHolder(DimensionType.NETHER_LOCATION);
        Holder<NoiseGeneratorSettings> netherNoiseSettings = noiseSettings.getOrCreateHolder(NoiseGeneratorSettings.NETHER);
        Holder<DimensionType> endDimensionType = dimensionTypes.getOrCreateHolder(DimensionType.END_LOCATION);
        Holder<NoiseGeneratorSettings> endNoiseSettings = noiseSettings.getOrCreateHolder(NoiseGeneratorSettings.END);


        LevelStem overworldStem = new LevelStem(
                overworldDimensionType,
                WorldGenSettings.makeDefaultOverworld(BuiltinRegistries.ACCESS, 0)
        );

        WorldGenUtil.Context netherContext = new WorldGenUtil.Context(
                biomes,
                netherDimensionType,
                structureSets,
                noises,
                netherNoiseSettings
        );
        WorldGenUtil.Context endContext = new WorldGenUtil.Context(
                biomes,
                endDimensionType,
                structureSets,
                noises,
                endNoiseSettings
        );

        WorldPresets.bootstrapPresets(presets, overworldStem, netherContext, endContext);
    }

}
