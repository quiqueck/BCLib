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
 * Marks a block's item as compostable.
 * <p>
 * The rate is declared as a chance and snapped to the nearest {@link CompostTier}, because 26.3 removed
 * vanilla's static {@code ComposterBlock.COMPOSTABLES} map (and the arbitrary {@code float} it stored) in
 * favour of the {@code minecraft:compostable} item component, whose rate is a reference into the
 * {@code number_provider} registry. {@code BlockDefinition} reads the trait back at block-item registration
 * and sets that component, which is what makes composting work on every vanilla path - the player's
 * right-click, a dispenser, and the container a hopper inserts through - without a single mixin.
 * <p>
 * The trait is still attached at runtime ({@link #forRuntime()}) because it remains the authoring record of
 * the requested chance, which the {@code block_registrations.txt} datagen oracle reports.
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
        // gone, the trait itself is now responsible for the tag. The composting rate itself travels
        // separately, as the minecraft:compostable component BlockDefinition sets on the block item.
        definition.addItemTags(CommonItemTags.COMPOSTABLE);
    }

    /**
     * Resolves the composting chance carried by the block that backs {@code item}, or a negative value when
     * the item is not a {@link BlockItem} whose block carries this trait. Read by the
     * {@code block_registrations.txt} datagen oracle; the game itself goes through the item's
     * {@code minecraft:compostable} component and never consults this.
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
