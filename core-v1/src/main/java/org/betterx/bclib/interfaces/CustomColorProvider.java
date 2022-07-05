package org.betterx.bclib.interfaces;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;

public interface CustomColorProvider {
    BlockColor getProvider();

    ItemColor getItemProvider();
}
