package org.betterx.bclib.interfaces;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public interface BlockModelProvider extends ItemModelProvider {
    @Environment(EnvType.CLIENT)
    default @Nullable BlockModel getBlockModel(ResourceLocation resourceLocation, BlockState blockState) {
        Optional<String> pattern = PatternsHelper.createBlockSimple(resourceLocation);
        return ModelsHelper.fromPattern(pattern);
    }

    @Environment(EnvType.CLIENT)
    default UnbakedModel getModelVariant(
            ResourceLocation stateId,
            BlockState blockState,
            Map<ResourceLocation, UnbakedModel> modelCache
    ) {
        ResourceLocation modelId = new ResourceLocation(stateId.getNamespace(), "block/" + stateId.getPath());
        registerBlockModel(stateId, modelId, blockState, modelCache);
        return ModelsHelper.createBlockSimple(modelId);
    }

    @Environment(EnvType.CLIENT)
    default void registerBlockModel(
            ResourceLocation stateId,
            ResourceLocation modelId,
            BlockState blockState,
            Map<ResourceLocation, UnbakedModel> modelCache
    ) {
        if (!modelCache.containsKey(modelId)) {
            BlockModel model = getBlockModel(stateId, blockState);
            if (model != null) {
                model.name = modelId.toString();
                modelCache.put(modelId, model);
            } else {
                BCLib.LOGGER.warning("Error loading model: {}", modelId);
            }
        }
    }
}
