package org.betterx.bclib.blocks;

import org.betterx.bclib.client.sound.BlockSounds;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;
import org.betterx.wover.loot.api.BlockLootProvider;
import org.betterx.wover.loot.api.LootLookupProvider;
import org.betterx.wover.tag.api.TagManager;
import org.betterx.wover.tag.api.predefined.MineableTags;

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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class BaseTerrainBlock extends BaseBlock implements BlockLootProvider, BlockModelProvider {
    private final Block baseBlock;
    private Block pathBlock;

    public BaseTerrainBlock(Block baseBlock, MapColor color) {
        super(Properties
                .ofFullCopy(baseBlock)
                .mapColor(color)
                .sound(BlockSounds.TERRAIN_SOUND)
                .randomTicks()
        );
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
                    player.getMainHandItem().hurtAndBreak(1, (ServerLevel) level, (ServerPlayer) player, i -> {
                    });
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
        BlockPos blockPos = pos.above();
        BlockState blockState = worldView.getBlockState(blockPos);
        if (blockState.is(Blocks.SNOW) && blockState.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        } else if (blockState.getFluidState().getAmount() == 8) {
            return false;
        } else {
            int i = LightEngine.getLightBlockInto(
                    worldView,
                    state,
                    pos,
                    blockState,
                    blockPos,
                    Direction.UP,
                    blockState.getLightBlock(worldView, blockPos)
            );
            return i < 5;
        }
    }

    @Override
    public void provideBlockModels(WoverBlockModelGenerators generator) {
        generator.createBlockTopSideBottom(getBaseBlock(), this, true);
    }

    @Override
    public LootTable.Builder registerBlockLoot(
            @NotNull ResourceLocation location,
            @NotNull LootLookupProvider provider,
            @NotNull ResourceKey<LootTable> tableKey
    ) {
        return provider.dropWithSilkTouch(this, getBaseBlock(), ConstantValue.exactly(1));
    }
}
