package org.betterx.bclib.mixin.client;

import org.betterx.bclib.interfaces.SurvivesOnSpecialGround;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import org.jetbrains.annotations.Nullable;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "appendHoverText", at = @At("HEAD"))
    void bclib_appendSurvivalBlock(
            ItemStack itemStack,
            @Nullable BlockGetter blockGetter,
            List<Component> list,
            TooltipFlag tooltipFlag,
            CallbackInfo ci
    ) {
        if (this instanceof SurvivesOnSpecialGround surv) {
            SurvivesOnSpecialGround.appendHoverText(surv, list);
        }
    }
}
