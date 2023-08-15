package org.betterx.worlds.together.tag.v3;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class CommonBlockTags {
    public static final TagKey<Block> BARREL = TagManager.BLOCKS.makeCommonTag("barrel");
    public static final TagKey<Block> BOOKSHELVES = TagManager.BLOCKS.makeCommonTag("bookshelves");
    public static final TagKey<Block> CHEST = TagManager.BLOCKS.makeCommonTag("chest");
    public static final TagKey<Block> COMPOSTER = TagManager.BLOCKS.makeCommonTag("composter");
    public static final TagKey<Block> END_STONES = TagManager.BLOCKS.makeCommonTag("end_stones");
    public static final TagKey<Block> GEN_END_STONES = END_STONES;
    public static final TagKey<Block> IMMOBILE = TagManager.BLOCKS.makeCommonTag("immobile");
    public static final TagKey<Block> LEAVES = TagManager.BLOCKS.makeCommonTag("leaves");
    public static final TagKey<Block> NETHERRACK = TagManager.BLOCKS.makeCommonTag("netherrack");
    public static final TagKey<Block> MYCELIUM = TagManager.BLOCKS.makeCommonTag("mycelium");
    public static final TagKey<Block> NETHER_MYCELIUM = TagManager.BLOCKS.makeCommonTag("nether_mycelium");
    public static final TagKey<Block> NETHER_PORTAL_FRAME = TagManager.BLOCKS.makeCommonTag("nether_pframe");
    public static final TagKey<Block> NETHER_STONES = TagManager.BLOCKS.makeCommonTag("nether_stones");
    public static final TagKey<Block> NETHER_ORES = TagManager.BLOCKS.makeCommonTag("nether_ores");
    public static final TagKey<Block> ORES = TagManager.BLOCKS.makeCommonTag("ores");
    public static final TagKey<Block> END_ORES = TagManager.BLOCKS.makeCommonTag("end_ores");
    public static final TagKey<Block> SAPLINGS = TagManager.BLOCKS.makeCommonTag("saplings");
    public static final TagKey<Block> SEEDS = TagManager.BLOCKS.makeCommonTag("seeds");
    public static final TagKey<Block> SOUL_GROUND = TagManager.BLOCKS.makeCommonTag("soul_ground");
    public static final TagKey<Block> SCULK_LIKE = TagManager.BLOCKS.makeCommonTag("sculk_like");
    public static final TagKey<Block> WOODEN_BARREL = TagManager.BLOCKS.makeCommonTag("wooden_barrels");
    public static final TagKey<Block> WOODEN_CHEST = TagManager.BLOCKS.makeCommonTag("wooden_chests");
    public static final TagKey<Block> WOODEN_COMPOSTER = TagManager.BLOCKS.makeCommonTag("wooden_composter");
    public static final TagKey<Block> WORKBENCHES = TagManager.BLOCKS.makeCommonTag("workbench");

    public static final TagKey<Block> DRAGON_IMMUNE = TagManager.BLOCKS.makeCommonTag("dragon_immune");

    public static final TagKey<Block> MINABLE_WITH_HAMMER = TagManager.BLOCKS.makeCommonTag("mineable/hammer");

    public static final TagKey<Block> IS_OBSIDIAN = TagManager.BLOCKS.makeCommonTag("is_obsidian");
    public static final TagKey<Block> TERRAIN = TagManager.BLOCKS.makeCommonTag("terrain");
    public static final TagKey<Block> GRASS_SOIL = TagManager.BLOCKS.makeCommonTag("grass_soil");
    public static final TagKey<Block> NETHER_TERRAIN = TagManager.BLOCKS.makeCommonTag("nether_terrain");
    public static final TagKey<Block> BUDDING_BLOCKS = TagManager.BLOCKS.makeCommonTag("budding_blocks");
    public static final TagKey<Block> WATER_PLANT = TagManager.BLOCKS.makeCommonTag("water_plant");
    ;
    public static final TagKey<Block> PLANT = TagManager.BLOCKS.makeCommonTag("plant");
    ;

    static void prepareTags() {
        TagManager.BLOCKS.addOtherTags(MINABLE_WITH_HAMMER, BlockTags.MINEABLE_WITH_PICKAXE);
        TagManager.BLOCKS.add(SCULK_LIKE, Blocks.SCULK);
        TagManager.BLOCKS.addOtherTags(DRAGON_IMMUNE, BlockTags.DRAGON_IMMUNE);

        TagManager.BLOCKS.add(END_STONES, Blocks.END_STONE);
        TagManager.BLOCKS.addOtherTags(NETHER_STONES, BlockTags.BASE_STONE_NETHER);

        TagManager.BLOCKS.add(
                NETHERRACK,
                Blocks.NETHERRACK,
                Blocks.NETHER_QUARTZ_ORE,
                Blocks.NETHER_GOLD_ORE,
                Blocks.CRIMSON_NYLIUM,
                Blocks.WARPED_NYLIUM
        );

        TagManager.BLOCKS.add(NETHER_ORES, Blocks.NETHER_QUARTZ_ORE, Blocks.NETHER_GOLD_ORE);
        TagManager.BLOCKS.add(SOUL_GROUND, Blocks.SOUL_SAND, Blocks.SOUL_SOIL);

        TagManager.BLOCKS.add(IS_OBSIDIAN, Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN);

        TagManager.BLOCKS.add(MYCELIUM, Blocks.MYCELIUM);
        TagManager.BLOCKS.addOtherTags(MYCELIUM, NETHER_MYCELIUM);

        TagManager.BLOCKS.addOtherTags(GRASS_SOIL, BlockTags.DIRT, TERRAIN, BlockTags.LOGS, BlockTags.PLANKS);


        TagManager.BLOCKS.add(
                TERRAIN,
                Blocks.MAGMA_BLOCK,
                Blocks.GRAVEL,
                Blocks.SAND,
                Blocks.RED_SAND,
                Blocks.GLOWSTONE,
                Blocks.BONE_BLOCK,
                Blocks.SCULK
        );
        TagManager.BLOCKS.addOtherTags(
                TERRAIN,
                NETHER_TERRAIN,
                BlockTags.DRIPSTONE_REPLACEABLE,
                BlockTags.BASE_STONE_OVERWORLD,
                BlockTags.NYLIUM,
                MYCELIUM,
                END_STONES
        );

        TagManager.BLOCKS.add(
                NETHER_TERRAIN,
                Blocks.MAGMA_BLOCK,
                Blocks.GRAVEL,
                Blocks.RED_SAND,
                Blocks.GLOWSTONE,
                Blocks.BONE_BLOCK,
                Blocks.BLACKSTONE
        );
        TagManager.BLOCKS.addOtherTags(
                NETHER_TERRAIN,
                NETHERRACK,
                BlockTags.NYLIUM,
                NETHER_ORES,
                SOUL_GROUND,
                NETHER_MYCELIUM
        );

        TagManager.BLOCKS.add(CommonBlockTags.BOOKSHELVES, Blocks.BOOKSHELF);
        TagManager.BLOCKS.add(CommonBlockTags.CHEST, Blocks.CHEST);

        TagManager.BLOCKS.add(
                BlockTags.NETHER_CARVER_REPLACEABLES,
                Blocks.BASALT,
                Blocks.RED_SAND,
                Blocks.MAGMA_BLOCK,
                Blocks.SCULK
        );
        TagManager.BLOCKS.addOtherTags(
                BlockTags.NETHER_CARVER_REPLACEABLES,
                CommonBlockTags.NETHER_STONES,
                CommonBlockTags.NETHERRACK
        );

        TagManager.BLOCKS.addOtherTags(
                BlockTags.MINEABLE_WITH_AXE,
                WOODEN_BARREL,
                WOODEN_COMPOSTER,
                WOODEN_CHEST,
                WORKBENCHES
        );

        TagManager.BLOCKS.add(WATER_PLANT, Blocks.KELP, Blocks.KELP_PLANT, Blocks.SEAGRASS, Blocks.TALL_SEAGRASS);
        TagManager.BLOCKS.add(
                SAPLINGS,
                Blocks.OAK_SAPLING,
                Blocks.SPRUCE_SAPLING,
                Blocks.BIRCH_SAPLING,
                Blocks.JUNGLE_SAPLING,
                Blocks.ACACIA_SAPLING,
                Blocks.DARK_OAK_SAPLING,
                Blocks.CHERRY_SAPLING,
                Blocks.BAMBOO_SAPLING,
                Blocks.MANGROVE_PROPAGULE
        );
        TagManager.BLOCKS.addOtherTags(PLANT, SAPLINGS);
        TagManager.BLOCKS.add(
                PLANT,
                Blocks.MANGROVE_LEAVES,
                Blocks.GRASS,
                Blocks.FERN,
                Blocks.DANDELION,
                Blocks.TORCHFLOWER,
                Blocks.POPPY,
                Blocks.BLUE_ORCHID,
                Blocks.ALLIUM,
                Blocks.AZURE_BLUET,
                Blocks.RED_TULIP,
                Blocks.ORANGE_TULIP,
                Blocks.WHITE_TULIP,
                Blocks.PINK_TULIP,
                Blocks.OXEYE_DAISY,
                Blocks.CORNFLOWER,
                Blocks.WITHER_ROSE,
                Blocks.LILY_OF_THE_VALLEY,
                Blocks.WHEAT,
                Blocks.CACTUS,
                Blocks.SUGAR_CANE,
                Blocks.ATTACHED_PUMPKIN_STEM,
                Blocks.ATTACHED_MELON_STEM,
                Blocks.PUMPKIN_STEM,
                Blocks.MELON_STEM,
                Blocks.VINE,
                Blocks.LILY_PAD,
                Blocks.COCOA,
                Blocks.CARROTS,
                Blocks.POTATOES,
                Blocks.SUNFLOWER,
                Blocks.LILAC,
                Blocks.ROSE_BUSH,
                Blocks.PEONY,
                Blocks.TALL_GRASS,
                Blocks.LARGE_FERN,
                Blocks.TORCHFLOWER_CROP,
                Blocks.PITCHER_CROP,
                Blocks.PITCHER_PLANT,
                Blocks.BEETROOTS,
                Blocks.BAMBOO,
                Blocks.SWEET_BERRY_BUSH,
                Blocks.CAVE_VINES,
                Blocks.CAVE_VINES_PLANT,
                Blocks.SPORE_BLOSSOM,
                Blocks.AZALEA,
                Blocks.FLOWERING_AZALEA,
                Blocks.PINK_PETALS,
                Blocks.BIG_DRIPLEAF,
                Blocks.BIG_DRIPLEAF_STEM,
                Blocks.SMALL_DRIPLEAF
        );
    }
}
