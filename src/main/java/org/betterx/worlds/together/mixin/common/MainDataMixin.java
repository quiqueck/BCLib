package org.betterx.worlds.together.mixin.common;

import net.minecraft.data.Main;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(Main.class)
public class MainDataMixin {
//    @Inject(method = "createStandardGenerator", at = @At("RETURN"))
//    private static void wt_createStandardGenerator(
//            Path path,
//            Collection<Path> collection,
//            boolean isClient,
//            boolean isServer,
//            boolean isDev,
//            boolean reports,
//            boolean validate,
//            WorldVersion worldVersion,
//            boolean alwaysGenerate,
//            CallbackInfoReturnable<DataGenerator> cir
//    ) {
//        DataGenerator dataGenerator = cir.getReturnValue();
//        dataGenerator.addProvider(isServer, new SurfaceRulesDatapackGenerator(dataGenerator.getVanillaPackOutput()));
//        cir.setReturnValue(dataGenerator);
//    }
}
