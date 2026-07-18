package org.betterx.bclib.mixin.common;

import org.betterx.bclib.trait.block.SurvivesOnBlockTrait;
import org.betterx.bclib.trait.block.SurvivesOnSolidTrait;
import org.betterx.wover.block.api.trait.BlockTrait;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
 * check from that trait, with no per-block method override needed. A {@link SurvivesOnSolidTrait} instead
 * lets the block survive on any sturdy solid surface. Blocks without either trait fall through to
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
        final boolean hasBlockTrait = BlockTrait.hasRuntimeTrait(self, SurvivesOnBlockTrait.KEY);
        final boolean hasSolidTrait = BlockTrait.hasRuntimeTrait(self, SurvivesOnSolidTrait.KEY);
        if (hasBlockTrait || hasSolidTrait) {
            final boolean canPlace =
                    (hasBlockTrait && SurvivesOnBlockTrait.survivesOn(self, groundState))
                            || (hasSolidTrait && groundState.isFaceSturdy(getter, pos, Direction.UP));
            cir.setReturnValue(canPlace);
        }
    }
}
