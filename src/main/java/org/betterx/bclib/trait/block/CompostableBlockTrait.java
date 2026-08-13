package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import de.ambertation.wover.block.api.BlockDefinition;
import de.ambertation.wover.block.api.trait.BlockTrait;
import de.ambertation.wover.block.api.trait.BlockTraitKey;
import de.ambertation.wover.block.api.trait.CompostTier;
import de.ambertation.wover.block.api.trait.CompostableTrait;
import de.ambertation.wover.block.api.trait.GenericBlockTrait;
import de.ambertation.wover.block.impl.trait.BlockTraitImpl;
import de.ambertation.wover.tag.api.predefined.CommonItemTags;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Marks a block's item as compostable. The composting chance is stored as a runtime trait on the block (via
 * {@link #forRuntime()}) and resolved lazily at composter-use time by {@code ComposterBlockMixin}, which reads
 * it straight off the block through {@link #chanceFor(Item)}.
 * <p>
 * This deliberately does <em>not</em> register the block into vanilla's static
 * {@link net.minecraft.world.level.block.ComposterBlock#COMPOSTABLES} map. That map is populated in a class
 * initializer for vanilla items and was previously mirrored into at world load, but that init-time
 * registration is invisible to datagen (no world is ever loaded there), so the
 * {@code block_registrations.txt} oracle showed every modded block as non-compostable. Reading the trait at
 * use time keeps the runtime behaviour identical for players while making compostability observable to the
 * oracle (which now reads the same trait).
 */
public class CompostableBlockTrait extends BlockTraitImpl<Block, GenericBlockTrait>
        implements GenericBlockTrait, CompostableTrait {
    public static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "compostable");
    private static final CompostableBlockTrait DEFAULT = new CompostableBlockTrait(CompostTier.VERY_LOW);

    public static CompostableBlockTrait withDefault() {
        return DEFAULT;
    }

    /**
     * @param chance the requested composting chance; snapped to the nearest {@link CompostTier}
     */
    public static CompostableBlockTrait withChance(float chance) {
        return new CompostableBlockTrait(CompostTier.nearest(chance));
    }

    public static CompostableBlockTrait withTier(CompostTier tier) {
        return new CompostableBlockTrait(tier);
    }

    public final CompostTier tier;
    public final float compostingChance;

    private CompostableBlockTrait(CompostTier tier) {
        this.tier = tier;
        this.compostingChance = tier.chance;
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
    public float compostChance() {
        return compostingChance;
    }

    @Override
    public CompostTier compostTier() {
        return tier;
    }

    @Override
    public void configure(BlockDefinition<Block, ? extends BlockDefinition<Block, ?>> definition) {
        super.configure(definition);
        // Tags the block's item as compostable at datagen. This used to be added by
        // BCLAutoItemTagProvider from the BehaviourCompostable marker interface; with that interface
        // gone, the trait itself is now responsible for the tag (the composter chance is resolved at
        // runtime from the trait via chanceFor below).
        definition.addItemTags(CommonItemTags.COMPOSTABLE);
    }

    /**
     * Resolves the composting chance carried by the block that backs {@code item}, or a negative value when
     * the item is not a {@link BlockItem} whose block carries this trait. Consumed by {@code ComposterBlockMixin}
     * (and by the {@code block_registrations.txt} datagen oracle) as the trait-based fallback for vanilla's
     * {@code ComposterBlock.COMPOSTABLES} lookup.
     *
     * @param item the item a composter is being filled with
     * @return the trait's composting chance in {@code (0,1]}, or {@code -1} if the item has no compostable trait
     */
    public static float chanceFor(Item item) {
        if (item instanceof BlockItem blockItem) {
            final Block block = blockItem.getBlock();
            final List<GenericBlockTrait> traits = BlockTrait.getRuntimeTraits(block, KEY);
            if (traits != null && !traits.isEmpty() && traits.get(0) instanceof CompostableBlockTrait trait) {
                return trait.compostingChance;
            }
        }
        return -1.0f;
    }

    /**
     * @param item the item a composter is being filled with
     * @return {@code true} if {@code item}'s block carries a compostable trait
     */
    public static boolean isCompostable(Item item) {
        return chanceFor(item) >= 0.0f;
    }
}
