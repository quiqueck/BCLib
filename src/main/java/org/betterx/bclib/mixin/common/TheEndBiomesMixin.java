package org.betterx.bclib.mixin.common;

import org.betterx.bclib.api.v2.generator.TheEndBiomesHelper;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiome;
import org.betterx.bclib.api.v2.levelgen.biomes.InternalBiomeAPI;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import net.fabricmc.fabric.api.biome.v1.TheEndBiomes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TheEndBiomes.class, remap = false)
public class TheEndBiomesMixin {
    @Inject(method = "addBarrensBiome", at = @At("HEAD"))
    private static void bcl_registerBarrens(
            ResourceKey<Biome> highlands,
            ResourceKey<Biome> barrens,
            double weight,
            CallbackInfo ci
    ) {
        TheEndBiomesHelper.add(InternalBiomeAPI.OTHER_END_BARRENS, barrens);
    }

    @Inject(method = "addMidlandsBiome", at = @At("HEAD"))
    private static void bcl_registerMidlands(
            ResourceKey<Biome> highlands,
            ResourceKey<Biome> midlands,
            double weight,
            CallbackInfo ci
    ) {
        BCLBiome highland = InternalBiomeAPI.wrapNativeBiome(highlands, InternalBiomeAPI.OTHER_END_LAND);
        BCLBiome midland = InternalBiomeAPI.wrapNativeBiome(midlands, InternalBiomeAPI.OTHER_END_LAND);
        if (highland != null) {
            highland.addEdge(midland);
        }
        TheEndBiomesHelper.add(InternalBiomeAPI.OTHER_END_LAND, midlands);
    }

    @Inject(method = "addSmallIslandsBiome", at = @At("HEAD"))
    private static void bcl_registerSmallIslands(
            ResourceKey<Biome> biome, double weight, CallbackInfo ci
    ) {
        TheEndBiomesHelper.add(InternalBiomeAPI.OTHER_END_VOID, biome);
    }

    @Inject(method = "addHighlandsBiome", at = @At("HEAD"))
    private static void bcl_registerHighlands(
            ResourceKey<Biome> biome, double weight, CallbackInfo ci
    ) {
        TheEndBiomesHelper.add(InternalBiomeAPI.OTHER_END_LAND, biome);
    }

    @Inject(method = "addMainIslandBiome", at = @At("HEAD"))
    private static void bcl_registerMainIsnalnd(
            ResourceKey<Biome> biome, double weight, CallbackInfo ci
    ) {
        TheEndBiomesHelper.add(InternalBiomeAPI.OTHER_END_CENTER, biome);
    }
}
