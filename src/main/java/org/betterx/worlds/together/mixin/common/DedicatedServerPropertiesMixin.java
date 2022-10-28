package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.worldPreset.WorldPresets;

import net.minecraft.server.dedicated.DedicatedServerProperties;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Properties;

@Mixin(DedicatedServerProperties.class)
public class DedicatedServerPropertiesMixin {
    //Make sure the default server properties use our Default World Preset by default (read from "level-type")
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/DedicatedServerProperties$WorldDimensionData;<init>(Lcom/google/gson/JsonObject;Ljava/lang/String;)V"))
    protected String wt_defaultPreset(String string) {
        if (WorldsTogether.FORCE_SERVER_TO_BETTERX_PRESET) {
            return WorldPresets.getDEFAULT().location().toString();
        }

        return string;
    }

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/Settings;<init>(Ljava/util/Properties;)V"))
    private static Properties wt_defaultPreset(Properties property) {
        //init default value level preset in server.properties
        property.setProperty(
                "level-type",
                property.getProperty("level-type", WorldPresets.getDEFAULT().location().toString())
        );
        return property;
    }
}
