package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.levelgen.WorldGenUtil;
import org.betterx.worlds.together.worldPreset.WorldPresets;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(net.minecraft.world.level.levelgen.presets.WorldPresets.Bootstrap.class)
public abstract class WorldPresetsBootstrapMixin {
    @Shadow
    @Final
    private HolderGetter<Biome> biomes;
    @Shadow
    @Final
    private HolderGetter<StructureSet> structureSets;

    @Shadow
    @Final
    private BootstapContext<WorldPreset> context;
    @Shadow
    @Final
    private HolderGetter<PlacedFeature> placedFeatures;
    @Shadow
    @Final
    private LevelStem netherStem;
    @Shadow
    @Final
    private LevelStem endStem;
    //see WorldPresets.register

    @Shadow
    protected abstract LevelStem makeNoiseBasedOverworld(
            BiomeSource biomeSource,
            Holder<NoiseGeneratorSettings> holder
    );

    @Shadow
    @Final
    private HolderGetter<NoiseGeneratorSettings> noiseSettings;

    @ModifyArg(method = "run", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/level/levelgen/presets/WorldPresets$Bootstrap;registerCustomOverworldPreset(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/dimension/LevelStem;)V"))
    private LevelStem bcl_getOverworldStem(LevelStem overworldStem) {
        Holder<NoiseGeneratorSettings> netherSettings, endSettings;
        if (this.netherStem.generator() instanceof NoiseBasedChunkGenerator nether) {
            netherSettings = nether.generatorSettings();
        } else {
            netherSettings = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.NETHER);
        }

        if (this.endStem.generator() instanceof NoiseBasedChunkGenerator nether) {
            endSettings = nether.generatorSettings();
        } else {
            endSettings = this.noiseSettings.getOrThrow(NoiseGeneratorSettings.END);
        }

        WorldGenUtil.Context netherContext = new WorldGenUtil.Context(
                this.biomes,
                this.netherStem.type(),
                this.structureSets,
                netherSettings
        );
        WorldGenUtil.Context endContext = new WorldGenUtil.Context(
                this.biomes,
                this.endStem.type(),
                this.structureSets,
                endSettings
        );

        WorldPresets.bootstrapPresets(
                context,
                overworldStem,
                netherContext,
                endContext,
                noiseSettings,
                this::makeNoiseBasedOverworld
        );

        return overworldStem;
    }

}
