package org.betterx.bclib.blocks;

import org.betterx.worlds.together.tag.v3.MineableTags;
import org.betterx.worlds.together.tag.v3.TagManager;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;


public class StripableBarkBlock extends BaseBarkBlock {
    private final Block striped;

    public StripableBarkBlock(MapColor color, Block striped) {
        super(Properties.copy(striped).mapColor(color));
        this.striped = striped;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(
            BlockState state,
            Level world,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (TagManager.isToolWithMineableTag(player.getMainHandItem(), MineableTags.AXE)) {
            world.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!world.isClientSide) {
                world.setBlock(
                        pos,
                        striped.defaultBlockState()
                               .setValue(AXIS, state.getValue(AXIS)),
                        11
                );
                if (!player.isCreative()) {
                    player.getMainHandItem().hurt(1, world.random, (ServerPlayer) player);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }
}
