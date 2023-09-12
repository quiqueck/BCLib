package org.betterx.bclib.mixin.common;

import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {
    private int bclib_featureIteratorSeed;


    @ModifyArg(method = "applyBiomeDecoration", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/WorldgenRandom;setFeatureSeed(JII)V"))
    private long bclib_updateFeatureSeed(long seed) {
        return Long.rotateRight(seed, bclib_featureIteratorSeed++);
    }

    @Inject(method = "applyBiomeDecoration", at = @At("HEAD"))
    private void bclib_obBiomeGenerate(
            WorldGenLevel worldGenLevel,
            ChunkAccess chunkAccess,
            StructureManager structureFeatureManager,
            CallbackInfo ci
    ) {
        bclib_featureIteratorSeed = 0;
    }
}
