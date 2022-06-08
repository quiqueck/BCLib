package org.betterx.bclib.mixin.common;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.world.level.dimension.DimensionType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionType.class)
public class DimensionTypeMixin {
    //	@Inject(
//		method = "defaultDimensions(Lnet/minecraft/core/RegistryAccess;JZ)Lnet/minecraft/core/Registry;",
//		locals = LocalCapture.CAPTURE_FAILHARD,
//		at = @At("TAIL")
//	)
    private static void bclib_updateDimensions(
            RegistryAccess registryAccess,
            long seed,
            boolean bl,
            CallbackInfoReturnable<Registry> info,
            WritableRegistry writableRegistry,
            Registry registry,
            Registry biomeRegistry,
            Registry structureRegistry,
            Registry noiseSettingsRegistry,
            Registry noiseParamRegistry
    ) {
        //This probably moved to WorldPresets.bootstrap();
//		int id = writableRegistry.getId(writableRegistry.get(LevelStem.NETHER));
//		writableRegistry.registerOrOverride(
//				OptionalInt.of(id),
//				LevelStem.NETHER,
//				new LevelStem(
//						registry.getOrCreateHolder(BuiltinDimensionTypes.NETHER),
//						new NoiseBasedChunkGenerator(
//								structureRegistry,
//								noiseParamRegistry,
//								new BCLibNetherBiomeSource(biomeRegistry, seed),
//								seed,
//								noiseSettingsRegistry.getOrCreateHolder(NoiseGeneratorSettings.NETHER))
//				),
//				Lifecycle.stable()
//		);
//
//
//		id = writableRegistry.getId(writableRegistry.get(LevelStem.END));
//		writableRegistry.registerOrOverride(
//				OptionalInt.of(id),
//				LevelStem.END,
//				new LevelStem(
//						registry.getOrCreateHolder(BuiltinDimensionTypes.END),
//						new NoiseBasedChunkGenerator(
//								structureRegistry,
//								noiseParamRegistry,
//								new BCLibEndBiomeSource(biomeRegistry, seed),
//								seed,
//								noiseSettingsRegistry.getOrCreateHolder(NoiseGeneratorSettings.END))
//				),
//				Lifecycle.stable()
//		);
    }
}
