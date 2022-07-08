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
//    @ModifyArg(method = "create", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/presets/WorldPreset;createWorldGenSettings(JZZ)Lnet/minecraft/world/level/levelgen/WorldGenSettings;"))
//    public long bcl_create(long seed) {
//        return seed;
//    }

    //Make sure Servers use our Default World Preset
    @ModifyArg(method = "create", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/core/Registry;getHolder(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;"))
    private ResourceKey<WorldPreset> wt_returnDefault(ResourceKey<WorldPreset> resourceKey) {
        return WorldPresets.getDEFAULT();
    }
}