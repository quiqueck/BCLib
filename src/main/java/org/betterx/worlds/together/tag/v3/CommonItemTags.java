package org.betterx.worlds.together.tag.v3;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class CommonItemTags {
    public final static TagKey<Item> HAMMERS = TagManager.ITEMS.makeCommonTag("hammers");
    public static final TagKey<Item> BARREL = TagManager.ITEMS.makeCommonTag("barrel");
    public static final TagKey<Item> CHEST = TagManager.ITEMS.makeCommonTag("chest");
    public static final TagKey<Item> SHEARS = TagManager.ITEMS.makeCommonTag("shears");
    public static final TagKey<Item> FURNACES = TagManager.ITEMS.makeCommonTag("furnaces");
    public static final TagKey<Item> IRON_INGOTS = TagManager.ITEMS.makeCommonTag("iron_ingots");
    public static final TagKey<Item> LEAVES = TagManager.ITEMS.makeCommonTag("leaves");
    public static final TagKey<Item> SAPLINGS = TagManager.ITEMS.makeCommonTag("saplings");
    public static final TagKey<Item> SEEDS = TagManager.ITEMS.makeCommonTag("seeds");
    public static final TagKey<Item> SOUL_GROUND = TagManager.ITEMS.makeCommonTag("soul_ground");
    public static final TagKey<Item> WOODEN_BARREL = TagManager.ITEMS.makeCommonTag("wooden_barrels");
    public static final TagKey<Item> WOODEN_CHEST = TagManager.ITEMS.makeCommonTag("wooden_chests");
    public static final TagKey<Item> WORKBENCHES = TagManager.ITEMS.makeCommonTag("workbench");

    public static final TagKey<Item> WATER_BOTTLES = TagManager.ITEMS.makeCommonTag("water_bottles");
    public static final TagKey<Item> COMPOSTABLE = TagManager.ITEMS.makeCommonTag("compostable");
    ;

    static void prepareTags() {
        TagManager.ITEMS.add(SOUL_GROUND, Blocks.SOUL_SAND.asItem(), Blocks.SOUL_SOIL.asItem());

        TagManager.ITEMS.add(CommonItemTags.CHEST, Items.CHEST);
        TagManager.ITEMS.add(CommonItemTags.IRON_INGOTS, Items.IRON_INGOT);
        TagManager.ITEMS.add(CommonItemTags.FURNACES, Blocks.FURNACE.asItem());
        TagManager.ITEMS.add(CommonItemTags.WATER_BOTTLES, Items.WATER_BUCKET);
    }
}
