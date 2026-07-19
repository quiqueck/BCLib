package org.betterx.bclib.items;

import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.interfaces.ItemModelProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class ModelProviderItem extends Item implements ItemModelProvider {
    public ModelProviderItem(Properties settings) {
        super(settings);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation resourceLocation) {
        return ModelsHelper.createItemModel(resourceLocation);
    }
}
