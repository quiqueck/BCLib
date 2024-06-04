package org.betterx.bclib.items;

import org.betterx.bclib.interfaces.ItemModelProvider;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

public class BaseArmorItem extends ArmorItem implements ItemModelProvider {
    public BaseArmorItem(Holder<ArmorMaterial> material, Type type, Properties settings) {
        super(material, type, settings);
    }
}
