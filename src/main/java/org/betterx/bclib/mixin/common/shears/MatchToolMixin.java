package org.betterx.bclib.mixin.common.shears;

import org.betterx.worlds.together.tag.v3.CommonItemTags;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/**
 * Lets every item in {@code c:shears} pass the vanilla "tool is minecraft:shears" loot condition
 * (leaves, grass, vines, cobweb, ... dropping themselves).
 * <p>
 * Whether this condition is such a shears check is decided once, when the condition is built, so
 * a loot roll only costs a single field read for every other match_tool condition. This replaces
 * the former {@code ItemPredicate#matches} hook, which ran for every item predicate in the game
 * (advancement triggers, loot, ...).
 */
@Mixin(MatchTool.class)
public class MatchToolMixin {
    @Shadow
    @Final
    ItemPredicate predicate;

    @Unique
    private boolean bcl_isShears;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void bcl_initShears(ItemPredicate itemPredicate, CallbackInfo ci) {
        if (this.predicate != null) {
            final Set<Item> items = ((ItemPredicateAccessor) this.predicate).bcl_getItems();
            bcl_isShears = items != null && items.size() == 1 && items.contains(Items.SHEARS);
        }
    }

    @Inject(method = "test(Lnet/minecraft/world/level/storage/loot/LootContext;)Z", at = @At("HEAD"), cancellable = true)
    private void bcl_isShears(LootContext lootContext, CallbackInfoReturnable<Boolean> cir) {
        if (bcl_isShears) {
            final ItemStack tool = lootContext.getParamOrNull(LootContextParams.TOOL);
            if (tool != null && tool.is(CommonItemTags.SHEARS)) {
                cir.setReturnValue(true);
            }
        }
    }
}
