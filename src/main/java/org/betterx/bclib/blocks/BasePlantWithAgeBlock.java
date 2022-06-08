package org.betterx.bclib.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;

import java.util.function.Function;

public abstract class BasePlantWithAgeBlock extends BasePlantBlock {
    public static final IntegerProperty AGE = BlockProperties.AGE;

    public BasePlantWithAgeBlock() {
        this(p -> p);
    }

    public BasePlantWithAgeBlock(Function<Properties, Properties> propMod) {
        this(
                propMod.apply(FabricBlockSettings.of(Material.PLANT)
                                                 .sound(SoundType.GRASS)
                                                 .randomTicks()
                                                 .noCollission())
        );
    }

    public BasePlantWithAgeBlock(Properties settings) {
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
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        super.tick(state, world, pos, random);
        if (random.nextInt(8) == 0) {
            performBonemeal(world, random, pos, state);
        }
    }
}
