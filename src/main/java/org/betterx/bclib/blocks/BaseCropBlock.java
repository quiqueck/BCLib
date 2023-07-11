package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.BehaviourBuilders;
import org.betterx.bclib.interfaces.SurvivesOnBlocks;
import org.betterx.bclib.util.BlocksHelper;
import org.betterx.bclib.util.LootUtil;
import org.betterx.bclib.util.MHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

public class BaseCropBlock extends BasePlantBlock implements SurvivesOnBlocks {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);
    private static final VoxelShape SHAPE = box(2, 0, 2, 14, 14, 14);

    private final List<Block> terrain;
    private final Item drop;

    public BaseCropBlock(Item drop, Block... terrain) {
        this(
                BehaviourBuilders.createPlant().randomTicks().sound(SoundType.CROP).offsetType(OffsetType.XZ),
                drop,
                terrain
        );
    }

    protected BaseCropBlock(BlockBehaviour.Properties properties, Item drop, Block... terrain) {
        super(properties);
        this.drop = drop;
        this.terrain = List.of(terrain);
        this.registerDefaultState(defaultBlockState().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager) {
        stateManager.add(AGE);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (state.getValue(AGE) < 3) {
            return Collections.singletonList(new ItemStack(this));
        }
        ItemStack tool = builder.getParameter(LootContextParams.TOOL);
        if (LootUtil.isCorrectTool(this, state, tool)) {
            int enchantment = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
            if (enchantment > 0) {
                int countSeeds = MHelper.randRange(Mth.clamp(1 + enchantment, 1, 3), 3, MHelper.RANDOM_SOURCE);
                int countDrops = MHelper.randRange(Mth.clamp(1 + enchantment, 1, 2), 2, MHelper.RANDOM_SOURCE);
                return Lists.newArrayList(new ItemStack(this, countSeeds), new ItemStack(drop, countDrops));
            }
        }
        int countSeeds = MHelper.randRange(1, 3, MHelper.RANDOM_SOURCE);
        int countDrops = MHelper.randRange(1, 2, MHelper.RANDOM_SOURCE);
        return Lists.newArrayList(new ItemStack(this, countSeeds), new ItemStack(drop, countDrops));
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int age = state.getValue(AGE);
        if (age < 3) {
            BlocksHelper.setWithUpdate(level, pos, state.setValue(AGE, age + 1));
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state, boolean isClient) {
        return state.getValue(AGE) < 3;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < 3;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        super.tick(state, world, pos, random);
        if (isBonemealSuccess(world, random, pos, state) && random.nextInt(8) == 0) {
            performBonemeal(world, random, pos, state);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        return SHAPE;
    }

    @Override
    public List<Block> getSurvivableBlocks() {
        return terrain;
    }

    @Override
    public boolean isTerrain(BlockState state) {
        return SurvivesOnBlocks.super.isTerrain(state);
    }
}
