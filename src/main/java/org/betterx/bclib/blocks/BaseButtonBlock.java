package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.BehaviourHelper;
import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.interfaces.BlockModelProvider;
import org.betterx.bclib.interfaces.TagProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockSetType;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public abstract class BaseButtonBlock extends ButtonBlock implements BlockModelProvider, TagProvider, DropSelfLootProvider<BaseButtonBlock> {
    private final Block parent;

    protected BaseButtonBlock(Block parent, Properties properties, boolean sensitive, BlockSetType type) {
        this(parent, properties, 30, sensitive, type);
    }

    protected BaseButtonBlock(
            Block parent,
            Properties properties,
            int ticksToStayPressed,
            boolean sensitive,
            BlockSetType type
    ) {
        super(
                properties.noCollission(), type, ticksToStayPressed, sensitive
        );
        this.parent = parent;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation blockId) {
        ResourceLocation parentId = BuiltInRegistries.BLOCK.getKey(parent);
        Optional<String> pattern = PatternsHelper.createJson(BasePatterns.ITEM_BUTTON, parentId);
        return ModelsHelper.fromPattern(pattern);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public @Nullable BlockModel getBlockModel(ResourceLocation resourceLocation, BlockState blockState) {
        ResourceLocation parentId = BuiltInRegistries.BLOCK.getKey(parent);
        Optional<String> pattern = blockState.getValue(POWERED)
                ? PatternsHelper.createJson(
                BasePatterns.BLOCK_BUTTON_PRESSED,
                parentId
        )
                : PatternsHelper.createJson(BasePatterns.BLOCK_BUTTON, parentId);
        return ModelsHelper.fromPattern(pattern);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public UnbakedModel getModelVariant(
            ResourceLocation stateId,
            BlockState blockState,
            Map<ResourceLocation, UnbakedModel> modelCache
    ) {
        String powered = blockState.getValue(POWERED) ? "_powered" : "";
        ResourceLocation modelId = new ResourceLocation(stateId.getNamespace(), "block/" + stateId.getPath() + powered);
        registerBlockModel(stateId, modelId, blockState, modelCache);
        AttachFace face = blockState.getValue(FACE);
        boolean isCeiling = face == AttachFace.CEILING;
        int x = 0, y = 0;
        switch (face) {
            case CEILING:
                x = 180;
                break;
            case WALL:
                x = 90;
                break;
            default:
                break;
        }
        switch (blockState.getValue(FACING)) {
            case NORTH:
                if (isCeiling) {
                    y = 180;
                }
                break;
            case EAST:
                y = isCeiling ? 270 : 90;
                break;
            case SOUTH:
                if (!isCeiling) {
                    y = 180;
                }
                break;
            case WEST:
                y = isCeiling ? 90 : 270;
                break;
            default:
                break;
        }
        BlockModelRotation rotation = BlockModelRotation.by(x, y);
        return ModelsHelper.createMultiVariant(modelId, rotation.getRotation(), face == AttachFace.WALL);
    }

    @Override
    public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
        blockTags.add(BlockTags.BUTTONS);
        itemTags.add(ItemTags.BUTTONS);
    }

    public static class Metal extends BaseButtonBlock implements BehaviourMetal {
        public Metal(Block source, BlockSetType type) {
            super(source, Properties.copy(source).noOcclusion(), false, type);
        }
    }

    public static class Stone extends BaseButtonBlock implements BehaviourStone {
        public Stone(Block source, BlockSetType type) {
            super(source, Properties.copy(source).noOcclusion(), false, type);
        }


        @Override
        public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
            super.addTags(blockTags, itemTags);
            blockTags.add(BlockTags.STONE_BUTTONS);
            itemTags.add(ItemTags.STONE_BUTTONS);
        }
    }

    public static class Wood extends BaseButtonBlock implements BehaviourWood {
        public Wood(Block source, BlockSetType type) {
            super(source, Properties.copy(source).strength(0.5F, 0.5F).noOcclusion(), true, type);
        }

        @Override
        public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
            super.addTags(blockTags, itemTags);
            blockTags.add(BlockTags.WOODEN_BUTTONS);
            itemTags.add(ItemTags.WOODEN_BUTTONS);
        }
    }

    public static BaseButtonBlock from(Block source, BlockSetType type) {
        return BehaviourHelper.from(source, type, Wood::new, Stone::new, Metal::new);
    }
}
