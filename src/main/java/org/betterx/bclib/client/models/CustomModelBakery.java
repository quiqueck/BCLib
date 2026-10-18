package org.betterx.bclib.client.models;

import org.betterx.bclib.BCLib;
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
                                   final ResourceLocation blockID = BuiltInRegistries.BLOCK.getKey(block);
                                   try {
                                       loadBlock(resourceManager, blockID, block);
                                   } catch (RuntimeException | LinkageError e) {
                                       BCLib.LOGGER.warning(
                                               "Skipping custom models for block {} ({}): {}",
                                               blockID, block.getClass().getName(), e.toString()
                                       );
                                   }
                               });

        BuiltInRegistries.ITEM.stream()
                              .parallel()
                              .filter(item -> item instanceof ItemModelProvider || RecordItemModelProvider.has(item))
                              .forEach(item -> {
                                  final ResourceLocation registryID = BuiltInRegistries.ITEM.getKey(item);
                                  try {
                                      ResourceLocation storageID = new ResourceLocation(
                                              registryID.getNamespace(),
                                              "models/item/" + registryID.getPath() + ".json"
                                      );
                                      final ItemModelProvider provider = (item instanceof ItemModelProvider)
                                              ? (ItemModelProvider) item
                                              : RecordItemModelProvider.get(item);

                                      if (provider != null && resourceManager.getResource(storageID).isEmpty()) {
                                          addItemModel(registryID, provider);
                                      }
                                  } catch (RuntimeException | LinkageError e) {
                                      BCLib.LOGGER.warning(
                                              "Skipping custom item model for {} ({}): {}",
                                              registryID, item.getClass().getName(), e.toString()
                                      );
                                  }
                              });
    }

    private void loadBlock(ResourceManager resourceManager, ResourceLocation blockID, Block block) {
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
    }

    private void addBlockModel(ResourceLocation blockID, Block block) {
        BlockModelProvider provider = (BlockModelProvider) block;
        ImmutableList<BlockState> states = block.getStateDefinition().getPossibleStates();
        BlockState defaultState = block.defaultBlockState();

        ResourceLocation defaultStateID = BlockModelShaper.stateToModelLocation(blockID, defaultState);
        UnbakedModel defaultModel = provider.getModelVariant(defaultStateID, defaultState, models);

        int missing = 0;
        if (defaultModel instanceof MultiPart) {
            for (BlockState blockState : states) {
                ResourceLocation stateID = BlockModelShaper.stateToModelLocation(blockID, blockState);
                models.put(stateID, defaultModel);
            }
        } else {
            for (BlockState blockState : states) {
                ResourceLocation stateID = BlockModelShaper.stateToModelLocation(blockID, blockState);
                UnbakedModel model = stateID.equals(defaultStateID)
                        ? defaultModel
                        : provider.getModelVariant(stateID, blockState, models);
                //ConcurrentHashMap does not accept null values (quiqueck/BCLib#151)
                if (model == null) missing++;
                else models.put(stateID, model);
            }
        }
        if (missing > 0) {
            BCLib.LOGGER.warning(
                    "No custom model for {} of {} block states of {} ({})",
                    missing, states.size(), blockID, block.getClass().getName()
            );
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
        //ConcurrentHashMap does not accept null values. A provider returns null when its model
        //pattern could not be loaded (quiqueck/BCLib#151, quiqueck/BCLib#211)
        if (model == null) {
            BCLib.LOGGER.warning(
                    "No custom item model for {} ({}), skipping it",
                    itemID, provider.getClass().getName()
            );
            return;
        }
        models.put(modelLocation, model);
    }
}
