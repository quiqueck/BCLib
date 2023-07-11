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
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public abstract class BasePressurePlateBlock extends PressurePlateBlock implements BlockModelProvider, TagProvider, DropSelfLootProvider<BasePressurePlateBlock> {
    private final Block parent;

    protected BasePressurePlateBlock(Sensitivity rule, Block source, BlockSetType type) {
        super(
                rule, Properties.copy(source).noCollission().noOcclusion().strength(0.5F),
                type
        );
        this.parent = source;
    }


    @Override
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation resourceLocation) {
        return getBlockModel(resourceLocation, defaultBlockState());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public @Nullable BlockModel getBlockModel(ResourceLocation resourceLocation, BlockState blockState) {
        ResourceLocation parentId = BuiltInRegistries.BLOCK.getKey(parent);
        Optional<String> pattern;
        if (blockState.getValue(POWERED)) {
            pattern = PatternsHelper.createJson(BasePatterns.BLOCK_PLATE_DOWN, parentId);
        } else {
            pattern = PatternsHelper.createJson(BasePatterns.BLOCK_PLATE_UP, parentId);
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
        String state = blockState.getValue(POWERED) ? "_down" : "_up";
        ResourceLocation modelId = new ResourceLocation(stateId.getNamespace(), "block/" + stateId.getPath() + state);
        registerBlockModel(stateId, modelId, blockState, modelCache);
        return ModelsHelper.createBlockSimple(modelId);
    }

    @Override
    public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
        blockTags.add(BlockTags.PRESSURE_PLATES);
    }

    public static class Wood extends BasePressurePlateBlock implements BehaviourWood {
        public Wood(Block source, BlockSetType type) {
            super(Sensitivity.EVERYTHING, source, type);
        }

        @Override
        public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
            super.addTags(blockTags, itemTags);
            blockTags.add(BlockTags.WOODEN_PRESSURE_PLATES);
            itemTags.add(ItemTags.WOODEN_PRESSURE_PLATES);
        }
    }

    public static class Stone extends BasePressurePlateBlock implements BehaviourStone {
        public Stone(Block source, BlockSetType type) {
            super(Sensitivity.MOBS, source, type);
        }
    }

    public static class Metal extends BasePressurePlateBlock implements BehaviourMetal {
        public Metal(Block source, BlockSetType type) {
            super(Sensitivity.MOBS, source, type);
        }
    }

    public static BasePressurePlateBlock from(Block source, BlockSetType type) {
        return BehaviourHelper.from(source, type,
                Wood::new, Stone::new, Metal::new
        );
    }
}
