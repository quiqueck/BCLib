package org.betterx.bclib.mixin.client;

import org.betterx.bclib.trait.block.SurvivesOnBlockTrait;
import org.betterx.bclib.trait.block.SurvivesOnSolidTrait;

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
        // Blocks get the "can be placed on ..." hint from whatever SurvivesOnBlockTrait(s) are attached to
        // them, so there is no need for a block class to implement any special interface just for the
        // tooltip.
        if (itemStack.getItem() instanceof BlockItem blockItem) {
            SurvivesOnBlockTrait.appendHoverText(blockItem.getBlock(), consumer);
            SurvivesOnSolidTrait.appendHoverText(blockItem.getBlock(), consumer);
        }
    }
}
