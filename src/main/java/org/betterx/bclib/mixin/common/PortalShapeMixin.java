package org.betterx.bclib.mixin.common;

import de.ambertation.wover.tag.api.predefined.CommonBlockTags;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour.StatePredicate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Lets blocks in {@code #c:nether_pframe} count as nether portal frame.
 * <p>
 * Uses {@code @WrapOperation} instead of {@code @Redirect}: since Fabric's Mixin 0.17.4 {@code Redirect.at} is an
 * array, and older MixinExtras versions (Fabric Loader 0.19.4 and older, Forgified Fabric API with Sinytra
 * Connector) crash on an array {@code at} of a {@code @Redirect} (#250). {@code WrapOperation.at} has always been an
 * array, and wrapped calls chain with other mods instead of conflicting.
 */
@Mixin(PortalShape.class)
public class PortalShapeMixin {
    @Unique
    private static final String FRAME_TEST = "Lnet/minecraft/world/level/block/state/BlockBehaviour$StatePredicate;test(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z";

    @WrapOperation(method = "getDistanceUntilEdgeAboveFrame", at = @At(value = "INVOKE", target = FRAME_TEST))
    private static boolean be_getDistanceUntilEdgeAboveFrame(
            StatePredicate statePredicate,
            BlockState blockState,
            BlockGetter blockGetter,
            BlockPos blockPos,
            Operation<Boolean> original
    ) {
        return be_FRAME(statePredicate, blockState, blockGetter, blockPos, original);
    }

    @WrapOperation(method = "hasTopFrame", at = @At(value = "INVOKE", target = FRAME_TEST))
    private static boolean be_hasTopFrame(
            StatePredicate statePredicate,
            BlockState blockState,
            BlockGetter blockGetter,
            BlockPos blockPos,
            Operation<Boolean> original
    ) {
        return be_FRAME(statePredicate, blockState, blockGetter, blockPos, original);
    }

    @WrapOperation(method = "getDistanceUntilTop", at = @At(value = "INVOKE", target = FRAME_TEST))
    private static boolean be_getDistanceUntilTop(
            StatePredicate statePredicate,
            BlockState blockState,
            BlockGetter blockGetter,
            BlockPos blockPos,
            Operation<Boolean> original
    ) {
        return be_FRAME(statePredicate, blockState, blockGetter, blockPos, original);
    }

    @Unique
    private static boolean be_FRAME(
            StatePredicate frame,
            BlockState state,
            BlockGetter getter,
            BlockPos pos,
            Operation<Boolean> original
    ) {
        return state.is(CommonBlockTags.NETHER_PORTAL_FRAME) || original.call(frame, state, getter, pos);
    }
}
