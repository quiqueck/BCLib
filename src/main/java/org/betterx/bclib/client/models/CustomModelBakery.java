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

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomModelBakery {
    private record StateModelPair(BlockState state, UnbakedModel model) {
    }

    private final Map<ResourceLocation, UnbakedModel> models = Maps.newConcurrentMap();
    private final Map<Block, List<StateModelPair>> blockModels = Maps.newConcurrentMap();

    public UnbakedModel getBlockModel(ResourceLocation location) {
        return models.get(location);
    }

    public UnbakedModel getItemModel(ResourceLocation location) {
        return models.get(location);
    }

    public void registerBlockStateResolvers(ModelLoadingPlugin.Context pluginContext) {
        for (Map.Entry<Block, List<StateModelPair>> e : this.blockModels.entrySet()) {
            pluginContext.registerBlockStateResolver(
                    e.getKey(),
                    context -> {
                        e.getValue().forEach(p -> context.setModel(p.state, p.model));
                    }
            );
        }
    }

    public void loadCustomModels(ResourceManager resourceManager) {
        BuiltInRegistries.BLOCK.stream()
                               .parallel()
                               .filter(block -> block instanceof BlockModelProvider)
                               .forEach(block -> {
                                   ResourceLocation blockID = BuiltInRegistries.BLOCK.getKey(block);
                                   ResourceLocation storageID = ResourceLocation.fromNamespaceAndPath(
                                           blockID.getNamespace(),
                                           "blockstates/" + blockID.getPath() + ".json"
                                   );
                                   if (resourceManager.getResource(storageID).isEmpty()) {
                                       addBlockModel(blockID, block);
                                   }
                                   storageID = ResourceLocation.fromNamespaceAndPath(
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
                                  ResourceLocation storageID = ResourceLocation.fromNamespaceAndPath(
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

        ModelResourceLocation defaultStateID = BlockModelShaper.stateToModelLocation(blockID, defaultState);
        UnbakedModel defaultModel = provider.getModelVariant(defaultStateID, defaultState, models);

        List<StateModelPair> stateModels = new ArrayList<>(states.size());
        if (defaultModel instanceof MultiPart) {
            states.forEach(blockState -> {
                ModelResourceLocation stateID = BlockModelShaper.stateToModelLocation(blockID, blockState);
                models.put(stateID.id(), defaultModel);
                stateModels.add(new StateModelPair(blockState, defaultModel));
            });
        } else {
            states.forEach(blockState -> {
                ModelResourceLocation stateID = BlockModelShaper.stateToModelLocation(blockID, blockState);
                UnbakedModel model = stateID.equals(defaultStateID)
                        ? defaultModel
                        : provider.getModelVariant(stateID, blockState, models);
                models.put(stateID.id(), model);
                stateModels.add(new StateModelPair(blockState, model));
            });
        }
        blockModels.put(block, stateModels);
    }

    private void addItemModel(ResourceLocation itemID, ItemModelProvider provider) {
        ModelResourceLocation modelLocation = new ModelResourceLocation(
                itemID,
                "inventory"
        );

        if (!models.containsKey(modelLocation)) {
            ResourceLocation itemModelLocation = itemID.withPrefix("item/");
            BlockModel model = provider.getItemModel(modelLocation.id());
            models.put(modelLocation.id(), model);
            models.put(itemModelLocation, model);
        }
    }
}