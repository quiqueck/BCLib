package org.betterx.bclib.complexmaterials;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.blocks.*;
import org.betterx.bclib.complexmaterials.entry.BlockEntry;
import org.betterx.bclib.complexmaterials.entry.RecipeEntry;
import org.betterx.bclib.recipes.BCLRecipeBuilder;
import org.betterx.worlds.together.tag.v3.CommonBlockTags;
import org.betterx.worlds.together.tag.v3.CommonItemTags;
import org.betterx.worlds.together.tag.v3.CommonPoiTags;
import org.betterx.worlds.together.tag.v3.TagManager;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;

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

    public final MapColor planksColor;
    public final MapColor woodColor;

    public final WoodType woodType;

    public WoodenComplexMaterial(
            String modID,
            String baseName,
            String receipGroupPrefix,
            MapColor woodColor,
            MapColor planksColor
    ) {
        super(modID, baseName, receipGroupPrefix);
        this.planksColor = planksColor;
        this.woodColor = woodColor;
        this.woodType = WoodType.register(new BCLWoodType(modID, baseName));
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
    protected void initDefault(BlockBehaviour.Properties blockSettings, Item.Properties itemSettings) {
        initBase(blockSettings, itemSettings);
        initStorage(blockSettings, itemSettings);
        initDecorations(blockSettings, itemSettings);
    }

    final protected void initBase(BlockBehaviour.Properties blockSettings, Item.Properties itemSettings) {
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
                (complexMaterial, settings) -> new BaseGateBlock(getBlock(BLOCK_PLANKS), this.woodType)
        )
                .setBlockTags(BlockTags.FENCE_GATES));

        addBlockEntry(new BlockEntry(
                BLOCK_BUTTON,
                (complexMaterial, settings) -> new BaseWoodenButtonBlock(
                        getBlock(BLOCK_PLANKS),
                        this.woodType.setType()
                )
        )
                .setBlockTags(BlockTags.BUTTONS, BlockTags.WOODEN_BUTTONS)
                .setItemTags(ItemTags.BUTTONS, ItemTags.WOODEN_BUTTONS));

        addBlockEntry(new BlockEntry(
                BLOCK_PRESSURE_PLATE,
                (complexMaterial, settings) -> new WoodenPressurePlateBlock(
                        getBlock(BLOCK_PLANKS),
                        this.woodType.setType()
                )
        )
                .setBlockTags(BlockTags.PRESSURE_PLATES, BlockTags.WOODEN_PRESSURE_PLATES)
                .setItemTags(ItemTags.WOODEN_PRESSURE_PLATES));

        addBlockEntry(new BlockEntry(
                BLOCK_TRAPDOOR,
                (complexMaterial, settings) -> new BaseTrapdoorBlock(getBlock(BLOCK_PLANKS), this.woodType.setType())
        )
                .setBlockTags(BlockTags.TRAPDOORS, BlockTags.WOODEN_TRAPDOORS)
                .setItemTags(ItemTags.TRAPDOORS, ItemTags.WOODEN_TRAPDOORS));

        addBlockEntry(new BlockEntry(
                BLOCK_DOOR,
                (complexMaterial, settings) -> new BaseDoorBlock(getBlock(BLOCK_PLANKS), this.woodType.setType())
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

    final protected void initStorage(BlockBehaviour.Properties blockSettings, Item.Properties itemSettings) {
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

    protected void initDecorations(BlockBehaviour.Properties blockSettings, Item.Properties itemSettings) {
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
        addRecipeEntry(new RecipeEntry("planks", (material, id) -> {
            Block log_stripped = getBlock(BLOCK_STRIPPED_LOG);
            Block bark_stripped = getBlock(BLOCK_STRIPPED_BARK);
            Block log = getBlock(BLOCK_LOG);
            Block bark = getBlock(BLOCK_BARK);
            BCLRecipeBuilder.crafting(id, planks)
                            .setOutputCount(4)
                            .shapeless()
                            .addMaterial('#', log, bark, log_stripped, bark_stripped)
                            .setGroup("planks")
                            .setCategory(RecipeCategory.BUILDING_BLOCKS)
                            .build();
        }));
        addRecipeEntry(new RecipeEntry("stairs", (material, id) -> {
            BCLRecipeBuilder.crafting(id, getBlock(BLOCK_STAIRS))
                            .setOutputCount(4)
                            .setShape("#  ", "## ", "###")
                            .addMaterial('#', planks)
                            .setGroup("stairs")
                            .setCategory(RecipeCategory.BUILDING_BLOCKS)
                            .build();
        }));
        addRecipeEntry(new RecipeEntry("slab", (material, id) -> {
            BCLRecipeBuilder.crafting(id, getBlock(BLOCK_SLAB))
                            .setOutputCount(6)
                            .setShape("###")
                            .addMaterial('#', planks)
                            .setGroup("slab")
                            .setCategory(RecipeCategory.BUILDING_BLOCKS)
                            .build();
        }));
        addRecipeEntry(new RecipeEntry("fence", (material, id) -> {
            BCLRecipeBuilder.crafting(id, getBlock(BLOCK_FENCE))
                            .setOutputCount(3)
                            .setShape("#I#", "#I#")
                            .addMaterial('#', planks)
                            .addMaterial('I', Items.STICK)
                            .setGroup("fence")
                            .setCategory(RecipeCategory.DECORATIONS)
                            .build();
        }));
        addRecipeEntry(new RecipeEntry("gate", (material, id) -> {
            BCLRecipeBuilder.crafting(id, getBlock(BLOCK_GATE))
                            .setShape("I#I", "I#I")
                            .addMaterial('#', planks)
                            .addMaterial('I', Items.STICK)
                            .setGroup("gate")
                            .setCategory(RecipeCategory.REDSTONE)
                            .build();
        }));
        addRecipeEntry(new RecipeEntry("button", (material, id) -> {
            BCLRecipeBuilder.crafting(id, getBlock(BLOCK_BUTTON))
                            .shapeless()
                            .addMaterial('#', planks)
                            .setGroup("button")
                            .setCategory(RecipeCategory.REDSTONE)
                            .build();
        }));
        addRecipeEntry(new RecipeEntry("pressure_plate", (material, id) -> {
            BCLRecipeBuilder.crafting(id, getBlock(BLOCK_PRESSURE_PLATE))
                            .setShape("##")
                            .addMaterial('#', planks)
                            .setGroup("pressure_plate")
                            .setCategory(RecipeCategory.REDSTONE)
                            .build();
        }));
        addRecipeEntry(new RecipeEntry("trapdoor", (material, id) -> {
            BCLRecipeBuilder.crafting(id, getBlock(BLOCK_TRAPDOOR))
                            .setOutputCount(2)
                            .setShape("###", "###")
                            .addMaterial('#', planks)
                            .setGroup("trapdoor")
                            .setCategory(RecipeCategory.REDSTONE)
                            .build();
        }));
        addRecipeEntry(new RecipeEntry("door", (material, id) -> {
            BCLRecipeBuilder.crafting(id, getBlock(BLOCK_DOOR))
                            .setOutputCount(3)
                            .setShape("##", "##", "##")
                            .addMaterial('#', planks)
                            .setGroup("door")
                            .setCategory(RecipeCategory.REDSTONE)
                            .build();
        }));
        addRecipeEntry(new RecipeEntry("crafting_table", (material, id) -> {
            BCLRecipeBuilder.crafting(id, getBlock(BLOCK_CRAFTING_TABLE))
                            .setShape("##", "##")
                            .addMaterial('#', planks)
                            .setGroup("table")
                            .setCategory(RecipeCategory.DECORATIONS)
                            .build();
        }));
        addRecipeEntry(new RecipeEntry("ladder", (material, id) -> {
            BCLRecipeBuilder.crafting(id, getBlock(BLOCK_LADDER))
                            .setOutputCount(3)
                            .setShape("I I", "I#I", "I I")
                            .addMaterial('#', planks)
                            .addMaterial('I', Items.STICK)
                            .setGroup("ladder")
                            .setCategory(RecipeCategory.DECORATIONS)
                            .build();
        }));
        addRecipeEntry(new RecipeEntry("sign", (material, id) -> {
            BCLRecipeBuilder.crafting(id, getBlock(BLOCK_SIGN))
                            .setOutputCount(3)
                            .setShape("###", "###", " I ")
                            .addMaterial('#', planks)
                            .addMaterial('I', Items.STICK)
                            .setGroup("sign")
                            .setCategory(RecipeCategory.DECORATIONS)
                            .build();
        }));
        addRecipeEntry(new RecipeEntry("chest", (material, id) -> {
            BCLRecipeBuilder.crafting(id, getBlock(BLOCK_CHEST))
                            .setShape("###", "# #", "###")
                            .addMaterial('#', planks)
                            .setGroup("chest")
                            .setCategory(RecipeCategory.DECORATIONS)
                            .build();
        }));
        addRecipeEntry(new RecipeEntry("barrel", (material, id) -> {
            BCLRecipeBuilder.crafting(id, getBlock(BLOCK_BARREL))
                            .setShape("#S#", "# #", "#S#")
                            .addMaterial('#', planks)
                            .addMaterial('S', getBlock(BLOCK_SLAB))
                            .setGroup("barrel")
                            .setCategory(RecipeCategory.DECORATIONS)
                            .build();
        }));
        addRecipeEntry(new RecipeEntry("bookshelf", (material, id) -> {
            BCLRecipeBuilder.crafting(id, getBlock(BLOCK_BOOKSHELF))
                            .setShape("###", "PPP", "###")
                            .addMaterial('#', planks)
                            .addMaterial('P', Items.BOOK)
                            .setGroup("bookshelf")
                            .setCategory(RecipeCategory.BUILDING_BLOCKS)
                            .build();
        }));
        addRecipeEntry(new RecipeEntry("bark", (material, id) -> {
            BCLRecipeBuilder.crafting(id, getBlock(BLOCK_BARK))
                            .setShape("##", "##")
                            .addMaterial('#', getBlock(BLOCK_LOG))
                            .setOutputCount(3)
                            .setCategory(RecipeCategory.BUILDING_BLOCKS)
                            .build();
        }));
        addRecipeEntry(new RecipeEntry("log", (material, id) -> {
            BCLRecipeBuilder.crafting(id, getBlock(BLOCK_LOG))
                            .setShape("##", "##")
                            .addMaterial('#', getBlock(BLOCK_BARK))
                            .setOutputCount(3)
                            .setCategory(RecipeCategory.BUILDING_BLOCKS)
                            .build();
        }));
        addRecipeEntry(new RecipeEntry("stripped_bark", (material, id) -> {
            BCLRecipeBuilder.crafting(id, getBlock(BLOCK_STRIPPED_BARK))
                            .setShape("##", "##")
                            .addMaterial('#', getBlock(BLOCK_STRIPPED_LOG))
                            .setOutputCount(3)
                            .setCategory(RecipeCategory.BUILDING_BLOCKS)
                            .build();
        }));
        addRecipeEntry(new RecipeEntry("stripped_log", (material, id) -> {
            BCLRecipeBuilder.crafting(id, getBlock(BLOCK_STRIPPED_LOG))
                            .setShape("##", "##")
                            .addMaterial('#', getBlock(BLOCK_STRIPPED_BARK))
                            .setOutputCount(3)
                            .setCategory(RecipeCategory.BUILDING_BLOCKS)
                            .build();
        }));
        addRecipeEntry(new RecipeEntry("composter", (material, id) -> {
            BCLRecipeBuilder.crafting(id, getBlock(BLOCK_COMPOSTER))
                            .setShape("# #", "# #", "###")
                            .addMaterial('#', getBlock(BLOCK_SLAB))
                            .setGroup("composter")
                            .setCategory(RecipeCategory.DECORATIONS)
                            .build();
        }));
    }
}