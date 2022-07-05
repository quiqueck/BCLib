package org.betterx.bclib.interfaces;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;

public interface CustomItemProvider {
    /**
     * Used to replace default Block Item when block is registered.
     *
     * @return {@link BlockItem}
     */
    BlockItem getCustomItem(ResourceLocation blockID, FabricItemSettings settings);
}
