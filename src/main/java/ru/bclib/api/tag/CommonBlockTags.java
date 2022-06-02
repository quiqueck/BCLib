package ru.bclib.api.tag;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class CommonBlockTags {
    public static final TagKey<Block> BARREL = TagAPI.makeCommonBlockTag("barrel");
    public static final TagKey<Block> BOOKSHELVES = TagAPI.makeCommonBlockTag("bookshelves");
    public static final TagKey<Block> CHEST = TagAPI.makeCommonBlockTag("chest");
    public static final TagKey<Block> END_STONES = TagAPI.makeCommonBlockTag("end_stones");
    public static final TagKey<Block> GEN_END_STONES = END_STONES;
    public static final TagKey<Block> IMMOBILE = TagAPI.makeCommonBlockTag("immobile");
    public static final TagKey<Block> LEAVES = TagAPI.makeCommonBlockTag("leaves");
    public static final TagKey<Block> NETHERRACK = TagAPI.makeCommonBlockTag("netherrack");
    public static final TagKey<Block> NETHER_MYCELIUM = TagAPI.makeCommonBlockTag("nether_mycelium");
    public static final TagKey<Block> NETHER_PORTAL_FRAME = TagAPI.makeCommonBlockTag("nether_pframe");
    public static final TagKey<Block> NETHER_STONES = TagAPI.makeCommonBlockTag("nether_stones");
    public static final TagKey<Block> SAPLINGS = TagAPI.makeCommonBlockTag("saplings");
    public static final TagKey<Block> SOUL_GROUND = TagAPI.makeCommonBlockTag("soul_ground");
    public static final TagKey<Block> WOODEN_BARREL = TagAPI.makeCommonBlockTag("wooden_barrels");
    public static final TagKey<Block> WOODEN_CHEST = TagAPI.makeCommonBlockTag("wooden_chests");
    public static final TagKey<Block> WORKBENCHES = TagAPI.makeCommonBlockTag("workbench");

    public static final TagKey<Block> MINABLE_WITH_HAMMER = TagAPI.makeCommonBlockTag("mineable/hammer");
    public static final TagKey<Block> IS_OBSIDIAN = TagAPI.makeCommonBlockTag("is_obsidian");
}
