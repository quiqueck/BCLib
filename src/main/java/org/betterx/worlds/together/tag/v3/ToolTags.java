package org.betterx.worlds.together.tag.v3;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;


public class ToolTags {
    public static final TagKey<Item> FABRIC_AXES = TagManager.ITEMS.makeTag("fabric", "axes");
    public static final TagKey<Item> FABRIC_HOES = TagManager.ITEMS.makeTag("fabric", "hoes");
    public static final TagKey<Item> FABRIC_PICKAXES = TagManager.ITEMS.makeTag("fabric", "pickaxes");
    public static final TagKey<Item> FABRIC_SHEARS = TagManager.ITEMS.makeTag("fabric", "shears");
    public static final TagKey<Item> FABRIC_SHOVELS = TagManager.ITEMS.makeTag("fabric", "shovels");
    public static final TagKey<Item> FABRIC_SWORDS = TagManager.ITEMS.makeTag("fabric", "swords");

    static void prepareTags() {
    }
}
