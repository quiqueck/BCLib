package org.betterx.bclib.mixin.common.shears;

import org.betterx.wover.tag.api.predefined.CommonItemTags;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(MatchTool.class)
public class MatchToolMixin {
    @Unique
    private boolean bcl_isShears;
    @Unique
    private static final ItemPredicate BCL_SHEARS_PREDICATE = ItemPredicate.Builder
            .item()
            .of(CommonItemTags.SHEARS)
            .build();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void bcl_initShears(Optional<ItemPredicate> optional, CallbackInfo ci) {
        if (optional.isPresent()) {
            if (optional.get().items().isPresent()) {
                final var items = optional.get().items().get();
                if (items.size() == 1 && items.get(0).value() == Items.SHEARS) {
                    bcl_isShears = true;
                }
            }
        }
    }

    @Inject(method = "test(Lnet/minecraft/world/level/storage/loot/LootContext;)Z", at = @At("HEAD"), cancellable = true)
    private void bcl_isShears(LootContext lootContext, CallbackInfoReturnable<Boolean> cir) {
        if (bcl_isShears) {
            ItemStack itemStack = lootContext.getParamOrNull(LootContextParams.TOOL);
            cir.setReturnValue(itemStack != null && BCL_SHEARS_PREDICATE.test(itemStack));
        }
    }
}
