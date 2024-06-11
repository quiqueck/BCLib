package org.betterx.bclib.interfaces;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;

import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
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
    static ModelResourceLocation remapModelResourceLocation(
            ModelResourceLocation stateId,
            BlockState blockState
    ) {
        return remapModelResourceLocation(stateId, blockState, "");
    }

    static ModelResourceLocation remapModelResourceLocation(
            ModelResourceLocation stateId,
            BlockState blockState,
            String pathAddOn
    ) {
        return BlockModelShaper.stateToModelLocation(
                ResourceLocation.fromNamespaceAndPath(stateId.id().getNamespace(), "block/" + stateId
                        .id()
                        .getPath() + pathAddOn),
                blockState
        );
    }

    @Environment(EnvType.CLIENT)
    default UnbakedModel getModelVariant(
            ModelResourceLocation stateId,
            BlockState blockState,
            Map<ResourceLocation, UnbakedModel> modelCache
    ) {
        ModelResourceLocation modelId = remapModelResourceLocation(stateId, blockState);
        registerBlockModel(stateId, modelId, blockState, modelCache);
        return ModelsHelper.createBlockSimple(modelId.id());
    }

    @Environment(EnvType.CLIENT)
    default void registerBlockModel(
            ModelResourceLocation stateId,
            ModelResourceLocation modelId,
            BlockState blockState,
            Map<ResourceLocation, UnbakedModel> modelCache
    ) {
        if (!modelCache.containsKey(modelId.id())) {
            BlockModel model = getBlockModel(stateId.id(), blockState);
            if (model != null) {
                model.name = modelId.toString();
                modelCache.put(modelId.id(), model);
            } else {
                BCLib.LOGGER.warn("Error loading model: {}", modelId);
            }
        }
    }
}
