package org.betterx.bclib.furniture.block;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.blocks.BaseBlockNotFull;
import org.betterx.bclib.furniture.entity.EntityChair;
import org.betterx.bclib.registry.BaseBlockEntities;
import org.betterx.bclib.util.BlocksHelper;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;
import org.betterx.wover.loot.api.BlockLootProvider;
import org.betterx.wover.loot.api.LootLookupProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractChair extends BaseBlockNotFull implements BlockModelProvider, BlockLootProvider {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public final Block baseMaterial;
    protected final float height;

    public static BlockBehaviour.Properties defaultFurnitureProperties(Block block) {
        return BlockBehaviour.Properties.ofFullCopy(block).noOcclusion();
    }

    public AbstractChair(Block baseMaterial, int height) {
        super(defaultFurnitureProperties(baseMaterial));
        this.height = (height - 3F) / 16F;
        this.baseMaterial = baseMaterial;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager) {
        stateManager.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public @NotNull ItemInteractionResult useItemOn(
            ItemStack itemStack,
            BlockState state,
            Level world,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (world.isClientSide) {
            return ItemInteractionResult.FAIL;
        } else {
            if (player.isPassenger() || player.isSpectator())
                return ItemInteractionResult.FAIL;


            Optional<EntityChair> active = getEntity(world, pos);
            EntityChair entity;

            if (active.isEmpty()) {
                entity = createEntity(state, world, pos);
            } else {
                entity = active.get();
                if (entity.isVehicle())
                    return ItemInteractionResult.FAIL;
            }

            if (entity != null) {
                float yaw = state.getValue(FACING).getOpposite().toYRot();
                player.startRiding(entity, true);
                player.setYBodyRot(yaw);
                player.setYHeadRot(yaw);
                return ItemInteractionResult.SUCCESS;
            }

            return ItemInteractionResult.FAIL;
        }
    }

    @Nullable
    private EntityChair createEntity(BlockState state, Level world, BlockPos pos) {
        BCLib.LOGGER.info("Creating Chair at " + pos + ", " + state);
        EntityChair entity;
        double px = pos.getX() + 0.5;
        double py = pos.getY() + height;
        double pz = pos.getZ() + 0.5;
        float yaw = state.getValue(FACING).getOpposite().toYRot();

        entity = BaseBlockEntities.CHAIR.create(world);
        entity.moveTo(px, py, pz, yaw, 0);
        entity.setNoGravity(true);
        entity.setSilent(true);
        entity.setInvisible(true);
        entity.setYHeadRot(yaw);
        entity.setYBodyRot(yaw);
        if (!world.addFreshEntity(entity)) {
            entity = null;
        }
        return entity;
    }

    private Optional<EntityChair> getEntity(Level level, BlockPos pos) {
        List<EntityChair> list = level.getEntitiesOfClass(
                EntityChair.class,
                new AABB(pos),
                entity -> true
        );
        if (list.isEmpty()) return Optional.empty();
        return Optional.of(list.get(0));
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return BlocksHelper.rotateHorizontal(state, rotation, FACING);
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return BlocksHelper.mirrorHorizontal(state, mirror, FACING);
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        super.onPlace(blockState, level, blockPos, blockState2, bl);
        BCLib.LOGGER.info("Created at " + blockPos + ", " + blockState + ", " + blockState2);
        if (blockState.hasProperty(BaseChair.TOP)) {
            if (blockState.getValue(BaseChair.TOP))
                return;
        }
        createEntity(blockState, level, blockPos);
    }

    @Override
    public LootTable.Builder registerBlockLoot(
            @NotNull ResourceLocation location,
            @NotNull LootLookupProvider provider,
            @NotNull ResourceKey<LootTable> tableKey
    ) {
        return provider.drop(this.asItem());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void provideBlockModels(WoverBlockModelGenerators generators) {

    }
}
