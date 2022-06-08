package org.betterx.bclib.blocks;

import org.betterx.bclib.client.models.BasePatterns;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;
import org.betterx.bclib.interfaces.BlockModelProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeightedPressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public class BaseWeightedPlateBlock extends WeightedPressurePlateBlock implements BlockModelProvider {
    private final Block parent;

    public BaseWeightedPlateBlock(Block source) {
        super(
                15,
                FabricBlockSettings.copyOf(source)
                                   .noCollission()
                                   .noOcclusion()
                                   .requiresCorrectToolForDrops()
                                   .strength(0.5F)
        );
        this.parent = source;
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        return Collections.singletonList(new ItemStack(this));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation resourceLocation) {
        return getBlockModel(resourceLocation, defaultBlockState());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public @Nullable BlockModel getBlockModel(ResourceLocation resourceLocation, BlockState blockState) {
        ResourceLocation parentId = Registry.BLOCK.getKey(parent);
        Optional<String> pattern;
        if (blockState.getValue(POWER) > 0) {
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
        String state = blockState.getValue(POWER) > 0 ? "_down" : "_up";
        ResourceLocation modelId = new ResourceLocation(stateId.getNamespace(), "block/" + stateId.getPath() + state);
        registerBlockModel(stateId, modelId, blockState, modelCache);
        return ModelsHelper.createBlockSimple(modelId);
    }
}
