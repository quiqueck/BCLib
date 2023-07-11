package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.blockentities.BaseBarrelBlockEntity;
import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.interfaces.BlockModelProvider;
import org.betterx.bclib.interfaces.TagProvider;
import org.betterx.bclib.registry.BaseBlockEntities;
import org.betterx.worlds.together.tag.v3.CommonBlockTags;
import org.betterx.worlds.together.tag.v3.CommonItemTags;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public abstract class BaseBarrelBlock extends BarrelBlock implements BlockModelProvider, TagProvider, DropSelfLootProvider<BaseBarrelBlock> {
    BaseBarrelBlock(Block source) {
        this(Properties.copy(source).noOcclusion());
    }

    BaseBarrelBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return BaseBlockEntities.BARREL.create(blockPos, blockState);
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BaseBarrelBlockEntity) {
                player.openMenu((BaseBarrelBlockEntity) blockEntity);
                player.awardStat(Stats.OPEN_BARREL);
                PiglinAi.angerNearbyPiglins(player, true);
            }

            return InteractionResult.CONSUME;
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof BaseBarrelBlockEntity) {
            ((BaseBarrelBlockEntity) blockEntity).recheckOpen();
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (itemStack.hasCustomHoverName()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof BaseBarrelBlockEntity) {
                ((BaseBarrelBlockEntity) blockEntity).setCustomName(itemStack.getHoverName());
            }
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation blockId) {
        return getBlockModel(blockId, defaultBlockState());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public @Nullable BlockModel getBlockModel(ResourceLocation blockId, BlockState blockState) {
        Optional<String> pattern;
        if (blockState.getValue(OPEN)) {
            pattern = PatternsHelper.createJson(BasePatterns.BLOCK_BARREL_OPEN, blockId);
        } else {
            pattern = PatternsHelper.createJson(BasePatterns.BLOCK_BOTTOM_TOP, blockId);
        }
        return ModelsHelper.fromPattern(pattern);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public UnbakedModel getModelVariant(
            ResourceLocation stateId,
            BlockState blockState,
            Map<ResourceLocation, UnbakedModel> modelCache
    ) {
        String open = blockState.getValue(OPEN) ? "_open" : "";
        ResourceLocation modelId = new ResourceLocation(stateId.getNamespace(), "block/" + stateId.getPath() + open);
        registerBlockModel(stateId, modelId, blockState, modelCache);
        Direction facing = blockState.getValue(FACING);
        BlockModelRotation rotation = BlockModelRotation.X0_Y0;
        switch (facing) {
            case NORTH:
                rotation = BlockModelRotation.X90_Y0;
                break;
            case EAST:
                rotation = BlockModelRotation.X90_Y90;
                break;
            case SOUTH:
                rotation = BlockModelRotation.X90_Y180;
                break;
            case WEST:
                rotation = BlockModelRotation.X90_Y270;
                break;
            case DOWN:
                rotation = BlockModelRotation.X180_Y0;
                break;
            default:
                break;
        }
        return ModelsHelper.createMultiVariant(modelId, rotation.getRotation(), false);
    }

    @Override
    public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
        blockTags.add(CommonBlockTags.BARREL);
        itemTags.add(CommonItemTags.BARREL);
    }

    public static class Wood extends BaseBarrelBlock implements BehaviourWood {
        public Wood(Block source) {
            super(source);
        }

        public Wood(Properties properties) {
            super(properties);
        }

        @Override
        public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
            super.addTags(blockTags, itemTags);
            blockTags.add(CommonBlockTags.WOODEN_BARREL);
            itemTags.add(CommonItemTags.WOODEN_BARREL);
        }
    }

    public static BaseBarrelBlock from(Block source) {
        return new Wood(source);
    }
}
