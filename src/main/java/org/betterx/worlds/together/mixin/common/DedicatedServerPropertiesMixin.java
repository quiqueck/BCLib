package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.worldPreset.WorldPresets;

import net.minecraft.server.dedicated.DedicatedServerProperties;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(DedicatedServerProperties.class)
public class DedicatedServerPropertiesMixin {
    //Make sure the default server properties use our Default World Preset by default (read from "level-type")
    @ModifyArg(method = "<init>", index = 3, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/DedicatedServerProperties$WorldGenProperties;<init>(Ljava/lang/String;Lcom/google/gson/JsonObject;ZLjava/lang/String;)V"))
    protected String wt_defaultPreset(String string) {
        if (WorldsTogether.FORCE_SERVER_TO_BETTERX_PRESET) {
            return WorldPresets.getDEFAULT().location().toString();
        }

        return string;
    }
}
