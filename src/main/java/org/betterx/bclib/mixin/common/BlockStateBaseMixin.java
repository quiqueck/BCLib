package org.betterx.bclib.mixin.common;

import org.betterx.bclib.util.MethodReplace;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(BlockStateBase.class)
public class BlockStateBaseMixin {
    @Inject(method = "is(Lnet/minecraft/world/level/block/Block;)Z", at = @At("HEAD"), cancellable = true)
    private void bclib_replaceFunction(Block block, CallbackInfoReturnable<Boolean> info) {
        Function<BlockStateBase, Boolean> replacement = MethodReplace.getBlockReplace(block);
        if (replacement != null) {
            info.setReturnValue(replacement.apply(BlockStateBase.class.cast(this)));
        }
    }
}
