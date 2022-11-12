package org.betterx.bclib.blocks;

import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.interfaces.BlockModelProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.level.storage.loot.LootContext;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public class BaseWallBlock extends WallBlock implements BlockModelProvider {
    private final Block parent;

    public BaseWallBlock(Block source) {
        super(Properties.copy(source).noOcclusion());
        this.parent = source;
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        return Collections.singletonList(new ItemStack(this));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation blockId) {
        ResourceLocation parentId = BuiltInRegistries.BLOCK.getKey(parent);
        Optional<String> pattern = PatternsHelper.createJson(BasePatterns.ITEM_WALL, parentId);
        return ModelsHelper.fromPattern(pattern);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public @Nullable BlockModel getBlockModel(ResourceLocation blockId, BlockState blockState) {
        ResourceLocation parentId = BuiltInRegistries.BLOCK.getKey(parent);
        String path = blockId.getPath();
        Optional<String> pattern = Optional.empty();
        if (path.endsWith("_post")) {
            pattern = PatternsHelper.createJson(BasePatterns.BLOCK_WALL_POST, parentId);
        }
        if (path.endsWith("_side")) {
            pattern = PatternsHelper.createJson(BasePatterns.BLOCK_WALL_SIDE, parentId);
        }
        if (path.endsWith("_side_tall")) {
            pattern = PatternsHelper.createJson(BasePatterns.BLOCK_WALL_SIDE_TALL, parentId);
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
        ResourceLocation postId = new ResourceLocation(stateId.getNamespace(), "block/" + stateId.getPath() + "_post");
        ResourceLocation sideId = new ResourceLocation(stateId.getNamespace(), "block/" + stateId.getPath() + "_side");
        ResourceLocation sideTallId = new ResourceLocation(
                stateId.getNamespace(),
                "block/" + stateId.getPath() + "_side_tall"
        );
        registerBlockModel(postId, postId, blockState, modelCache);
        registerBlockModel(sideId, sideId, blockState, modelCache);
        registerBlockModel(sideTallId, sideTallId, blockState, modelCache);

        ModelsHelper.MultiPartBuilder builder = ModelsHelper.MultiPartBuilder.create(stateDefinition);
        builder.part(sideId).setCondition(state -> state.getValue(NORTH_WALL) == WallSide.LOW).setUVLock(true).add();
        builder.part(sideId)
               .setCondition(state -> state.getValue(EAST_WALL) == WallSide.LOW)
               .setTransformation(BlockModelRotation.X0_Y90.getRotation())
               .setUVLock(true)
               .add();
        builder.part(sideId)
               .setCondition(state -> state.getValue(SOUTH_WALL) == WallSide.LOW)
               .setTransformation(BlockModelRotation.X0_Y180.getRotation())
               .setUVLock(true)
               .add();
        builder.part(sideId)
               .setCondition(state -> state.getValue(WEST_WALL) == WallSide.LOW)
               .setTransformation(BlockModelRotation.X0_Y270.getRotation())
               .setUVLock(true)
               .add();
        builder.part(sideTallId)
               .setCondition(state -> state.getValue(NORTH_WALL) == WallSide.TALL)
               .setUVLock(true)
               .add();
        builder.part(sideTallId)
               .setCondition(state -> state.getValue(EAST_WALL) == WallSide.TALL)
               .setTransformation(BlockModelRotation.X0_Y90.getRotation())
               .setUVLock(true)
               .add();
        builder.part(sideTallId)
               .setCondition(state -> state.getValue(SOUTH_WALL) == WallSide.TALL)
               .setTransformation(BlockModelRotation.X0_Y180.getRotation())
               .setUVLock(true)
               .add();
        builder.part(sideTallId)
               .setCondition(state -> state.getValue(WEST_WALL) == WallSide.TALL)
               .setTransformation(BlockModelRotation.X0_Y270.getRotation())
               .setUVLock(true)
               .add();
        builder.part(postId).setCondition(state -> state.getValue(UP)).add();

        return builder.build();
    }
}
