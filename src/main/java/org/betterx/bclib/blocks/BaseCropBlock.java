package org.betterx.bclib.blocks;

import org.betterx.bclib.trait.block.SurvivesOnBlockTrait;
import org.betterx.bclib.util.BlocksHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Passive growth ({@link #performBonemeal} on a 1-in-8 chance) runs from {@link #randomTick}, so a block
 * registered with this class needs a random-ticking {@code Properties} to ever grow on its own - attach
 * {@code org.betterx.bclib.trait.block.RandomTicksTrait} (or a more specific trait that already calls
 * {@code definition.randomTicks()}) at the registration site. This class deliberately does not flip that flag
 * itself: constructor {@code Properties} mutation is reserved for traits, so the setting stays visible and
 * overridable where the block is registered.
 */
public class BaseCropBlock extends BasePlantBlock {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);
    private static final VoxelShape SHAPE = box(2, 0, 2, 14, 14, 14);

    private final SurvivesOnBlockTrait survivesOn;
    private final Item drop;

    protected BaseCropBlock(BlockBehaviour.Properties properties, Item drop, Block... terrain) {
        super(properties);
        this.drop = drop;
        this.survivesOn = SurvivesOnBlockTrait.withBlocks(terrain);
        this.registerDefaultState(defaultBlockState().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager) {
        stateManager.add(AGE);
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int age = state.getValue(AGE);
        if (age < 3) {
            BlocksHelper.setWithUpdate(level, pos, state.setValue(AGE, age + 1));
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < 3;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < 3;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        super.randomTick(state, world, pos, random);
        if (isBonemealSuccess(world, random, pos, state) && random.nextInt(8) == 0) {
            performBonemeal(world, random, pos, state);
        }
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        return SHAPE;
    }

    @Override
    protected boolean isTerrain(BlockState state) {
        return survivesOn.isSurvivable(state);
    }
}
