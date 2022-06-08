package org.betterx.bclib.api.v2;

import org.betterx.bclib.mixin.common.ComposterBlockAccessor;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class ComposterAPI {
    public static Block allowCompost(float chance, Block block) {
        if (block != null) {
            allowCompost(chance, block.asItem());
        }
        return block;
    }

    public static Item allowCompost(float chance, Item item) {
        if (item != null && item != Items.AIR) {
            ComposterBlockAccessor.callAdd(chance, item);
        }
        return item;
    }
}
