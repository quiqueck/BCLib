package org.betterx.bclib.blocks;

import de.ambertation.wover.tag.api.TagManager;
import de.ambertation.wover.tag.api.predefined.MineableTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class BaseTerrainBlock extends Block {
    private final Block baseBlock;
    private Block pathBlock;

    public BaseTerrainBlock(BlockBehaviour.Properties props, Block baseBlock) {
        super(props);
        this.baseBlock = baseBlock;
    }

    public void setPathBlock(Block roadBlock) {
        this.pathBlock = roadBlock;
    }

    public Block getBaseBlock() {
        return baseBlock;
    }

    @Override
    public InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (pathBlock != null && TagManager.isToolWithMineableTag(player.getMainHandItem(), MineableTags.SHOVEL)) {
            level.playSound(player, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!level.isClientSide) {
                level.setBlockAndUpdate(pos, pathBlock.defaultBlockState());
                if (!player.isCreative()) {
                    player.getMainHandItem().hurtAndBreak(
                            1, (ServerLevel) level, (ServerPlayer) player, i -> {
                            }
                    );
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (random.nextInt(16) == 0 && !canStay(state, world, pos)) {
            world.setBlockAndUpdate(pos, getBaseBlock().defaultBlockState());
        }
    }

    public boolean canStay(BlockState state, LevelReader worldView, BlockPos pos) {
        return willSurvive(state, worldView, pos);
    }

    public static boolean willSurvive(BlockState state, LevelReader worldView, BlockPos pos) {
        BlockState blockState = worldView.getBlockState(pos.above());
        if (blockState.is(Blocks.SNOW) && blockState.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        } else if (blockState.getFluidState().getAmount() == 8) {
            return false;
        } else {
            int i = LightEngine.getLightBlockInto(
                    state,
                    blockState,
                    Direction.UP,
                    blockState.getLightBlock()
            );
            return i < 5;
        }
    }
}
