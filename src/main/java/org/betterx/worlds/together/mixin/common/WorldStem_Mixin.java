package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.world.event.WorldBootstrap;

import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.WorldStem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = WorldStem.class, priority = 1500)
public class WorldStem_Mixin {
    @ModifyVariable(method = "<init>", argsOnly = true, at = @At(value = "INVOKE", target = "Ljava/lang/Record;<init>()V", shift = At.Shift.AFTER))
    LayeredRegistryAccess<RegistryLayer> wt_bake(LayeredRegistryAccess<RegistryLayer> registries) {
        LayeredRegistryAccess<RegistryLayer> rNew = WorldBootstrap.enforceInLayeredRegistry(registries);
        return rNew;
    }
}
