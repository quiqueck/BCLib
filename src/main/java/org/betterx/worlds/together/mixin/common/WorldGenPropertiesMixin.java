package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.worldPreset.WorldPreset;
import org.betterx.worlds.together.worldPreset.WorldPresets;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dedicated.DedicatedServerProperties;

import com.google.gson.JsonObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DedicatedServerProperties.WorldGenProperties.class)
public class WorldGenPropertiesMixin {
    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private static void bcl_create(String string, JsonObject jsonObject, boolean bl, String string2, CallbackInfo ci) {
        return seed;
    }

    //Make sure Servers use our Default World Preset
    @ModifyArg(method = "create", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/core/Registry;getHolder(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;"))
    private ResourceKey<WorldPreset> bcl_foo(ResourceKey<WorldPreset> resourceKey) {
        return WorldPresets.getDEFAULT();
    }
}