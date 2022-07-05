package org.betterx.bclib.mixin.common;

import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Mixin(BiomeGenerationSettings.class)
public interface BiomeGenerationSettingsAccessor {
    @Accessor("features")
    List<HolderSet<PlacedFeature>> bclib_getFeatures();

    @Accessor("features")
    @Mutable
    void bclib_setFeatures(List<HolderSet<PlacedFeature>> value);

    @Accessor("featureSet")
    void bclib_setFeatureSet(Supplier<Set<PlacedFeature>> featureSet);

    @Accessor("flowerFeatures")
    void bclib_setFlowerFeatures(Supplier<List<ConfiguredFeature<?, ?>>> flowerFeatures);

    @Accessor("carvers")
    Map<GenerationStep.Carving, HolderSet<ConfiguredWorldCarver<?>>> bclib_getCarvers();

    @Accessor("carvers")
    void bclib_setCarvers(Map<GenerationStep.Carving, HolderSet<ConfiguredWorldCarver<?>>> features);
}
