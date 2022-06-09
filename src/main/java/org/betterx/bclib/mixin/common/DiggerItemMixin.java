package org.betterx.bclib.mixin.common;

import org.betterx.bclib.api.v2.DiggerItemSpeed;

import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(DiggerItem.class)
public class DiggerItemMixin {
    @Inject(method = "getDestroySpeed", at = @At(value = "RETURN"), cancellable = true)
    void bn_getDestroySpeed(ItemStack stack, BlockState state, CallbackInfoReturnable<Float> cir) {
        final Optional<Float> newSpeed = DiggerItemSpeed.getModifiedSpeed(stack, state, cir.getReturnValue());
        if (newSpeed.isPresent()) {
            cir.setReturnValue(newSpeed.get());
        }
    }
}
