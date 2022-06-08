package org.betterx.bclib.mixin.common;

import org.betterx.bclib.util.MethodReplace;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "is(Lnet/minecraft/world/item/Item;)Z", at = @At("HEAD"), cancellable = true)
    private void bclib_replaceFunction(Item item, CallbackInfoReturnable<Boolean> info) {
        Function<ItemStack, Boolean> replacement = MethodReplace.getItemReplace(item);
        if (replacement != null) {
            info.setReturnValue(replacement.apply(ItemStack.class.cast(this)));
        }
    }
}
