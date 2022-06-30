package org.betterx.bclib.mixin.common;

import org.betterx.bclib.interfaces.TheEndBiomeDataAccessor;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import net.fabricmc.fabric.impl.biome.TheEndBiomeData;
import net.fabricmc.fabric.impl.biome.WeightedPicker;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = TheEndBiomeData.class, remap = false)
public class TheEndBiomeDataMixin implements TheEndBiomeDataAccessor {
    @Shadow
    @Final
    private static Map<ResourceKey<Biome>, WeightedPicker<ResourceKey<Biome>>> END_BIOMES_MAP;
    @Shadow
    @Final
    private static Map<ResourceKey<Biome>, WeightedPicker<ResourceKey<Biome>>> END_MIDLANDS_MAP;
    @Shadow
    @Final
    private static Map<ResourceKey<Biome>, WeightedPicker<ResourceKey<Biome>>> END_BARRENS_MAP;

    public boolean bcl_canGenerateAsEndBiome(ResourceKey<Biome> key) {
        return END_BIOMES_MAP != null && END_BIOMES_MAP.containsKey(key);
    }

    public boolean bcl_canGenerateAsEndMidlandBiome(ResourceKey<Biome> key) {
        return END_MIDLANDS_MAP != null && END_MIDLANDS_MAP.containsKey(key);
    }

    public boolean bcl_canGenerateAsEndBarrensBiome(ResourceKey<Biome> key) {
        return END_BARRENS_MAP != null && END_BARRENS_MAP.containsKey(key);
    }
}
