package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.BehaviourBuilders;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public abstract class UnderwaterPlantWithAgeBlock extends UnderwaterPlantBlock {
    public static final IntegerProperty AGE = BlockProperties.AGE;

    public UnderwaterPlantWithAgeBlock(BlockBehaviour.Properties properties) {
        super(properties.randomTicks());
    }

    public UnderwaterPlantWithAgeBlock() {
        this(BehaviourBuilders
                .createWaterPlant()
                .sound(SoundType.WET_GRASS)
                .offsetType(BlockBehaviour.OffsetType.XZ)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager) {
        stateManager.add(AGE);
    }

    public abstract void grow(WorldGenLevel world, RandomSource random, BlockPos pos);

    @Override
    public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
        if (random.nextInt(4) == 0) {
            int age = state.getValue(AGE);
            if (age < 3) {
                world.setBlockAndUpdate(pos, state.setValue(AGE, age + 1));
            } else {
                grow(world, random, pos);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        super.tick(state, world, pos, random);
        if (isBonemealSuccess(world, random, pos, state)) {
            performBonemeal(world, random, pos, state);
        }
    }
}
