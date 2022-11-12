package org.betterx.bclib.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public class RecipeHelper {
    public static boolean exists(ItemLike item) {
        if (item instanceof Block) {
            return BuiltInRegistries.BLOCK.getKey((Block) item) != BuiltInRegistries.BLOCK.getDefaultKey();
        } else {
            return BuiltInRegistries.ITEM.getKey(item.asItem()) != BuiltInRegistries.ITEM.getDefaultKey();
        }
    }

    public static boolean exists(ItemLike... items) {
        for (ItemLike item : items) {
            if (!exists(item)) {
                return false;
            }
        }
        return true;
    }
}
