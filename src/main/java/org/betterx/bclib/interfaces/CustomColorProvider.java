package org.betterx.bclib.interfaces;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface CustomColorProvider {
    @Environment(EnvType.CLIENT)
    BlockColor getProvider();

    @Environment(EnvType.CLIENT)
    ItemColor getItemProvider();
}
