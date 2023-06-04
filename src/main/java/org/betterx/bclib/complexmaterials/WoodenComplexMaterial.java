package org.betterx.bclib.complexmaterials;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.complexmaterials.entry.SlotMap;
import org.betterx.bclib.complexmaterials.set.wood.WoodSlots;
import org.betterx.bclib.items.boat.BoatTypeOverride;
import org.betterx.worlds.together.tag.v3.TagManager;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;

import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

public class WoodenComplexMaterial extends ComplexMaterialSet<WoodenComplexMaterial> {
    public static final ResourceLocation MATERIAL_ID = BCLib.makeID("wooden_material");

    public static final String BLOCK_CRAFTING_TABLE = WoodSlots.CRAFTING_TABLE.suffix;
    public static final String BLOCK_STRIPPED_BARK = WoodSlots.STRIPPED_BARK.suffix;
    public static final String BLOCK_STRIPPED_LOG = WoodSlots.STRIPPED_LOG.suffix;
    public static final String BLOCK_PRESSURE_PLATE = WoodSlots.PRESSURE_PLATE.suffix;
    public static final String BLOCK_BOOKSHELF = WoodSlots.BOOKSHELF.suffix;
    public static final String BLOCK_COMPOSTER = WoodSlots.COMPOSTER.suffix;
    public static final String BLOCK_TRAPDOOR = WoodSlots.TRAPDOOR.suffix;
    public static final String BLOCK_BARREL = WoodSlots.BARREL.suffix;
    public static final String BLOCK_BUTTON = WoodSlots.BUTTON.suffix;
    public static final String BLOCK_LADDER = WoodSlots.LADDER.suffix;
    public static final String BLOCK_PLANKS = WoodSlots.PLANKS.suffix;
    public static final String BLOCK_STAIRS = WoodSlots.STAIRS.suffix;
    public static final String BLOCK_CHEST = WoodSlots.CHEST.suffix;
    public static final String BLOCK_FENCE = WoodSlots.FENCE.suffix;
    public static final String BLOCK_BARK = WoodSlots.BARK.suffix;
    public static final String BLOCK_DOOR = WoodSlots.DOOR.suffix;
    public static final String BLOCK_GATE = WoodSlots.GATE.suffix;
    public static final String BLOCK_SIGN = WoodSlots.SIGN.suffix;
    public static final String BLOCK_WALL_SIGN = WoodSlots.WALL_SIGN;
    public static final String BLOCK_SLAB = WoodSlots.SLAB.suffix;
    public static final String BLOCK_LOG = WoodSlots.LOG.suffix;
    public static final String ITEM_BOAT = WoodSlots.BOAT.suffix;
    public static final String ITEM_CHEST_BOAT = WoodSlots.CHEST_BOAT.suffix;

    public static final String TAG_LOGS = "logs";

    public final MapColor planksColor;
    public final MapColor woodColor;
    @Nullable
    protected BoatTypeOverride boatType;

    public final BCLWoodTypeWrapper woodType;

    public WoodenComplexMaterial(
            String modID,
            String baseName,
            String receipGroupPrefix,
            MapColor woodColor,
            MapColor planksColor
    ) {
        this(modID, baseName, receipGroupPrefix, woodColor, planksColor, null);
    }

    public WoodenComplexMaterial(
            String modID,
            String baseName,
            String receipGroupPrefix,
            MapColor woodColor,
            MapColor planksColor,
            BoatTypeOverride boatType
    ) {
        super(modID, baseName, receipGroupPrefix);
        this.planksColor = planksColor;
        this.woodColor = woodColor;
        this.woodType = createWoodTypeBuilder().build();
        this.boatType = boatType;
    }

    protected BCLWoodTypeWrapper.Builder createWoodTypeBuilder() {
        return BCLWoodTypeWrapper.create(getModID(), getBaseName()).setColor(planksColor);
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
    protected SlotMap<WoodenComplexMaterial> createMaterialSlots() {
        return SlotMap.of(
                WoodSlots.STRIPPED_LOG,
                WoodSlots.STRIPPED_BARK,
                WoodSlots.LOG,
                WoodSlots.BARK,
                WoodSlots.PLANKS,
                WoodSlots.STAIRS,
                WoodSlots.SLAB,
                WoodSlots.FENCE,
                WoodSlots.GATE,
                WoodSlots.BUTTON,
                WoodSlots.PRESSURE_PLATE,
                WoodSlots.TRAPDOOR,
                WoodSlots.DOOR,
                WoodSlots.LADDER,
                WoodSlots.SIGN,
                WoodSlots.CHEST,
                WoodSlots.BARREL,
                WoodSlots.CRAFTING_TABLE,
                WoodSlots.BOOKSHELF,
                WoodSlots.COMPOSTER
        );
    }

    @Override
    protected void initFlammable(FlammableBlockRegistry registry) {
        final Consumer<Block> addFlammableHardWood = (Block block) -> registry.add(block, 5, 5);
        getBlocks().forEach(block -> {
            registry.add(block, 5, 20);
        });

        ifBlockPresent(WoodSlots.LOG, addFlammableHardWood);
        ifBlockPresent(WoodSlots.BARK, addFlammableHardWood);
        ifBlockPresent(WoodSlots.STRIPPED_LOG, addFlammableHardWood);
        ifBlockPresent(WoodSlots.STRIPPED_BARK, addFlammableHardWood);
    }


    public final void initBoatType() {
        if (getBoatType() == null) {
            boatType = supplyBoatType();
        }
    }

    protected BoatTypeOverride supplyBoatType() {
        return BoatTypeOverride.create(
                getModID(),
                getBaseName(),
                getBlock(WoodSlots.PLANKS)
        );
    }

    public BoatTypeOverride getBoatType() {
        return boatType;
    }
}