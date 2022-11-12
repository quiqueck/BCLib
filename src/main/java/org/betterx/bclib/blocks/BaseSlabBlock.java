package org.betterx.bclib.blocks;

import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.interfaces.BlockModelProvider;
import org.betterx.bclib.interfaces.CustomItemProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.LootContext;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public class BaseSlabBlock extends SlabBlock implements BlockModelProvider, CustomItemProvider {
    private final Block parent;
    public final boolean fireproof;

    public BaseSlabBlock(Block source) {
        this(source, false);
    }

    public BaseSlabBlock(Block source, boolean fireproof) {
        super(Properties.copy(source));
        this.parent = source;
        this.fireproof = fireproof;
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        int count = state.getValue(TYPE) == SlabType.DOUBLE ? 2 : 1;
        return Collections.singletonList(new ItemStack(this, count));
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
            ResourceLocation stateId,
            BlockState blockState,
            Map<ResourceLocation, UnbakedModel> modelCache
    ) {
        SlabType type = blockState.getValue(TYPE);
        ResourceLocation modelId = new ResourceLocation(
                stateId.getNamespace(),
                "block/" + stateId.getPath() + "_" + type
        );
        registerBlockModel(stateId, modelId, blockState, modelCache);
        if (type == SlabType.TOP) {
            return ModelsHelper.createMultiVariant(modelId, BlockModelRotation.X180_Y0.getRotation(), true);
        }
        return ModelsHelper.createBlockSimple(modelId);
    }

    @Override
    public BlockItem getCustomItem(ResourceLocation blockID, Item.Properties settings) {
        if (fireproof) settings = settings.fireResistant();
        return new BlockItem(this, settings);
    }
}
