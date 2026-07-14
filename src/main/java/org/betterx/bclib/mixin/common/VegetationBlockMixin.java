package org.betterx.bclib.mixin.common;

import org.betterx.bclib.trait.block.SurvivesOnBlockTrait;
import org.betterx.wover.block.api.trait.BlockTrait;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes {@link SurvivesOnBlockTrait} self-sufficient: any {@link VegetationBlock} (plants, saplings, ...)
 * registered with a {@link SurvivesOnBlockTrait} derives its {@code mayPlaceOn}/{@code canSurvive} ground
 * check from that trait, with no per-block method override needed. Blocks without the trait fall through to
 * vanilla behaviour (or their own {@code mayPlaceOn} override, which - being a subclass override - bypasses
 * this injection through normal virtual dispatch).
 */
@Mixin(VegetationBlock.class)
public class VegetationBlockMixin {
    @Inject(method = "mayPlaceOn", at = @At("HEAD"), cancellable = true)
    private void bclib_survivesOnTrait(
            BlockState groundState,
            BlockGetter getter,
            BlockPos pos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        Block self = (Block) (Object) this;
        if (BlockTrait.hasRuntimeTrait(self, SurvivesOnBlockTrait.KEY)) {
            cir.setReturnValue(SurvivesOnBlockTrait.survivesOn(self, groundState));
        }
    }
}
