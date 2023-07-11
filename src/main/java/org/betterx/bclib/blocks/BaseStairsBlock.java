package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.BehaviourHelper;
import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.behaviours.interfaces.BehaviourObsidian;
import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.interfaces.BlockModelProvider;
import org.betterx.bclib.interfaces.CustomItemProvider;
import org.betterx.bclib.interfaces.TagProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public abstract class BaseStairsBlock extends StairBlock implements BlockModelProvider, CustomItemProvider, TagProvider, DropSelfLootProvider<BaseStairsBlock> {


    private final Block parent;
    public final boolean fireproof;

    protected BaseStairsBlock(Block source, boolean fireproof) {
        super(source.defaultBlockState(), Properties.copy(source));
        this.parent = source;
        this.fireproof = fireproof;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation resourceLocation) {
        return getBlockModel(resourceLocation, defaultBlockState());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public @Nullable BlockModel getBlockModel(ResourceLocation blockId, BlockState blockState) {
        ResourceLocation parentId = BuiltInRegistries.BLOCK.getKey(parent);
        Optional<String> pattern = PatternsHelper.createJson(switch (blockState.getValue(SHAPE)) {
            case STRAIGHT -> BasePatterns.BLOCK_STAIR;
            case INNER_LEFT, INNER_RIGHT -> BasePatterns.BLOCK_STAIR_INNER;
            case OUTER_LEFT, OUTER_RIGHT -> BasePatterns.BLOCK_STAIR_OUTER;
        }, parentId);
        return ModelsHelper.fromPattern(pattern);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public UnbakedModel getModelVariant(
            ResourceLocation stateId,
            BlockState blockState,
            Map<ResourceLocation, UnbakedModel> modelCache
    ) {
        String state;
        StairsShape shape = blockState.getValue(SHAPE);
        state = switch (shape) {
            case INNER_LEFT, INNER_RIGHT -> "_inner";
            case OUTER_LEFT, OUTER_RIGHT -> "_outer";
            default -> "";
        };
        ResourceLocation modelId = new ResourceLocation(stateId.getNamespace(), "block/" + stateId.getPath() + state);
        registerBlockModel(stateId, modelId, blockState, modelCache);

        boolean isTop = blockState.getValue(HALF) == Half.TOP;
        boolean isLeft = shape == StairsShape.INNER_LEFT || shape == StairsShape.OUTER_LEFT;
        boolean isRight = shape == StairsShape.INNER_RIGHT || shape == StairsShape.OUTER_RIGHT;
        int y = 0;
        int x = isTop ? 180 : 0;
        switch (blockState.getValue(FACING)) {
            case NORTH:
                if (isTop && !isRight) y = 270;
                else if (!isTop) y = isLeft ? 180 : 270;
                break;
            case EAST:
                if (isTop && isRight) y = 90;
                else if (!isTop && isLeft) y = 270;
                break;
            case SOUTH:
                if (isTop) y = isRight ? 180 : 90;
                else if (!isLeft) y = 90;
                break;
            case WEST:
            default:
                y = (isTop && isRight) ? 270 : (!isTop && isLeft) ? 90 : 180;
                break;
        }
        BlockModelRotation rotation = BlockModelRotation.by(x, y);
        return ModelsHelper.createMultiVariant(modelId, rotation.getRotation(), true);
    }

    @Override
    public BlockItem getCustomItem(ResourceLocation blockID, Item.Properties settings) {
        if (fireproof) settings = settings.fireResistant();
        return new BlockItem(this, settings);
    }

    @Override
    public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
        blockTags.add(BlockTags.STAIRS);
        itemTags.add(ItemTags.STAIRS);
    }

    public static class Stone extends BaseStairsBlock implements BehaviourStone {
        public Stone(Block source) {
            this(source, true);
        }

        public Stone(Block source, boolean fireproof) {
            super(source, fireproof);
        }
    }

    public static class Metal extends BaseStairsBlock implements BehaviourMetal {
        public Metal(Block source) {
            this(source, true);
        }

        public Metal(Block source, boolean fireproof) {
            super(source, fireproof);
        }
    }

    public static class Wood extends BaseStairsBlock implements BehaviourWood {
        public Wood(Block source) {
            this(source, false);
        }

        public Wood(Block source, boolean fireproof) {
            super(source, fireproof);
        }

        @Override
        public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
            super.addTags(blockTags, itemTags);
            blockTags.add(BlockTags.WOODEN_STAIRS);
            itemTags.add(ItemTags.WOODEN_STAIRS);
        }
    }

    public static class Obsidian extends BaseStairsBlock implements BehaviourObsidian {
        public Obsidian(Block source) {
            this(source, true);
        }

        public Obsidian(Block source, boolean fireproof) {
            super(source, fireproof);
        }
    }

    public static BaseStairsBlock from(Block source, boolean flammable) {
        return BehaviourHelper.from(
                source,
                (block) -> new Wood(block, flammable),
                (block) -> new Stone(block, !flammable),
                (block) -> new Metal(block, !flammable),
                (block) -> new Obsidian(block, !flammable),
                null
        );

    }
}
