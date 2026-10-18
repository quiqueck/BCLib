package org.betterx.bclib.mixin.common;

import org.betterx.bclib.api.v2.levelgen.biomes.VanillaBootstrapGuard;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

import net.fabricmc.fabric.impl.biome.NetherBiomeData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

/**
 * Fabric adds every {@code NetherBiomes.addNetherBiome} entry (all BCL Nether biomes) to the vanilla Nether
 * parameter-list preset. While {@code VanillaRegistries.createLookup()} is built only vanilla biomes exist, so
 * keep the vanilla entries there (see {@link VanillaBootstrapGuard}).
 */
@Mixin(value = NetherBiomeData.class, remap = false)
public class NetherBiomeDataMixin {
    @Inject(method = "withModdedBiomeEntries", at = @At("HEAD"), cancellable = true)
    private static <T> void bcl_skipInVanillaLookup(
            Climate.ParameterList<T> entries,
            Function<ResourceKey<Biome>, T> biomes,
            CallbackInfoReturnable<Climate.ParameterList<T>> cir
    ) {
        if (VanillaBootstrapGuard.isBuildingVanillaLookup()) {
            cir.setReturnValue(entries);
        }
    }
}
