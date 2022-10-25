package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.levelgen.WorldGenUtil;
import org.betterx.worlds.together.worldPreset.WorldPresets;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(net.minecraft.world.level.levelgen.presets.WorldPresets.Bootstrap.class)
public abstract class WorldPresetsBootstrapMixin {
    @Shadow
    @Final
    private Registry<WorldPreset> presets;
    @Shadow
    @Final
    private Registry<Biome> biomes;
    @Shadow
    @Final
    private Registry<StructureSet> structureSets;
    @Shadow
    @Final
    private Registry<NormalNoise.NoiseParameters> noises;
    @Shadow
    @Final
    private Holder<DimensionType> netherDimensionType;
    @Shadow
    @Final
    private Holder<NoiseGeneratorSettings> netherNoiseSettings;
    @Shadow
    @Final
    private Holder<DimensionType> endDimensionType;
    @Shadow
    @Final
    private Holder<NoiseGeneratorSettings> endNoiseSettings;

    //see WorldPresets.register

    //TODO: 1.19.3
    @ModifyArg(method = "run", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/level/levelgen/presets/WorldPresets$Bootstrap;registerCustomOverworldPreset(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/dimension/LevelStem;)Lnet/minecraft/core/Holder;"))
    private LevelStem bcl_getOverworldStem(LevelStem overworldStem) {
        WorldGenUtil.Context netherContext = new WorldGenUtil.Context(
                this.biomes,
                this.netherDimensionType,
                this.structureSets,
                this.noises,
                this.netherNoiseSettings
        );
        WorldGenUtil.Context endContext = new WorldGenUtil.Context(
                this.biomes,
                this.endDimensionType,
                this.structureSets,
                this.noises,
                this.endNoiseSettings
        );

        WorldPresets.bootstrapPresets(presets, overworldStem, netherContext, endContext);

        return overworldStem;
    }

}
