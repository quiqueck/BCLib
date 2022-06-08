package org.betterx.bclib.mixin.common;

import org.betterx.bclib.presets.worldgen.BCLWorldPresets;

import net.minecraft.server.dedicated.DedicatedServerProperties;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(DedicatedServerProperties.class)
public class DedicatedServerPropertiesMixin {
    //Make sure the default server properties use our Default World Preset
    @ModifyArg(method = "<init>", index = 3, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/DedicatedServerProperties$WorldGenProperties;<init>(Ljava/lang/String;Lcom/google/gson/JsonObject;ZLjava/lang/String;)V"))
    private String bcl_init(String levelType) {
        return BCLWorldPresets.DEFAULT.orElseThrow().location().toString();
    }
}
