package org.betterx.bclib.api.v3.bonemeal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;

import java.util.HashMap;
import java.util.Map;

abstract class BlockSpreader implements BonemealBlockSpreader {
    protected abstract boolean isValidSource(BlockState state);
    protected abstract boolean hasCustomBehaviour(BlockState state);

    public boolean isValidBonemealSpreadTarget(
            BlockGetter blockGetter,
            BlockPos blockPos,
            BlockState blockState,
            boolean bl
    ) {
        if (!canSpreadAt(blockGetter, blockPos)) {
            return false;
        } else {
            for (BlockPos testPos : BlockPos.betweenClosed(
                    blockPos.offset(-1, -1, -1),
                    blockPos.offset(1, 1, 1)
            )) {
                BlockState state = blockGetter.getBlockState(testPos);
                if (isValidSource(state))
                    if (hasCustomBehaviour(state))
                        return true;
            }
            return false;
        }
    }

    public boolean performBonemealSpread(
            ServerLevel serverLevel,
            RandomSource randomSource,
            BlockPos blockPos,
            BlockState blockState
    ) {
        final Map<BlockState, Integer> sourceBlocks = new HashMap<>();

        for (BlockPos testPos : BlockPos.betweenClosed(
                blockPos.offset(-1, -1, -1),
                blockPos.offset(1, 1, 1)
        )) {
            BlockState state = serverLevel.getBlockState(testPos);
            if (isValidSource(state)) {
                sourceBlocks.compute(state, (k, v) -> {
                    if (v == null) return 1;
                    return v + 1;
                });
            }
        }

        SimpleWeightedRandomList.Builder<BlockState> builder = new SimpleWeightedRandomList.Builder<>();
        for (Map.Entry<BlockState, Integer> e : sourceBlocks.entrySet()) {
            builder.add(e.getKey(), e.getValue());
        }
        WeightedStateProvider provider = new WeightedStateProvider(builder.build());

        BlockState bl = provider.getState(randomSource, blockPos);
        if (bl != null) {
            serverLevel.setBlock(blockPos, bl, 3);
            return true;
        }


        return false;
    }
}
