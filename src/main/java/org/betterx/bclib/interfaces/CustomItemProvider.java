package org.betterx.bclib.interfaces;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public interface CustomItemProvider {
    /**
     * Used to replace default Block Item when block is registered.
     *
     * @return {@link BlockItem}
     */
    BlockItem getCustomItem(ResourceLocation blockID, Item.Properties settings);
}
