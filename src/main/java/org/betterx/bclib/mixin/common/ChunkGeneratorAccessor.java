package org.betterx.bclib.mixin.common;

import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.chunk.ChunkGenerator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.function.Supplier;

@Mixin(ChunkGenerator.class)
public interface ChunkGeneratorAccessor {
    @Accessor("biomeSource")
    @Mutable
    void bcl_setBiomeSource(BiomeSource biomeSource);

    @Accessor("featuresPerStep")
    @Mutable
    void bcl_setFeaturesPerStep(Supplier<List<FeatureSorter.StepFeatureData>> supplier);
}
