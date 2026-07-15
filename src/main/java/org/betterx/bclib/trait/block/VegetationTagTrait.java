package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import org.betterx.wover.block.api.BlockDefinition;
import org.betterx.wover.block.api.trait.BlockTraitKey;
import org.betterx.wover.block.api.trait.GenericBlockTrait;
import org.betterx.wover.block.impl.trait.BlockTraitImpl;
import org.betterx.wover.tag.api.predefined.CommonBlockTags;
import org.betterx.wover.tag.api.predefined.CommonItemTags;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.Nullable;

/**
 * Marker trait that tags a block into one of the common "vegetation" families at datagen. Replaces the old
 * {@code BehaviourPlant} / {@code BehaviourSeed} / {@code BehaviourWaterPlant} marker interfaces, which used
 * to derive these tags via {@code instanceof} scanning in {@code BCLAutoBlock/ItemTagProvider}. Add the
 * matching factory ({@link #plant()} / {@link #seed()} / {@link #waterPlant()}) to a block at registration.
 */
public class VegetationTagTrait extends BlockTraitImpl<Block, GenericBlockTrait> implements GenericBlockTrait {
    public static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "vegetation_tag");

    private static final VegetationTagTrait PLANT = new VegetationTagTrait(CommonBlockTags.PLANT, null);
    private static final VegetationTagTrait SEED = new VegetationTagTrait(CommonBlockTags.SEEDS, CommonItemTags.SEEDS);
    private static final VegetationTagTrait WATER_PLANT = new VegetationTagTrait(CommonBlockTags.WATER_PLANT, null);

    /** Tags the block as {@link CommonBlockTags#PLANT} (replaces {@code BehaviourPlant}). */
    public static VegetationTagTrait plant() {
        return PLANT;
    }

    /** Tags the block and its item as {@link CommonBlockTags#SEEDS} (replaces {@code BehaviourSeed}). */
    public static VegetationTagTrait seed() {
        return SEED;
    }

    /** Tags the block as {@link CommonBlockTags#WATER_PLANT} (replaces {@code BehaviourWaterPlant}). */
    public static VegetationTagTrait waterPlant() {
        return WATER_PLANT;
    }

    private final TagKey<Block> blockTag;
    private final @Nullable TagKey<Item> itemTag;

    private VegetationTagTrait(TagKey<Block> blockTag, @Nullable TagKey<Item> itemTag) {
        this.blockTag = blockTag;
        this.itemTag = itemTag;
    }

    @Override
    public BlockTraitKey key() {
        return KEY;
    }

    @Override
    public GenericBlockTrait forRuntime() {
        return this;
    }

    @Override
    public void configure(BlockDefinition<Block, ? extends BlockDefinition<Block, ?>> definition) {
        super.configure(definition);
        definition.addTags(blockTag);
        if (itemTag != null) {
            definition.addItemTags(itemTag);
        }
    }
}
