package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.worldPreset.WorldPresets;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(DedicatedServerProperties.WorldDimensionData.class)
public class WorldGenPropertiesMixin {
//    @ModifyArg(method = "create", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/presets/WorldPreset;createWorldGenSettings(JZZ)Lnet/minecraft/world/level/levelgen/WorldGenSettings;"))
//    public long wt_getSeed(long seed) {
//        return seed;
//    }

    //Make sure Servers use our Default World Preset
    @ModifyArg(method = "create", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/core/Registry;getHolder(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;"))
    private ResourceKey<WorldPreset> wt_returnDefault(ResourceKey<WorldPreset> resourceKey) {
        return WorldPresets.getDEFAULT();
    }
}