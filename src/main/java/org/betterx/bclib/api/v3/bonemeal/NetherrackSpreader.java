package org.betterx.bclib.api.v3.bonemeal;

import org.betterx.bclib.util.WeightedList;
import org.betterx.worlds.together.tag.v3.CommonBlockTags;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class NetherrackSpreader implements BonemealSpreader {
    public static final NetherrackSpreader INSTANCE = new NetherrackSpreader();

    public boolean isValidBonemealSpreadTarget(
            BlockGetter blockGetter,
            BlockPos blockPos,
            BlockState blockState,
            boolean bl
    ) {
        if (!blockGetter.getBlockState(blockPos.above()).propagatesSkylightDown(blockGetter, blockPos)) {
            return false;
        } else {
            for (BlockPos testPos : BlockPos.betweenClosed(
                    blockPos.offset(-1, -1, -1),
                    blockPos.offset(1, 1, 1)
            )) {
                if (blockGetter.getBlockState(testPos).is(CommonBlockTags.NETHERRACK_SPREADABLE))
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
        Map<Block, Integer> sourceBlocks = new HashMap<>();
        boolean hasNonVanilla = false;
        for (BlockPos testPos : BlockPos.betweenClosed(
                blockPos.offset(-1, -1, -1),
                blockPos.offset(1, 1, 1)
        )) {
            BlockState state = serverLevel.getBlockState(testPos);
            if (serverLevel.getBlockState(testPos).is(CommonBlockTags.NETHERRACK_SPREADABLE)) {
                sourceBlocks.compute(state.getBlock(), (k, v) -> {
                    if (v == null) return 1;
                    return v + 1;
                });
            }

            if (!state.is(Blocks.WARPED_NYLIUM) && state.is(Blocks.CRIMSON_NYLIUM)) {
                hasNonVanilla = true;
            }
        }

        if (hasNonVanilla) {
            WeightedList<Block> list = new WeightedList<>();
            for (Map.Entry<Block, Integer> e : sourceBlocks.entrySet()) {
                list.add(e.getKey(), e.getValue());
            }
            Block bl = list.get(randomSource);
            if (bl != null) {
                serverLevel.setBlock(blockPos, bl.defaultBlockState(), 3);
                return true;
            }
        }


        return false;
    }
}
