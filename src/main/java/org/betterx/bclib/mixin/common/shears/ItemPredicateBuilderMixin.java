package org.betterx.bclib.mixin.common.shears;

import org.betterx.worlds.together.tag.v3.CommonItemTags;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ItemPredicate.class)
public abstract class ItemPredicateBuilderMixin {

    @Shadow
    @Final
    private Optional<HolderSet<Item>> items;

    @Unique
    private static final ResourceLocation BCL_SHEARS = new ResourceLocation("shears");

    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    void bclib_isShears(ItemStack itemStack, CallbackInfoReturnable<Boolean> info) {
        if (this.items != null && this.items.isPresent()) {
            final HolderSet<Item> items = this.items.get();
            if (items.size() == 1 && items.get(0) != null && items.get(0).is(BCL_SHEARS)) {
                if (itemStack.is(CommonItemTags.SHEARS)) {
                    info.setReturnValue(true);
                }
            }
        }
    }
}
