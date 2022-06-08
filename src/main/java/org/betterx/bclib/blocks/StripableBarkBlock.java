package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v2.tag.NamedMineableTags;
import org.betterx.bclib.api.v2.tag.TagAPI;

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
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;

public class StripableBarkBlock extends BaseBarkBlock {
    private final Block striped;

    public StripableBarkBlock(MaterialColor color, Block striped) {
        super(FabricBlockSettings.copyOf(striped).color(color));
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
        if (TagAPI.isToolWithMineableTag(player.getMainHandItem(), NamedMineableTags.AXE)) {
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
