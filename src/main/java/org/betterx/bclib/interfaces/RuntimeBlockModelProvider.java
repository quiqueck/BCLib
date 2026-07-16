package org.betterx.bclib.interfaces;

import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.client.models.PatternsHelper;

import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

@Deprecated(forRemoval = true)
public interface RuntimeBlockModelProvider extends ItemModelProvider {
    @Environment(EnvType.CLIENT)
    default @Nullable BlockModel getBlockModel(ResourceLocation resourceLocation, BlockState blockState) {
        Optional<String> pattern = PatternsHelper.createBlockSimple(resourceLocation);
        return ModelsHelper.fromPattern(pattern);
    }
    static ResourceLocation remapResourceLocation(
            ResourceLocation stateId,
            BlockState blockState
    ) {
        return remapResourceLocation(stateId, blockState, "");
    }

    static ResourceLocation remapResourceLocation(
            ResourceLocation stateId,
            BlockState blockState,
            String pathAddOn
    ) {
        return ResourceLocation.fromNamespaceAndPath(
                stateId.getNamespace(),
                "block/" + stateId.getPath() + pathAddOn
        );
    }

    @Environment(EnvType.CLIENT)
    default MultiVariant getModelVariant(
            ResourceLocation stateId,
            BlockState blockState,
            Map<ResourceLocation, MultiVariant> modelCache
    ) {
        var modelId = remapResourceLocation(stateId, blockState);
        registerBlockModel(stateId, modelId, blockState, modelCache);
        return ModelsHelper.createBlockSimple(modelId);
    }

    @Environment(EnvType.CLIENT)
    default void registerBlockModel(
            ResourceLocation stateId,
            ResourceLocation modelId,
            BlockState blockState,
            Map<ResourceLocation, MultiVariant> modelCache
    ) {
//        if (!modelCache.containsKey(modelId)) {
//            BlockModel model = getBlockModel(stateId, blockState);
//            if (model != null) {
//                modelCache.put(modelId, model);
//            } else {
//                BCLib.LOGGER.warn("Error loading model: {}", modelId);
//            }
//        }
    }
}
