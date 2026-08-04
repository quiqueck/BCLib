package org.betterx.bclib.mixin.common;

import org.betterx.bclib.api.v3.bonemeal.BonemealAPI;
import org.betterx.bclib.blocks.FeatureSaplingBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoneMealItem.class)
public class BoneMealItemMixin {
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void bclib_onUse(UseOnContext context, CallbackInfoReturnable<InteractionResult> info) {
        Level level = context.getLevel();
        final BlockPos blockPos = context.getClickedPos();

        // UseOnContext permits a null player - nothing in vanilla reaches BoneMealItem.useOn without one
        // (dispensers go through DispenseItemBehavior instead), but a mod calling it directly would have
        // crashed the server here. Without a player there is no game mode to read, so treat it as not
        // creative and let the normal bone meal path handle it.
        final Player player = context.getPlayer();
        if (player != null && player.isCreative()) {
            if (BonemealAPI.INSTANCE.runSpreaders(context.getItemInHand(), level, blockPos, true)) {
                info.setReturnValue(level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
            }

            final BlockState blockState = level.getBlockState(blockPos);
            // growFeatureNow() rather than performBonemeal(): the latter routes through advanceTree,
            // which spends this application on the STAGE property and so needs a second click to
            // actually produce the tree. Creative already skips the isBonemealSuccess roll here, so it
            // should skip that step too and grow on the one click.
            if (level instanceof ServerLevel server
                    && blockState.getBlock() instanceof FeatureSaplingBlock<?, ?> sapling
            ) {
                if (sapling.growFeatureNow(server, blockPos, blockState, server.getRandom())) {
                    // Both halves of the feedback vanilla's BoneMealItem.useOn emits after a successful
                    // application: the vibration a sculk sensor listens for, and the growth particles
                    // and sound. Cancelling at HEAD skips them, so replay them here - gated on the grow
                    // actually happening, so a sapling with no room stays silent.
                    // 1.21.8 emits this from the player directly; ItemStack.causeUseVibration is a
                    // later addition.
                    player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
                    server.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, blockPos, 0);
                }
                info.setReturnValue(level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
            }
        }
    }

    @Inject(method = "growCrop", at = @At("HEAD"), cancellable = true)
    private static void bcl_growCrop(
            ItemStack itemStack,
            Level level,
            BlockPos blockPos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (BonemealAPI.INSTANCE.runSpreaders(itemStack, level, blockPos, false)) {
            cir.setReturnValue(true);
        }
    }
}