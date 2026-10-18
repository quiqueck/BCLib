package org.betterx.bclib.mixin.common;

import org.betterx.bclib.api.v2.levelgen.biomes.VanillaBootstrapGuard;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.registries.VanillaRegistries;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VanillaRegistries.class)
public class VanillaRegistriesLookupMixin {
    @Inject(method = "createLookup", at = @At("HEAD"))
    private static void bcl_enterVanillaLookup(CallbackInfoReturnable<HolderLookup.Provider> cir) {
        VanillaBootstrapGuard.enter();
    }

    @Inject(method = "createLookup", at = @At("RETURN"))
    private static void bcl_leaveVanillaLookup(CallbackInfoReturnable<HolderLookup.Provider> cir) {
        VanillaBootstrapGuard.leave();
    }
}
