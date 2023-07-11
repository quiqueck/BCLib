package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.interfaces.BlockModelProvider;
import org.betterx.bclib.interfaces.TagProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public abstract class BaseGateBlock extends FenceGateBlock implements BlockModelProvider, TagProvider, DropSelfLootProvider<BaseGateBlock> {
    private final Block parent;

    protected BaseGateBlock(Block source, WoodType type) {
        super(Properties.copy(source).noOcclusion(), type);
        this.parent = source;
    }


    @Override
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation resourceLocation) {
        return getBlockModel(resourceLocation, defaultBlockState());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public @Nullable BlockModel getBlockModel(ResourceLocation blockId, BlockState blockState) {
        boolean inWall = blockState.getValue(IN_WALL);
        boolean isOpen = blockState.getValue(OPEN);
        ResourceLocation parentId = BuiltInRegistries.BLOCK.getKey(parent);
        Optional<String> pattern;
        if (inWall) {
            pattern = isOpen
                    ? PatternsHelper.createJson(
                    BasePatterns.BLOCK_GATE_OPEN_WALL,
                    parentId
            )
                    : PatternsHelper.createJson(BasePatterns.BLOCK_GATE_CLOSED_WALL, parentId);
        } else {
            pattern = isOpen
                    ? PatternsHelper.createJson(
                    BasePatterns.BLOCK_GATE_OPEN,
                    parentId
            )
                    : PatternsHelper.createJson(BasePatterns.BLOCK_GATE_CLOSED, parentId);
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
        boolean inWall = blockState.getValue(IN_WALL);
        boolean isOpen = blockState.getValue(OPEN);
        String state = "" + (inWall ? "_wall" : "") + (isOpen ? "_open" : "_closed");
        ResourceLocation modelId = new ResourceLocation(stateId.getNamespace(), "block/" + stateId.getPath() + state);
        registerBlockModel(stateId, modelId, blockState, modelCache);
        return ModelsHelper.createFacingModel(modelId, blockState.getValue(FACING), true, false);
    }

    @Override
    public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
        blockTags.add(BlockTags.FENCE_GATES);
    }

    public static class Wood extends BaseGateBlock implements BehaviourWood {
        public Wood(Block source, WoodType type) {
            super(source, type);
        }
    }

    public static BaseGateBlock from(Block source, WoodType type) {
        return new BaseGateBlock.Wood(source, type);
    }
}