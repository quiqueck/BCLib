package org.betterx.bclib.interfaces;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public interface AirSelectionItem {
    default boolean renderAirSelection() {
        return true;
    }

    default int airSelectionColor() {
        return 0xBFF6FA70;
    }

    default BlockHitResult getAirSelectionHit(Level level, Player player) {
        if (renderAirSelection()) {
            final var vec = new Vec3(0, 0, 1)
                    .xRot(-player.getXRot() * Mth.DEG_TO_RAD)
                    .yRot(-player.getYHeadRot() * Mth.DEG_TO_RAD);

            return level.isBlockInLine(new ClipBlockStateContext(
                    player.getEyePosition(),
                    player.getEyePosition().add(vec.scale(4.9)),
                    BlockBehaviour.BlockStateBase::isAir
            ));
        }
        return null;
    }

    default InteractionResultHolder<ItemStack> useOnAir(Level level, Player player, InteractionHand interactionHand) {
        final BlockHitResult hit = getAirSelectionHit(level, player);

        if (hit != null) {
            var result = this.useOn(new UseOnContext(player, interactionHand, hit));

            if (result == InteractionResult.SUCCESS)
                return InteractionResultHolder.success(player.getItemInHand(interactionHand));
            else if (result == InteractionResult.FAIL)
                return InteractionResultHolder.fail(player.getItemInHand(interactionHand));
            else if (result == InteractionResult.PASS)
                return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
            else if (result == InteractionResult.CONSUME)
                return InteractionResultHolder.consume(player.getItemInHand(interactionHand));
        }

        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }

    InteractionResult useOn(UseOnContext useOnContext);
}
