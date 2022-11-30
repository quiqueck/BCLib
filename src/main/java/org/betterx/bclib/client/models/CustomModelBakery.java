package org.betterx.bclib.client.models;

import org.betterx.bclib.interfaces.BlockModelProvider;
import org.betterx.bclib.interfaces.ItemModelProvider;
import org.betterx.bclib.models.RecordItemModelProvider;

import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import java.util.Map;

public class CustomModelBakery {
    private final Map<ResourceLocation, UnbakedModel> models = Maps.newConcurrentMap();

    public UnbakedModel getBlockModel(ResourceLocation location) {
        return models.get(location);
    }

    public UnbakedModel getItemModel(ResourceLocation location) {
        return models.get(location);
    }

    public void loadCustomModels(ResourceManager resourceManager) {
        BuiltInRegistries.BLOCK.stream()
                               .parallel()
                               .filter(block -> block instanceof BlockModelProvider)
                               .forEach(block -> {
                                   ResourceLocation blockID = BuiltInRegistries.BLOCK.getKey(block);
                                   ResourceLocation storageID = new ResourceLocation(
                                           blockID.getNamespace(),
                                           "blockstates/" + blockID.getPath() + ".json"
                                   );
                                   if (resourceManager.getResource(storageID).isEmpty()) {
                                       addBlockModel(blockID, block);
                                   }
                                   storageID = new ResourceLocation(
                                           blockID.getNamespace(),
                                           "models/item/" + blockID.getPath() + ".json"
                                   );
                                   if (resourceManager.getResource(storageID).isEmpty()) {
                                       addItemModel(blockID, (ItemModelProvider) block);
                                   }
                               });

        BuiltInRegistries.ITEM.stream()
                              .parallel()
                              .filter(item -> item instanceof ItemModelProvider || RecordItemModelProvider.has(item))
                              .forEach(item -> {
                                  ResourceLocation registryID = BuiltInRegistries.ITEM.getKey(item);
                                  ResourceLocation storageID = new ResourceLocation(
                                          registryID.getNamespace(),
                                          "models/item/" + registryID.getPath() + ".json"
                                  );
                                  final ItemModelProvider provider = (item instanceof ItemModelProvider)
                                          ? (ItemModelProvider) item
                                          : RecordItemModelProvider.get(item);

                                  if (resourceManager.getResource(storageID).isEmpty()) {
                                      addItemModel(registryID, provider);
                                  }
                              });
    }

    private void addBlockModel(ResourceLocation blockID, Block block) {
        BlockModelProvider provider = (BlockModelProvider) block;
        ImmutableList<BlockState> states = block.getStateDefinition().getPossibleStates();
        BlockState defaultState = block.defaultBlockState();

        ResourceLocation defaultStateID = BlockModelShaper.stateToModelLocation(blockID, defaultState);
        UnbakedModel defaultModel = provider.getModelVariant(defaultStateID, defaultState, models);

        if (defaultModel instanceof MultiPart) {
            states.forEach(blockState -> {
                ResourceLocation stateID = BlockModelShaper.stateToModelLocation(blockID, blockState);
                models.put(stateID, defaultModel);
            });
        } else {
            states.forEach(blockState -> {
                ResourceLocation stateID = BlockModelShaper.stateToModelLocation(blockID, blockState);
                UnbakedModel model = stateID.equals(defaultStateID)
                        ? defaultModel
                        : provider.getModelVariant(stateID, blockState, models);
                models.put(stateID, model);
            });
        }
    }

    private void addItemModel(ResourceLocation itemID, ItemModelProvider provider) {
        ModelResourceLocation modelLocation = new ModelResourceLocation(
                itemID.getNamespace(),
                itemID.getPath(),
                "inventory"
        );
        if (models.containsKey(modelLocation)) {
            return;
        }
        BlockModel model = provider.getItemModel(modelLocation);
        models.put(modelLocation, model);
    }
}
