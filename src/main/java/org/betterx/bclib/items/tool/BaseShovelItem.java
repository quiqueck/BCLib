package org.betterx.bclib.items.tool;

import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.interfaces.ItemModelProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class BaseShovelItem extends ShovelItem implements ItemModelProvider {
    public BaseShovelItem(Tier material, float attackDamage, float attackSpeed, Properties settings) {
        this(material, settings.attributes(ShovelItem.createAttributes(material, attackDamage, attackSpeed)));
    }

    public BaseShovelItem(Tier material, Properties settings) {
        super(material, settings);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation resourceLocation) {
        return ModelsHelper.createHandheldItem(resourceLocation);
    }
}
