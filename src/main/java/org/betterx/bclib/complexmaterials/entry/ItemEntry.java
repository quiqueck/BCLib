package org.betterx.bclib.complexmaterials.entry;

import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.wover.item.api.ItemRegistry;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.function.BiFunction;

public class ItemEntry extends ComplexMaterialEntry {
    final BiFunction<ComplexMaterial, Item.Properties, Item> initFunction;

    TagKey<Item>[] itemTags;

    public ItemEntry(String suffix, BiFunction<ComplexMaterial, Item.Properties, Item> initFunction) {
        super(suffix);
        this.initFunction = initFunction;
    }

    public ItemEntry setItemTags(TagKey<Item>[] itemTags) {
        this.itemTags = itemTags;
        return this;
    }

    public Item init(ComplexMaterial material, Item.Properties itemSettings, ItemRegistry registry) {
        String location = getName(material.getBaseName());
        Item item = initFunction.apply(material, itemSettings);
        if (itemTags == null)
            registry.register(location, item);
        else
            registry.register(location, item, itemTags);

        return item;
    }
}
