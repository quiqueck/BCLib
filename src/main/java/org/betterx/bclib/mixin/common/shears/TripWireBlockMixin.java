package org.betterx.bclib.mixin.common.shears;

import org.betterx.bclib.items.tool.BaseShearsItem;
import org.betterx.bclib.util.MethodReplace;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TripWireBlock.class)
public class TripWireBlockMixin {
    @Inject(method = "playerWillDestroy", at = @At("HEAD"))
    private void bclib_isShears(
            Level level, BlockPos blockPos, BlockState blockState, Player player, CallbackInfoReturnable<BlockState> cir
    ) {
        MethodReplace.addItemReplace(Items.SHEARS, BaseShearsItem::isShear);
    }
}
