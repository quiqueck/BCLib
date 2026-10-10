package org.betterx.bclib.mixin.common;

import de.ambertation.wover.tag.api.predefined.CommonBlockTags;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Mixin(PortalShape.class)
public class PortalShapeMixin {
    // 26.3 turned PortalShape.FRAME from a BlockBehaviour.StatePredicate into a plain Predicate<BlockState>,
    // so the frame checks are now Predicate#test(Object) calls. Every Predicate#test call in these three
    // methods is a FRAME check.
    @Redirect(method = "getDistanceUntilEdgeAboveFrame", at = @At(value = "INVOKE", target = "Ljava/util/function/Predicate;test(Ljava/lang/Object;)Z"))
    private static boolean be_getDistanceUntilEdgeAboveFrame(Predicate<BlockState> frame, Object state) {
        return be_FRAME(frame, (BlockState) state);
    }

    @Redirect(method = "hasTopFrame", at = @At(value = "INVOKE", target = "Ljava/util/function/Predicate;test(Ljava/lang/Object;)Z"))
    private static boolean be_hasTopFrame(Predicate<BlockState> frame, Object state) {
        return be_FRAME(frame, (BlockState) state);
    }

    @Redirect(method = "getDistanceUntilTop", at = @At(value = "INVOKE", target = "Ljava/util/function/Predicate;test(Ljava/lang/Object;)Z"))
    private static boolean be_getDistanceUntilTop(Predicate<BlockState> frame, Object state) {
        return be_FRAME(frame, (BlockState) state);
    }

    @Unique
    private static boolean be_FRAME(Predicate<BlockState> frame, BlockState state) {
        return state.is(CommonBlockTags.NETHER_PORTAL_FRAME) || frame.test(state);
    }
}
