package org.betterx.bclib.furniture.block;

import org.betterx.bclib.behaviours.BehaviourHelper;
import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.client.models.BCLModels;
import org.betterx.bclib.util.BlocksHelper;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;
import org.betterx.wover.loot.api.LootLookupProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public abstract class BaseChair extends AbstractChair {
    private static final VoxelShape SHAPE_BOTTOM = box(3, 0, 3, 13, 16, 13);
    private static final VoxelShape SHAPE_TOP = box(3, 0, 3, 13, 6, 13);
    private static final VoxelShape COLLIDER = box(3, 0, 3, 13, 10, 13);
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public final Block clothMaterial;

    public BaseChair(Block baseMaterial, Block clothMaterial) {
        super(baseMaterial, 10);
        this.clothMaterial = Objects.requireNonNull(
                clothMaterial,
                "Chair cloth material cannot be null (" + baseMaterial.getDescriptionId() + ")"
        );
        this.registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(TOP, false));
    }

    public BaseChair(Block baseMaterial, Block clothMaterial, BlockBehaviour.Properties settings) {
        super(baseMaterial, settings, 10);
        this.clothMaterial = Objects.requireNonNull(
                clothMaterial,
                "Chair cloth material cannot be null (" + baseMaterial.getDescriptionId() + ")"
        );
        this.registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(TOP, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager) {
        stateManager.add(FACING, TOP);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        return state.getValue(TOP) ? SHAPE_TOP : SHAPE_BOTTOM;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter view,
            BlockPos pos,
            CollisionContext ePos
    ) {
        return state.getValue(TOP) ? Shapes.empty() : COLLIDER;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        if (state.getValue(TOP))
            return true;
        BlockState up = world.getBlockState(pos.above());
        return up.isAir() || (up.getBlock() == this && up.getValue(TOP));
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.isClientSide())
            BlocksHelper.setWithUpdate(world, pos.above(), state.setValue(TOP, true));
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess scheduledTickAccess,
            BlockPos pos,
            Direction neighborDirection,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource randomSource
    ) {
        if (state.getValue(TOP)) {
            return level.getBlockState(pos.below()).getBlock() == this ? state : Blocks.AIR.defaultBlockState();
        } else {
            return level.getBlockState(pos.above()).getBlock() == this ? state : Blocks.AIR.defaultBlockState();
        }
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(
            BlockState state,
            Level world,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (state.getValue(TOP)) {
            pos = pos.below();
            state = world.getBlockState(pos);
        }
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (player.isCreative() && state.getValue(TOP) && world.getBlockState(pos.below()).getBlock() == this) {
            world.setBlockAndUpdate(pos.below(), Blocks.AIR.defaultBlockState());
        }
        return super.playerWillDestroy(world, pos, state, player);
    }

    public static class Wood extends BaseChair implements BehaviourWood {
        public Wood(Block baseMaterial, Block clothMaterial) {
            super(baseMaterial, clothMaterial);
        }

        public Wood(Block baseMaterial, Block clothMaterial, BlockBehaviour.Properties settings) {
            super(baseMaterial, clothMaterial, settings);
        }
    }

    public static class Stone extends BaseChair implements BehaviourStone {
        public Stone(Block baseMaterial, Block clothMaterial) {
            super(baseMaterial, clothMaterial);
        }

        public Stone(Block baseMaterial, Block clothMaterial, BlockBehaviour.Properties settings) {
            super(baseMaterial, clothMaterial, settings);
        }
    }

    public static class Metal extends BaseChair implements BehaviourMetal {
        public Metal(Block baseMaterial, Block clothMaterial) {
            super(baseMaterial, clothMaterial);
        }

        public Metal(Block baseMaterial, Block clothMaterial, BlockBehaviour.Properties settings) {
            super(baseMaterial, clothMaterial, settings);
        }
    }

    public static BaseChair from(Block baseMaterial, Block clothMaterial) {
        return BehaviourHelper.from(
                baseMaterial,
                (b) -> new BaseChair.Wood(b, clothMaterial),
                (b) -> new BaseChair.Stone(b, clothMaterial),
                (b) -> new BaseChair.Metal(b, clothMaterial)
        );
    }

    public static BaseChair from(Block baseMaterial, Block clothMaterial, BlockBehaviour.Properties settings) {
        return BehaviourHelper.from(
                baseMaterial,
                (b) -> new BaseChair.Wood(b, clothMaterial, settings),
                (b) -> new BaseChair.Stone(b, clothMaterial, settings),
                (b) -> new BaseChair.Metal(b, clothMaterial, settings)
        );
    }

    /**
     * Generates the block model for a chair, using {@code baseMaterial}/{@code clothMaterial}'s textures.
     *
     * @param generator The generator helper to emit the blockstate/model through
     * @param chairBlock The chair block to generate the model for
     */
    @Environment(EnvType.CLIENT)
    public static void provideBlockModel(WoverBlockModelGenerators generator, BaseChair chairBlock) {
        BCLModels.createChairBlockModel(generator, chairBlock, chairBlock.baseMaterial, chairBlock.clothMaterial);
    }

    @Override
    public LootTable.Builder registerBlockLoot(
            @NotNull ResourceLocation location,
            @NotNull LootLookupProvider provider,
            @NotNull ResourceKey<LootTable> tableKey
    ) {
        var bottomShape = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(this)
                .setProperties(net.minecraft.advancements.critereon.StatePropertiesPredicate.Builder
                        .properties()
                        .hasProperty(TOP, false));
        return LootTable
                .lootTable()
                .withPool(LootPool
                        .lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(this.asItem()))
                        .when(bottomShape)
                );
    }
}
