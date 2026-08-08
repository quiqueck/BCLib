package org.betterx.bclib.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Passive growth (age-up, then {@link #growAdult}) runs from {@link #randomTick}, so a block registered with
 * this class needs a random-ticking {@code Properties} to ever grow on its own - attach
 * {@code org.betterx.bclib.trait.block.RandomTicksTrait} (or a more specific trait that already calls
 * {@code definition.randomTicks()}) at the registration site. This class deliberately does not flip that flag
 * itself: constructor {@code Properties} mutation is reserved for traits, so the setting stays visible and
 * overridable where the block is registered.
 */
public abstract class BasePlantWithAgeBlock extends BasePlantBlock {
    public static final IntegerProperty AGE = BlockProperties.AGE;

    protected BasePlantWithAgeBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager) {
        stateManager.add(AGE);
    }

    public abstract void growAdult(WorldGenLevel world, RandomSource random, BlockPos pos);

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int age = state.getValue(AGE);
        if (age < 3) {
            level.setBlockAndUpdate(pos, state.setValue(AGE, age + 1));
        } else {
            growAdult(level, random, pos);
        }
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        super.randomTick(state, world, pos, random);
        if (random.nextInt(8) == 0) {
            performBonemeal(world, random, pos, state);
        }
    }
}
