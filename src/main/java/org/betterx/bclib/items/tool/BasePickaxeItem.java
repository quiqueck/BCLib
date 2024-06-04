package org.betterx.bclib.items.tool;

import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.interfaces.ItemModelProvider;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class BasePickaxeItem extends PickaxeItem implements ItemModelProvider {
    public BasePickaxeItem(Tier material, int attackDamage, float attackSpeed, Properties settings) {
        this(material, settings.attributes(PickaxeItem.createAttributes(material, attackDamage, attackSpeed)));
    }

    public BasePickaxeItem(Tier material, Properties settings) {
        super(material, settings);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation resourceLocation) {
        return ModelsHelper.createHandheldItem(resourceLocation);
    }
}
