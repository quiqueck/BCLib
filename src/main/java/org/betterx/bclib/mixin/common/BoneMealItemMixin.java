package org.betterx.bclib.mixin.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoneMealItem.class)
public class BoneMealItemMixin {
    @Unique
    private static final MutableBlockPos BCLIB_BLOCK_POS = new MutableBlockPos();

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void bclib_onUse(UseOnContext context, CallbackInfoReturnable<InteractionResult> info) {
        Level world = context.getLevel();
        final BlockPos blockPos = context.getClickedPos();
        if (!world.isClientSide()) {

            if (!context.getPlayer().isCreative()) {
                context.getItemInHand().shrink(1);
            }

        }
    }

    @Inject(method = "growCrop", at = @At("HEAD"), cancellable = true)
    private static void bcl_growCrop(
            ItemStack itemStack,
            Level level,
            BlockPos blockPos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (org.betterx.bclib.api.v3.bonemeal.BonemealAPI
                .INSTANCE
                .runSpreaders(itemStack, level, blockPos)
        ) {
            cir.setReturnValue(true);
        }
    }
}