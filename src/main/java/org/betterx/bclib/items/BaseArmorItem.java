package org.betterx.bclib.items;

import org.betterx.bclib.interfaces.ItemModelProvider;
import org.betterx.wover.item.api.ItemTagProvider;
import org.betterx.wover.tag.api.event.context.ItemTagBootstrapContext;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

public class BaseArmorItem extends ArmorItem implements ItemModelProvider, ItemTagProvider {
    public BaseArmorItem(Holder<ArmorMaterial> material, Type type, Properties settings) {
        super(material, type, settings);
    }

    @Override
    public void registerItemTags(ResourceLocation location, ItemTagBootstrapContext context) {
        if (this.type.getSlot() == EquipmentSlot.HEAD) {
            context.add(this, ItemTags.HEAD_ARMOR);
        } else if (this.type.getSlot() == EquipmentSlot.CHEST) {
            context.add(this, ItemTags.CHEST_ARMOR);
        } else if (this.type.getSlot() == EquipmentSlot.LEGS) {
            context.add(this, ItemTags.LEG_ARMOR);
        } else if (this.type.getSlot() == EquipmentSlot.FEET) {
            context.add(this, ItemTags.FOOT_ARMOR);
        }
    }
}
