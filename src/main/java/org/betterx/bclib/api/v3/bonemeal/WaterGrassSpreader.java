package org.betterx.bclib.api.v3.bonemeal;

import org.betterx.bclib.util.BlocksHelper;
import org.betterx.bclib.util.WeightedList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class WaterGrassSpreader extends TaggedBonemealBlockSpreader {
    public WaterGrassSpreader(TagKey<Block> sourceBlockTag) {
        super(sourceBlockTag);
    }

    @Override
    public boolean canSpreadAt(BlockGetter blockGetter, BlockPos blockPos) {
        final BlockState stateAbove = blockGetter.getBlockState(blockPos.above());
        return !stateAbove.getFluidState().isEmpty() && stateAbove.is(Blocks.WATER);
    }

    @Override
    public boolean isValidBonemealSpreadTarget(
            BlockGetter blockGetter,
            BlockPos blockPos,
            BlockState blockState,
            boolean bl
    ) {
        return canSpreadAt(blockGetter, blockPos);
    }

    @Override
    public boolean performBonemealSpread(
            ServerLevel level,
            RandomSource randomSource,
            BlockPos pos,
            BlockState blockState
    ) {
        final BlockPos.MutableBlockPos currentPos = new BlockPos.MutableBlockPos();
        final WeightedList<Holder<Block>> sourceSet = new WeightedList<>();
        BuiltInRegistries.BLOCK.getTagOrEmpty(blockTag).forEach(c -> sourceSet.add(c, 1));

        int y1 = pos.getY() + 3;
        int y2 = pos.getY() - 3;
        boolean result = false;
        for (byte i = 0; i < 64; i++) {
            int x = (int) (pos.getX() + level.random.nextGaussian() * 2);
            int z = (int) (pos.getZ() + level.random.nextGaussian() * 2);
            currentPos.setX(x);
            currentPos.setZ(z);
            for (int y = y1; y >= y2; y--) {
                currentPos.setY(y);
                BlockPos down = currentPos.below();
                if (BlocksHelper.isFluid(level.getBlockState(currentPos))
                        && !BlocksHelper.isFluid(level.getBlockState(down))) {
                    Holder<Block> grass = sourceSet.get(randomSource);
                    if (grass.isBound()) {
                        if (grass.value().canSurvive(grass.value().defaultBlockState(), level, currentPos)) {
                            level.setBlock(currentPos, grass.value().defaultBlockState(), BlocksHelper.SET_SILENT);
                            result = true;
                        }
                    }
//                    BiConsumer<Level, BlockPos> grass = getWaterGrassState(level, down);
//                    if (grass != null) {
//                        grass.accept(level, currentPos);
//                        result = true;
//                    }

                    break;
                }
            }
        }
        return result;
    }
}
