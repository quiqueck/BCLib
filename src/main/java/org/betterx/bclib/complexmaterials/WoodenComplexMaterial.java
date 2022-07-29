package org.betterx.bclib.complexmaterials;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.blocks.*;
import org.betterx.bclib.complexmaterials.entry.BlockEntry;
import org.betterx.bclib.complexmaterials.entry.RecipeEntry;
import org.betterx.bclib.recipes.GridRecipe;
import org.betterx.worlds.together.tag.v3.CommonBlockTags;
import org.betterx.worlds.together.tag.v3.CommonItemTags;
import org.betterx.worlds.together.tag.v3.CommonPoiTags;
import org.betterx.worlds.together.tag.v3.TagManager;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MaterialColor;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;

public class WoodenComplexMaterial extends ComplexMaterial {
    public static final ResourceLocation MATERIAL_ID = BCLib.makeID("wooden_material");

    public static final String BLOCK_CRAFTING_TABLE = "crafting_table";
    public static final String BLOCK_STRIPPED_BARK = "stripped_bark";
    public static final String BLOCK_STRIPPED_LOG = "stripped_log";
    public static final String BLOCK_PRESSURE_PLATE = "plate";
    public static final String BLOCK_BOOKSHELF = "bookshelf";
    public static final String BLOCK_COMPOSTER = "composter";
    public static final String BLOCK_TRAPDOOR = "trapdoor";
    public static final String BLOCK_BARREL = "barrel";
    public static final String BLOCK_BUTTON = "button";
    public static final String BLOCK_LADDER = "ladder";
    public static final String BLOCK_PLANKS = "planks";
    public static final String BLOCK_STAIRS = "stairs";
    public static final String BLOCK_CHEST = "chest";
    public static final String BLOCK_FENCE = "fence";
    public static final String BLOCK_BARK = "bark";
    public static final String BLOCK_DOOR = "door";
    public static final String BLOCK_GATE = "gate";
    public static final String BLOCK_SIGN = "sign";
    public static final String BLOCK_SLAB = "slab";
    public static final String BLOCK_LOG = "log";

    public static final String TAG_LOGS = "logs";

    public final MaterialColor planksColor;
    public final MaterialColor woodColor;

    public WoodenComplexMaterial(
            String modID,
            String baseName,
            String receipGroupPrefix,
            MaterialColor woodColor,
            MaterialColor planksColor
    ) {
        super(modID, baseName, receipGroupPrefix);
        this.planksColor = planksColor;
        this.woodColor = woodColor;
    }

