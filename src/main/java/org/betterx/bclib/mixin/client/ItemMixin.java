package org.betterx.bclib.mixin.client;

import org.betterx.bclib.interfaces.SurvivesOnSpecialGround;
import org.betterx.bclib.trait.block.SurvivesOnBlockTrait;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "appendHoverText", at = @At("HEAD"))
    void bclib_appendSurvivalBlock(
            ItemStack itemStack,
            Item.TooltipContext tooltipContext,
            TooltipDisplay tooltipDisplay,
            Consumer<Component> consumer,
            TooltipFlag tooltipFlag,
            CallbackInfo ci
    ) {
        // The SurvivesOn* hierarchy is implemented by BLOCKS, never by items, so this check on its own can
        // not match and the "can be placed on ..." hint has never actually rendered. Route through the
        // block item as well, which both fixes that and lets a block get the hint from a
        // SurvivesOnBlockTrait instead of having to implement SurvivesOnSpecialGround for the tooltip alone.
        if (this instanceof SurvivesOnSpecialGround surv) {
            SurvivesOnSpecialGround.appendHoverText(surv, consumer);
        } else if (itemStack.getItem() instanceof BlockItem blockItem) {
            if (blockItem.getBlock() instanceof SurvivesOnSpecialGround surv) {
                SurvivesOnSpecialGround.appendHoverText(surv, consumer);
            } else {
                SurvivesOnBlockTrait.appendHoverText(blockItem.getBlock(), consumer);
            }
        }
    }
}
