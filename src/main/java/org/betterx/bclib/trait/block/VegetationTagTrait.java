package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import org.betterx.wover.block.api.BlockDefinition;
import org.betterx.wover.block.api.trait.BlockTraitKey;
import org.betterx.wover.block.api.trait.GenericBlockTrait;
import org.betterx.wover.block.impl.trait.BlockTraitImpl;
import org.betterx.wover.tag.api.predefined.CommonBlockTags;
import org.betterx.wover.tag.api.predefined.CommonItemTags;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Marker trait that tags a block into one of the common "vegetation" families at datagen. Replaces the old
 * {@code BehaviourPlant} / {@code BehaviourSeed} / {@code BehaviourWaterPlant} / {@code BehaviourSaplingLike}
 * / {@code BehaviourLeaves} marker interfaces, which used to derive these tags via {@code instanceof}
 * scanning in {@code BCLAutoBlock/ItemTagProvider}. Add the matching factory ({@link #plant()} /
 * {@link #seed()} / {@link #waterPlant()} / {@link #sapling()} / {@link #leaves()}) to a block at
 * registration.
 * <p>
 * This trait only ever adds tags - it deliberately sets no block property, so it can be added to an existing
 * block without restating it.
 */
public class VegetationTagTrait extends BlockTraitImpl<Block, GenericBlockTrait> implements GenericBlockTrait {
    public static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "vegetation_tag");

    private static final VegetationTagTrait PLANT = new VegetationTagTrait(
            List.of(CommonBlockTags.PLANT), List.of()
    );
    private static final VegetationTagTrait SEED = new VegetationTagTrait(
            List.of(CommonBlockTags.SEEDS), List.of(CommonItemTags.SEEDS)
    );
    private static final VegetationTagTrait WATER_PLANT = new VegetationTagTrait(
            List.of(CommonBlockTags.WATER_PLANT), List.of()
    );
    private static final VegetationTagTrait SAPLING = new VegetationTagTrait(
            List.of(BlockTags.SAPLINGS, CommonBlockTags.SAPLINGS),
            List.of(ItemTags.SAPLINGS, CommonItemTags.SAPLINGS)
    );
    private static final VegetationTagTrait LEAVES = new VegetationTagTrait(
            List.of(BlockTags.LEAVES, CommonBlockTags.LEAVES),
            List.of(ItemTags.LEAVES, CommonItemTags.LEAVES)
    );

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

    /**
     * Tags the block as {@link BlockTags#SAPLINGS}/{@link CommonBlockTags#SAPLINGS} and its item as
     * {@link ItemTags#SAPLINGS}/{@link CommonItemTags#SAPLINGS} - exactly what
     * {@code BehaviourSaplingLike} contributed through the two auto tag providers.
     */
    public static VegetationTagTrait sapling() {
        return SAPLING;
    }

    /**
     * Tags the block as {@link BlockTags#LEAVES}/{@link CommonBlockTags#LEAVES} and its item as
     * {@link ItemTags#LEAVES}/{@link CommonItemTags#LEAVES} - exactly what {@code BehaviourLeaves}
     * contributed through the two auto tag providers.
     */
    public static VegetationTagTrait leaves() {
        return LEAVES;
    }

    private final List<TagKey<Block>> blockTags;
    private final List<TagKey<Item>> itemTags;

    private VegetationTagTrait(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
        this.blockTags = blockTags;
        this.itemTags = itemTags;
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
        blockTags.forEach(definition::addTags);
        itemTags.forEach(definition::addItemTags);
    }
}
