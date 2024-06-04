package org.betterx.bclib.mixin.common.shears;

import org.betterx.bclib.items.tool.BaseShearsItem;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.TripWireBlock;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TripWireBlock.class)
public class TripWireBlockMixin {
    @WrapOperation(
            method = "playerWillDestroy",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"
            )
    )
    private boolean bclib_isShears(ItemStack instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || (item == Items.SHEARS && BaseShearsItem.isShear(instance));
    }
}
