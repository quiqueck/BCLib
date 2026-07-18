package org.betterx.bclib.client.models;

import org.betterx.bclib.interfaces.ItemModelProvider;
import org.betterx.bclib.models.RecordItemModelProvider;

import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class CustomModelBakery {
    private record StateModelPair(BlockState state, MultiVariant model) {
    }

    private final Map<ResourceLocation, MultiVariant> models = Maps.newConcurrentMap();
    private final Map<ResourceLocation, MultiVariant> itemModels = Maps.newConcurrentMap();
    private final Map<Block, List<StateModelPair>> blockModels = Maps.newConcurrentMap();

    public MultiVariant getBlockModel(ResourceLocation location) {
        return models.get(location);
    }

    public MultiVariant getItemModel(ResourceLocation location) {
        return itemModels.get(location);
    }

    public void registerBlockStateResolvers(ModelLoadingPlugin.Context pluginContext) {
        for (Map.Entry<Block, List<StateModelPair>> e : this.blockModels.entrySet()) {
            pluginContext.registerBlockStateResolver(
                    e.getKey(),
                    context -> {
                        e.getValue().forEach(p -> context.setModel(p.state, p.model.toUnbaked().asRoot()));
                    }
            );
        }
    }

    public void loadCustomModels(ResourceManager resourceManager) {
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

    private void addItemModel(ResourceLocation itemID, ItemModelProvider provider) {
//        ResourceLocation modelLocation = itemID.withSuffix("inventory");
//
//        if (!models.containsKey(modelLocation)) {
//            ResourceLocation itemModelLocation = itemID.withPrefix("item/");
//            BlockModel model = provider.getItemModel(modelLocation);
//            itemModels.put(modelLocation, model);
//            itemModels.put(itemModelLocation, model);
//        }
    }
}