    @Override
    protected FabricBlockSettings getBlockSettings() {
        return FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)
                                  .mapColor(planksColor);
    }

    @Override
    public ResourceLocation getMaterialID() {
        return MATERIAL_ID;
    }

    @Override
    protected void initTags() {
        addBlockTag(TagManager.BLOCKS.makeTag(getModID(), getBaseName() + "_logs"));
        addItemTag(TagManager.ITEMS.makeTag(getModID(), getBaseName() + "_logs"));
    }

    @Override
    protected void initDefault(FabricBlockSettings blockSettings, FabricItemSettings itemSettings) {
        initBase(blockSettings, itemSettings);
        initStorage(blockSettings, itemSettings);
        initDecorations(blockSettings, itemSettings);
    }

    final protected void initBase(FabricBlockSettings blockSettings, FabricItemSettings itemSettings) {
        TagKey<Block> tagBlockLog = getBlockTag(TAG_LOGS);
        TagKey<Item> tagItemLog = getItemTag(TAG_LOGS);

        addBlockEntry(
                new BlockEntry(BLOCK_STRIPPED_LOG, (complexMaterial, settings) -> new BaseRotatedPillarBlock(settings))
                        .setBlockTags(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN, tagBlockLog)
                        .setItemTags(ItemTags.LOGS, ItemTags.LOGS_THAT_BURN, tagItemLog)
        );
        addBlockEntry(
                new BlockEntry(BLOCK_STRIPPED_BARK, (complexMaterial, settings) -> new BaseBarkBlock(settings))
                        .setBlockTags(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN, tagBlockLog)
                        .setItemTags(ItemTags.LOGS, ItemTags.LOGS_THAT_BURN, tagItemLog)
        );

        addBlockEntry(
                new BlockEntry(
                        BLOCK_LOG,
                        (complexMaterial, settings) -> new BaseStripableLogBlock(
                                woodColor,
                                getBlock(BLOCK_STRIPPED_LOG)
                        )
                )
                        .setBlockTags(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN, tagBlockLog)
                        .setItemTags(ItemTags.LOGS, ItemTags.LOGS_THAT_BURN, tagItemLog)
        );
        addBlockEntry(
                new BlockEntry(
                        BLOCK_BARK,
                        (complexMaterial, settings) -> new StripableBarkBlock(
                                woodColor,
                                getBlock(BLOCK_STRIPPED_BARK)
                        )
                )
                        .setBlockTags(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN, tagBlockLog)
                        .setItemTags(ItemTags.LOGS, ItemTags.LOGS_THAT_BURN, tagItemLog)
        );
        addBlockEntry(new BlockEntry(BLOCK_PLANKS, (complexMaterial, settings) -> new BaseBlock(settings))
                .setBlockTags(BlockTags.PLANKS)
                .setItemTags(ItemTags.PLANKS));

        addBlockEntry(new BlockEntry(
                BLOCK_STAIRS,
                (complexMaterial, settings) -> new BaseStairsBlock(getBlock(BLOCK_PLANKS), false)
        )
                .setBlockTags(BlockTags.WOODEN_STAIRS, BlockTags.STAIRS)
                .setItemTags(ItemTags.WOODEN_STAIRS, ItemTags.STAIRS));

        addBlockEntry(new BlockEntry(
                BLOCK_SLAB,
                (complexMaterial, settings) -> new BaseSlabBlock(getBlock(BLOCK_PLANKS), false)
        )
                .setBlockTags(BlockTags.WOODEN_SLABS, BlockTags.SLABS)
                .setItemTags(ItemTags.WOODEN_SLABS, ItemTags.SLABS));

        addBlockEntry(new BlockEntry(
                BLOCK_FENCE,
                (complexMaterial, settings) -> new BaseFenceBlock(getBlock(BLOCK_PLANKS))
        )
                .setBlockTags(BlockTags.FENCES, BlockTags.WOODEN_FENCES)
                .setItemTags(ItemTags.FENCES, ItemTags.WOODEN_FENCES));

        addBlockEntry(new BlockEntry(
                BLOCK_GATE,
                (complexMaterial, settings) -> new BaseGateBlock(getBlock(BLOCK_PLANKS))
        )
                .setBlockTags(BlockTags.FENCE_GATES));

        addBlockEntry(new BlockEntry(
                BLOCK_BUTTON,
                (complexMaterial, settings) -> new BaseWoodenButtonBlock(getBlock(BLOCK_PLANKS))
        )
                .setBlockTags(BlockTags.BUTTONS, BlockTags.WOODEN_BUTTONS)
                .setItemTags(ItemTags.BUTTONS, ItemTags.WOODEN_BUTTONS));

        addBlockEntry(new BlockEntry(
                BLOCK_PRESSURE_PLATE,
                (complexMaterial, settings) -> new WoodenPressurePlateBlock(getBlock(BLOCK_PLANKS))
        )
                .setBlockTags(BlockTags.PRESSURE_PLATES, BlockTags.WOODEN_PRESSURE_PLATES)
                .setItemTags(ItemTags.WOODEN_PRESSURE_PLATES));

        addBlockEntry(new BlockEntry(
                BLOCK_TRAPDOOR,
                (complexMaterial, settings) -> new BaseTrapdoorBlock(getBlock(BLOCK_PLANKS))
        )
                .setBlockTags(BlockTags.TRAPDOORS, BlockTags.WOODEN_TRAPDOORS)
                .setItemTags(ItemTags.TRAPDOORS, ItemTags.WOODEN_TRAPDOORS));

        addBlockEntry(new BlockEntry(
                BLOCK_DOOR,
                (complexMaterial, settings) -> new BaseDoorBlock(getBlock(BLOCK_PLANKS))
        )
                .setBlockTags(BlockTags.DOORS, BlockTags.WOODEN_DOORS)
                .setItemTags(ItemTags.DOORS, ItemTags.WOODEN_DOORS));

        addBlockEntry(new BlockEntry(
                BLOCK_LADDER,
                (complexMaterial, settings) -> new BaseLadderBlock(getBlock(BLOCK_PLANKS))
        )
                .setBlockTags(BlockTags.CLIMBABLE));

        addBlockEntry(new BlockEntry(
                BLOCK_SIGN,
                (complexMaterial, settings) -> new BaseSignBlock(getBlock(BLOCK_PLANKS))
        )
                .setBlockTags(BlockTags.SIGNS)
                .setItemTags(ItemTags.SIGNS));
    }

    final protected void initStorage(FabricBlockSettings blockSettings, FabricItemSettings itemSettings) {
        addBlockEntry(new BlockEntry(
                BLOCK_CHEST,
                (complexMaterial, settings) -> new BaseChestBlock(getBlock(BLOCK_PLANKS))
        )
                .setBlockTags(CommonBlockTags.CHEST, CommonBlockTags.WOODEN_CHEST)
                .setItemTags(CommonItemTags.CHEST, CommonItemTags.WOODEN_CHEST));

        addBlockEntry(new BlockEntry(
                BLOCK_BARREL,
                (complexMaterial, settings) -> new BaseBarrelBlock(getBlock(BLOCK_PLANKS))
        )
                .setBlockTags(CommonBlockTags.BARREL, CommonBlockTags.WOODEN_BARREL)
                .setItemTags(CommonItemTags.BARREL, CommonItemTags.WOODEN_BARREL));
    }

    final protected void initDecorations(FabricBlockSettings blockSettings, FabricItemSettings itemSettings) {
        addBlockEntry(new BlockEntry(
                        BLOCK_CRAFTING_TABLE,
                        (cmx, settings) -> new BaseCraftingTableBlock(getBlock(BLOCK_PLANKS))
                )
                        .setBlockTags(CommonBlockTags.WORKBENCHES)
                        .setItemTags(CommonItemTags.WORKBENCHES)
        );

        addBlockEntry(new BlockEntry(
                BLOCK_BOOKSHELF,
                (cmx, settings) -> new BaseBookshelfBlock(getBlock(BLOCK_PLANKS))
        )
                .setBlockTags(CommonBlockTags.BOOKSHELVES));

        addBlockEntry(new BlockEntry(
                BLOCK_COMPOSTER,
                (complexMaterial, settings) -> new BaseComposterBlock(getBlock(BLOCK_PLANKS))
        )
                .setBlockTags(CommonPoiTags.FARMER_WORKSTATION));
    }

    @Override
    protected void initFlammable(FlammableBlockRegistry registry) {
        getBlocks().forEach(block -> {
            registry.add(block, 5, 20);
        });

        registry.add(getBlock(BLOCK_LOG), 5, 5);
        registry.add(getBlock(BLOCK_BARK), 5, 5);
        registry.add(getBlock(BLOCK_STRIPPED_LOG), 5, 5);
        registry.add(getBlock(BLOCK_STRIPPED_BARK), 5, 5);
    }

    @Override
    public void initDefaultRecipes() {
        Block planks = getBlock(BLOCK_PLANKS);
        addRecipeEntry(new RecipeEntry("planks", (material, config, id) -> {
            Block log_stripped = getBlock(BLOCK_STRIPPED_LOG);
            Block bark_stripped = getBlock(BLOCK_STRIPPED_BARK);
            Block log = getBlock(BLOCK_LOG);
            Block bark = getBlock(BLOCK_BARK);
            GridRecipe.make(id, planks)
                      .checkConfig(config)
                      .setOutputCount(4)
                      .setList("#")
                      .addMaterial('#', log, bark, log_stripped, bark_stripped)
                      .setGroup(receipGroupPrefix + "_planks")
                      .build();
        }));
        addRecipeEntry(new RecipeEntry("stairs", (material, config, id) -> {
            GridRecipe.make(id, getBlock(BLOCK_STAIRS))
                      .checkConfig(config)
                      .setOutputCount(4)
                      .setShape("#  ", "## ", "###")
                      .addMaterial('#', planks)
                      .setGroup(receipGroupPrefix + "_planks_stairs")
                      .build();
        }));
        addRecipeEntry(new RecipeEntry("slab", (material, config, id) -> {
            GridRecipe.make(id, getBlock(BLOCK_SLAB))
                      .checkConfig(config)
                      .setOutputCount(6)
                      .setShape("###")
                      .addMaterial('#', planks)
                      .setGroup(receipGroupPrefix + "_planks_slabs")
                      .build();
        }));
        addRecipeEntry(new RecipeEntry("fence", (material, config, id) -> {
            GridRecipe.make(id, getBlock(BLOCK_FENCE))
                      .checkConfig(config)
                      .setOutputCount(3)
                      .setShape("#I#", "#I#")
                      .addMaterial('#', planks)
                      .addMaterial('I', Items.STICK)
                      .setGroup(receipGroupPrefix + "_planks_fences")
                      .build();
        }));
        addRecipeEntry(new RecipeEntry("gate", (material, config, id) -> {
            GridRecipe.make(id, getBlock(BLOCK_GATE))
                      .checkConfig(config)
                      .setShape("I#I", "I#I")
                      .addMaterial('#', planks)
                      .addMaterial('I', Items.STICK)
                      .setGroup(receipGroupPrefix + "_planks_gates")
                      .build();
        }));
        addRecipeEntry(new RecipeEntry("button", (material, config, id) -> {
            GridRecipe.make(id, getBlock(BLOCK_BUTTON))
                      .checkConfig(config)
                      .setList("#")
                      .addMaterial('#', planks)
                      .setGroup(receipGroupPrefix + "_planks_buttons")
                      .build();
        }));
        addRecipeEntry(new RecipeEntry("pressure_plate", (material, config, id) -> {
            GridRecipe.make(id, getBlock(BLOCK_PRESSURE_PLATE))
                      .checkConfig(config)
                      .setShape("##")
                      .addMaterial('#', planks)
                      .setGroup(receipGroupPrefix + "_planks_plates")
                      .build();
        }));
        addRecipeEntry(new RecipeEntry("trapdoor", (material, config, id) -> {
            GridRecipe.make(id, getBlock(BLOCK_TRAPDOOR))
                      .checkConfig(config)
                      .setOutputCount(2)
                      .setShape("###", "###")
                      .addMaterial('#', planks)
                      .setGroup(receipGroupPrefix + "_trapdoors")
                      .build();
        }));
        addRecipeEntry(new RecipeEntry("door", (material, config, id) -> {
            GridRecipe.make(id, getBlock(BLOCK_DOOR))
                      .checkConfig(config)
                      .setOutputCount(3)
                      .setShape("##", "##", "##")
                      .addMaterial('#', planks)
                      .setGroup(receipGroupPrefix + "_doors")
                      .build();
        }));
        addRecipeEntry(new RecipeEntry("crafting_table", (material, config, id) -> {
            GridRecipe.make(id, getBlock(BLOCK_CRAFTING_TABLE))
                      .checkConfig(config)
                      .setShape("##", "##")
                      .addMaterial('#', planks)
                      .setGroup(receipGroupPrefix + "_tables")
                      .build();
        }));
        addRecipeEntry(new RecipeEntry("ladder", (material, config, id) -> {
            GridRecipe.make(id, getBlock(BLOCK_LADDER))
                      .checkConfig(config)
                      .setOutputCount(3)
                      .setShape("I I", "I#I", "I I")
                      .addMaterial('#', planks)
                      .addMaterial('I', Items.STICK)
                      .setGroup(receipGroupPrefix + "_ladders")
                      .build();
        }));
        addRecipeEntry(new RecipeEntry("sign", (material, config, id) -> {
            GridRecipe.make(id, getBlock(BLOCK_SIGN))
                      .checkConfig(config)
                      .setOutputCount(3)
                      .setShape("###", "###", " I ")
                      .addMaterial('#', planks)
                      .addMaterial('I', Items.STICK)
                      .setGroup(receipGroupPrefix + "_signs")
                      .build();
        }));
        addRecipeEntry(new RecipeEntry("chest", (material, config, id) -> {
            GridRecipe.make(id, getBlock(BLOCK_CHEST))
                      .checkConfig(config)
                      .setShape("###", "# #", "###")
                      .addMaterial('#', planks)
                      .setGroup(receipGroupPrefix + "_chests")
                      .build();
        }));
        addRecipeEntry(new RecipeEntry("barrel", (material, config, id) -> {
            GridRecipe.make(id, getBlock(BLOCK_BARREL))
                      .checkConfig(config)
                      .setShape("#S#", "# #", "#S#")
                      .addMaterial('#', planks)
                      .addMaterial('S', getBlock(BLOCK_SLAB))
                      .setGroup(receipGroupPrefix + "_barrels")
                      .build();
        }));
        addRecipeEntry(new RecipeEntry("bookshelf", (material, config, id) -> {
            GridRecipe.make(id, getBlock(BLOCK_BOOKSHELF))
                      .checkConfig(config)
                      .setShape("###", "PPP", "###")
                      .addMaterial('#', planks)
                      .addMaterial('P', Items.BOOK)
                      .setGroup(receipGroupPrefix + "_bookshelves")
                      .build();
        }));
        addRecipeEntry(new RecipeEntry("bark", (material, config, id) -> {
            GridRecipe.make(id, getBlock(BLOCK_BARK))
                      .checkConfig(config)
                      .setShape("##", "##")
                      .addMaterial('#', getBlock(BLOCK_LOG))
                      .setOutputCount(3)
                      .build();
        }));
        addRecipeEntry(new RecipeEntry("log", (material, config, id) -> {
            GridRecipe.make(id, getBlock(BLOCK_LOG))
                      .checkConfig(config)
                      .setShape("##", "##")
                      .addMaterial('#', getBlock(BLOCK_BARK))
                      .setOutputCount(3)
                      .build();
        }));
        addRecipeEntry(new RecipeEntry("stripped_bark", (material, config, id) -> {
            GridRecipe.make(id, getBlock(BLOCK_STRIPPED_BARK))
                      .checkConfig(config)
                      .setShape("##", "##")
                      .addMaterial('#', getBlock(BLOCK_STRIPPED_LOG))
                      .setOutputCount(3)
                      .build();
        }));
        addRecipeEntry(new RecipeEntry("stripped_log", (material, config, id) -> {
            GridRecipe.make(id, getBlock(BLOCK_STRIPPED_LOG))
                      .checkConfig(config)
                      .setShape("##", "##")
                      .addMaterial('#', getBlock(BLOCK_STRIPPED_BARK))
                      .setOutputCount(3)
                      .build();
        }));
        addRecipeEntry(new RecipeEntry("composter", (material, config, id) -> {
            GridRecipe.make(id, getBlock(BLOCK_COMPOSTER))
                      .checkConfig(config)
                      .setShape("# #", "# #", "###")
                      .addMaterial('#', getBlock(BLOCK_SLAB))
                      .build();
        }));
    }
}