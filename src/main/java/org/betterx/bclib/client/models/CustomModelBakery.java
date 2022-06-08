package org.betterx.bclib.client.models;

import org.betterx.bclib.api.v2.ModIntegrationAPI;
import org.betterx.bclib.client.render.EmissiveTextureInfo;
import org.betterx.bclib.interfaces.BlockModelProvider;
import org.betterx.bclib.interfaces.ItemModelProvider;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CustomModelBakery {
    private final Map<ResourceLocation, UnbakedModel> models = Maps.newConcurrentMap();

    public UnbakedModel getBlockModel(ResourceLocation location) {
        return models.get(location);
    }

    public UnbakedModel getItemModel(ResourceLocation location) {
        ResourceLocation storageID = new ResourceLocation(
                location.getNamespace(),
                "models/item/" + location.getPath() + ".json"
        );
        return models.get(location);
    }

    public void loadCustomModels(ResourceManager resourceManager) {
        Registry.BLOCK.stream().parallel().filter(block -> block instanceof BlockModelProvider).forEach(block -> {
            ResourceLocation blockID = Registry.BLOCK.getKey(block);
            ResourceLocation storageID = new ResourceLocation(
                    blockID.getNamespace(),
                    "blockstates/" + blockID.getPath() + ".json"
            );
            if (resourceManager.getResource(storageID).isEmpty()) {
                addBlockModel(blockID, block);
            }
            storageID = new ResourceLocation(blockID.getNamespace(), "models/item/" + blockID.getPath() + ".json");
            if (resourceManager.getResource(storageID).isEmpty()) {
                addItemModel(blockID, (ItemModelProvider) block);
            }
        });

        Registry.ITEM.stream().parallel().filter(item -> item instanceof ItemModelProvider).forEach(item -> {
            ResourceLocation registryID = Registry.ITEM.getKey(item);
            ResourceLocation storageID = new ResourceLocation(
                    registryID.getNamespace(),
                    "models/item/" + registryID.getPath() + ".json"
            );
            if (resourceManager.getResource(storageID).isEmpty()) {
                addItemModel(registryID, (ItemModelProvider) item);
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

    public static void loadEmissiveModels(Map<ResourceLocation, UnbakedModel> unbakedCache) {
        if (!ModIntegrationAPI.hasCanvas()) {
            return;
        }

        Map<ResourceLocation, UnbakedModel> cacheCopy = new HashMap<>(unbakedCache);
        Set<Pair<String, String>> strings = Sets.newConcurrentHashSet();
        Registry.BLOCK.keySet().forEach(blockID -> {
            Block block = Registry.BLOCK.get(blockID);
            ImmutableList<BlockState> states = block.getStateDefinition().getPossibleStates();
            boolean addBlock = false;

            for (BlockState state : states) {
                ResourceLocation stateID = BlockModelShaper.stateToModelLocation(blockID, state);
                UnbakedModel model = cacheCopy.get(stateID);
                if (model == null) {
                    continue;
                }
                Collection<Material> materials = model.getMaterials(cacheCopy::get, strings);
                if (materials == null) {
                    continue;
                }
                for (Material material : materials) {
                    if (EmissiveTextureInfo.isEmissiveTexture(material.texture())) {
                        addBlock = true;
                        break;
                    }
                }
                if (addBlock) {
                    break;
                }
            }

            if (addBlock) {
                EmissiveTextureInfo.addBlock(blockID);
            }
        });
    }
}
