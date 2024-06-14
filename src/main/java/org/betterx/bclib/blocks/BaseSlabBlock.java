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
import org.betterx.bclib.interfaces.RuntimeBlockModelProvider;
import org.betterx.wover.block.api.BlockTagProvider;
import org.betterx.wover.block.api.CustomBlockItemProvider;
import org.betterx.wover.item.api.ItemTagProvider;
import org.betterx.wover.tag.api.event.context.ItemTagBootstrapContext;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public abstract class BaseSlabBlock extends SlabBlock implements RuntimeBlockModelProvider, CustomBlockItemProvider, BlockTagProvider, ItemTagProvider, DropSelfLootProvider<BaseSlabBlock> {
    private final Block parent;
    public final boolean fireproof;

    protected BaseSlabBlock(Block source, boolean fireproof) {
        super(Properties.ofFullCopy(source));
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
        Optional<String> pattern;
        if (blockState.getValue(TYPE) == SlabType.DOUBLE) {
            pattern = PatternsHelper.createBlockSimple(parentId);
        } else {
            pattern = PatternsHelper.createJson(BasePatterns.BLOCK_SLAB, parentId);
        }
        return ModelsHelper.fromPattern(pattern);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public UnbakedModel getModelVariant(
            ModelResourceLocation stateId,
            BlockState blockState,
            Map<ResourceLocation, UnbakedModel> modelCache
    ) {
        SlabType type = blockState.getValue(TYPE);
        ModelResourceLocation modelId = RuntimeBlockModelProvider.remapModelResourceLocation(stateId, blockState, "_" + type);
        registerBlockModel(stateId, modelId, blockState, modelCache);
        if (type == SlabType.TOP) {
            return ModelsHelper.createMultiVariant(modelId.id(), BlockModelRotation.X180_Y0.getRotation(), true);
        }
        return ModelsHelper.createBlockSimple(modelId.id());
    }

    @Override
    public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
        context.add(BlockTags.SLABS, this);
    }

    @Override
    public void registerItemTags(ResourceLocation location, ItemTagBootstrapContext context) {
        context.add(ItemTags.SLABS, this);
    }

    @Override
    public BlockItem getCustomBlockItem(ResourceLocation blockID, Item.Properties settings) {
        if (fireproof) settings = settings.fireResistant();
        return new BlockItem(this, settings);
    }

    public static class Stone extends BaseSlabBlock implements BehaviourStone {
        public Stone(Block source) {
            this(source, true);
        }

        public Stone(Block source, boolean fireproof) {
            super(source, fireproof);
        }
    }

    public static class Metal extends BaseSlabBlock implements BehaviourMetal {
        public Metal(Block source) {
            this(source, true);
        }

        public Metal(Block source, boolean fireproof) {
            super(source, fireproof);
        }
    }

    public static class Wood extends BaseSlabBlock implements BehaviourWood {
        public Wood(Block source) {
            this(source, false);
        }

        public Wood(Block source, boolean fireproof) {
            super(source, fireproof);
        }

        @Override
        public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
            context.add(this, BlockTags.SLABS, BlockTags.WOODEN_SLABS);
        }

        @Override
        public void registerItemTags(ResourceLocation location, ItemTagBootstrapContext context) {
            context.add(this, ItemTags.SLABS, ItemTags.WOODEN_SLABS);
        }
    }

    public static class Obsidian extends BaseSlabBlock implements BehaviourObsidian {
        public Obsidian(Block source) {
            super(source, true);
        }

        public Obsidian(Block source, boolean fireproof) {
            super(source, fireproof);
        }
    }

    public static BaseSlabBlock from(Block source, boolean flammable) {
        return BehaviourHelper.from(
                source,
                (s) -> new BaseSlabBlock.Wood(s, !flammable),
                (s) -> new BaseSlabBlock.Stone(s, !flammable),
                (s) -> new BaseSlabBlock.Metal(s, !flammable),
                (s) -> new BaseSlabBlock.Obsidian(s, !flammable),
                null
        );
    }
}
