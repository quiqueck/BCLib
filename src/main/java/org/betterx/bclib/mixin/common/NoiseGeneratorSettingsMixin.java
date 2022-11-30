package org.betterx.bclib.mixin.common;

import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(NoiseGeneratorSettings.class)
public abstract class NoiseGeneratorSettingsMixin {


    ;
//TODO:1.19.3 Replaced by Datagen pack file in NoiseDatagen
//    @Inject(method = "bootstrap", at = @At("HEAD"))
//    private static void bcl_addNoiseGenerators(
//            BootstapContext<NoiseGeneratorSettings> bootstapContext, CallbackInfo ci
//    ) {
//        bootstapContext.register(
//                BCLChunkGenerator.AMPLIFIED_NETHER,
//                BCLChunkGenerator.amplifiedNether(bootstapContext)
//        );
//    }
}
