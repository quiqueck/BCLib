package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import de.ambertation.wover.block.api.BlockDefinition;
import de.ambertation.wover.block.api.trait.BlockTraitKey;
import de.ambertation.wover.block.api.trait.GenericBlockTrait;
import de.ambertation.wover.block.impl.trait.BlockTraitImpl;
import de.ambertation.wover.tag.api.predefined.CommonBlockTags;
import de.ambertation.wover.tag.api.predefined.CommonItemTags;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Marker trait that tags a block into one of the common "vegetation" families at datagen. Replaces the old
 * {@code BehaviourPlant} / {@code BehaviourSeed} / {@code BehaviourWaterPlant} / {@code BehaviourSaplingLike}
 * / {@code BehaviourLeaves} / {@code BehaviourVine} marker interfaces, which used to derive these tags via
 * {@code instanceof} scanning in {@code BCLAutoBlock/ItemTagProvider}. Add the matching factory
 * ({@link #plant()} / {@link #seed()} / {@link #waterPlant()} / {@link #sapling()} / {@link #leaves()} /
 * {@link #vine()}) to a block at registration.
 * <p>
 * This trait only ever adds tags - it deliberately sets no block property, so it can be added to an existing
 * block without restating it.
 */
public class VegetationTagTrait extends BlockTraitImpl<Block, GenericBlockTrait> implements GenericBlockTrait {
    public static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "vegetation_tag");

    // sword_efficient (WP: mineable-audit §5): vanilla gives its own plants/leaves/vines the sword mining
    // bonus (minecraft:sword_efficient). Neither BetterEnd nor BetterNether populated it at all. Adding it
    // here - the single shared tag lever every plant/water-plant/seed/vine/leaves registration already goes
    // through - covers both mods' whole plant family in one place. Saplings are deliberately excluded:
    // vanilla's own tree saplings (oak_sapling, etc.) are NOT in sword_efficient (only bamboo_sapling is,
    // handled elsewhere), so SAPLING keeps its original tag list.
    private static final VegetationTagTrait PLANT = new VegetationTagTrait(
            List.of(CommonBlockTags.PLANT, BlockTags.SWORD_EFFICIENT), List.of()
    );
    private static final VegetationTagTrait SEED = new VegetationTagTrait(
            List.of(CommonBlockTags.SEEDS, BlockTags.SWORD_EFFICIENT), List.of(CommonItemTags.SEEDS)
    );
    private static final VegetationTagTrait WATER_PLANT = new VegetationTagTrait(
            List.of(CommonBlockTags.WATER_PLANT, BlockTags.SWORD_EFFICIENT), List.of()
    );
    private static final VegetationTagTrait SAPLING = new VegetationTagTrait(
            List.of(BlockTags.SAPLINGS, CommonBlockTags.SAPLINGS),
            List.of(ItemTags.SAPLINGS, CommonItemTags.SAPLINGS)
    );
    private static final VegetationTagTrait LEAVES = new VegetationTagTrait(
            List.of(BlockTags.LEAVES, CommonBlockTags.LEAVES, BlockTags.SWORD_EFFICIENT),
            List.of(ItemTags.LEAVES, CommonItemTags.LEAVES)
    );
    private static final VegetationTagTrait VINE = new VegetationTagTrait(
            List.of(CommonBlockTags.VINE, BlockTags.SWORD_EFFICIENT), List.of()
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

    /**
     * Tags the block as {@link CommonBlockTags#VINE} - exactly what the retired {@code BehaviourVine}
     * marker contributed via the {@code instanceof BehaviourVine} scan in {@code BCLAutoBlockTagProvider}.
     */
    public static VegetationTagTrait vine() {
        return VINE;
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
