package org.betterx.bclib.items.tool;


import org.betterx.worlds.together.tag.v3.CommonItemTags;
import org.betterx.worlds.together.tag.v3.TagManager;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;

import net.fabricmc.fabric.api.mininglevel.v1.FabricMineableTags;

public class BaseShearsItem extends ShearsItem {
    public BaseShearsItem(Properties properties) {
        super(properties);
    }

    public static boolean isShear(ItemStack tool) {
        return tool.is(Items.SHEARS) | tool.is(CommonItemTags.SHEARS) || TagManager.isToolWithMineableTag(
                tool,
                FabricMineableTags.SHEARS_MINEABLE
        );
    }

    public static boolean isShear(ItemStack itemStack, Item item) {
        if (item == Items.SHEARS) {
            return itemStack.is(item) | itemStack.is(CommonItemTags.SHEARS);
        } else {
            return itemStack.is(item);
        }
    }
}
