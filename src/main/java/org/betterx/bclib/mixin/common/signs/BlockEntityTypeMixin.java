package org.betterx.bclib.mixin.common.signs;

import org.betterx.bclib.blocks.BaseSignBlock;
import org.betterx.bclib.blocks.BaseWallSignBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityType.class)
public class BlockEntityTypeMixin {
    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    void bcl_isValid(BlockState blockState, CallbackInfoReturnable<Boolean> cir) {
        final BlockEntityType self = (BlockEntityType) (Object) this;
        if (self == BlockEntityType.SIGN) {
            final Block block = blockState.getBlock();
            if ((block instanceof BaseSignBlock) || (block instanceof BaseWallSignBlock)) {
                cir.setReturnValue(true);
            }
        }
    }
}
