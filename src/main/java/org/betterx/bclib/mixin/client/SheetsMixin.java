package org.betterx.bclib.mixin.client;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.state.properties.WoodType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla fills {@link Sheets#SIGN_MATERIALS} and {@link Sheets#HANGING_SIGN_MATERIALS} once, when the class is
 * initialised. If any mod touches {@link Sheets} before our wood types are registered, our signs have no material
 * and the sign renderer crashes with a NullPointerException. Create (and remember) the missing materials on demand.
 */
@Mixin(Sheets.class)
public abstract class SheetsMixin {
    @Shadow
    private static Material createSignMaterial(WoodType woodType) {
        throw new AssertionError();
    }

    @Shadow
    private static Material createHangingSignMaterial(WoodType woodType) {
        throw new AssertionError();
    }

    @Inject(method = "getSignMaterial", at = @At("RETURN"), cancellable = true)
    private static void bcl_createMissingSignMaterial(WoodType woodType, CallbackInfoReturnable<Material> cir) {
        if (cir.getReturnValue() == null && woodType != null) {
            final Material material = createSignMaterial(woodType);
            Sheets.SIGN_MATERIALS.put(woodType, material);
            cir.setReturnValue(material);
        }
    }

    @Inject(method = "getHangingSignMaterial", at = @At("RETURN"), cancellable = true)
    private static void bcl_createMissingHangingSignMaterial(WoodType woodType, CallbackInfoReturnable<Material> cir) {
        if (cir.getReturnValue() == null && woodType != null) {
            final Material material = createHangingSignMaterial(woodType);
            Sheets.HANGING_SIGN_MATERIALS.put(woodType, material);
            cir.setReturnValue(material);
        }
    }
}
