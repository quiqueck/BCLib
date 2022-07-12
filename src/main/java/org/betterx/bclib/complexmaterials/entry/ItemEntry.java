package org.betterx.bclib.complexmaterials.entry;

import org.betterx.bclib.complexmaterials.ComplexMaterial;
import org.betterx.bclib.registry.ItemRegistry;
import org.betterx.worlds.together.tag.v3.TagManager;

import net.minecraft.resources.ResourceLocation;
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
        ResourceLocation location = getLocation(material.getModID(), material.getBaseName());
        Item item = initFunction.apply(material, itemSettings);
        registry.register(location, item);
        if (itemTags != null) {
            TagManager.ITEMS.add(item, itemTags);
        }
        return item;
    }
}
