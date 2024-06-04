package org.betterx.bclib.mixin.common.axe;

import org.betterx.bclib.interfaces.tools.AxeCanStrip;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(AxeItem.class)
public class AxeItemMixin {
    @Inject(method = "getStripped", at = @At("HEAD"), cancellable = true)
    void bclib_getStripped(BlockState blockState, CallbackInfoReturnable<Optional<BlockState>> cir) {
        final Block block = blockState.getBlock();
        if (block instanceof AxeCanStrip stripable) {
            cir.setReturnValue(Optional.of(stripable.strippedState(blockState)));
        }
    }
}
