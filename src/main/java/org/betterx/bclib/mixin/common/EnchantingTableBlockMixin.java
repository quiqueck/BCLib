package org.betterx.bclib.mixin.common;

import org.betterx.worlds.together.tag.v3.CommonBlockTags;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnchantmentTableBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentTableBlock.class)
public abstract class EnchantingTableBlockMixin extends Block {
    public EnchantingTableBlockMixin(Properties settings) {
        super(settings);
    }

    @Inject(method = "isValidBookShelf(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    private static void bclib_isBookshelf(
            Level level,
            BlockPos tablePos,
            BlockPos delta,
            CallbackInfoReturnable<Boolean> info
    ) {
        if (level.getBlockState(tablePos.offset(delta)).is(CommonBlockTags.BOOKSHELVES)
                && level.isEmptyBlock(tablePos.offset(delta.getX() / 2, delta.getY(), delta.getZ() / 2))) {
            info.setReturnValue(true);
        }
    }
}
