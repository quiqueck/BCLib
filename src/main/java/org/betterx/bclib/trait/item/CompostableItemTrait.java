package org.betterx.bclib.trait.item;

import org.betterx.bclib.BCLib;
import de.ambertation.wover.block.api.trait.CompostTier;
import de.ambertation.wover.block.api.trait.CompostableTrait;
import de.ambertation.wover.item.api.ItemDefinition;
import de.ambertation.wover.item.api.trait.GenericItemTrait;
import de.ambertation.wover.item.api.trait.ItemTrait;
import de.ambertation.wover.item.api.trait.ItemTraitKey;
import de.ambertation.wover.item.impl.trait.ItemTraitImpl;
import de.ambertation.wover.tag.api.predefined.CommonItemTags;

import net.minecraft.world.item.Item;

import java.util.List;

/**
 * Marks an item as compostable. The composting chance is stored as a runtime trait on the item (via
 * {@link #forRuntime()}) and resolved lazily at composter-use time by {@code ComposterBlockMixin}, which reads
 * it straight off the item through {@link #chanceFor(Item)}.
 * <p>
 * This mirrors {@link org.betterx.bclib.trait.block.CompostableBlockTrait}, but for items whose compostability
 * is intrinsic to the item itself rather than derived from a backing block (e.g. food items). It deliberately
 * does <em>not</em> register the item into vanilla's static {@link net.minecraft.world.level.block.ComposterBlock#COMPOSTABLES}
 * map - that map is populated in a class initializer for vanilla items only, so mirroring into it at world load
 * would be invisible to datagen (no world is ever loaded there). Reading the trait at use time keeps the
 * runtime behaviour identical for players while making compostability observable to datagen oracles (which
 * read the same {@link CompostableTrait} interface uniformly across block and item traits).
 */
public class CompostableItemTrait extends ItemTraitImpl.Generic implements CompostableTrait {
    public static final ItemTraitKey KEY = ItemTraitKey.ofUnique(BCLib.C, "compostable");
    private static final CompostableItemTrait DEFAULT = new CompostableItemTrait(CompostTier.VERY_LOW);

    public static CompostableItemTrait withDefault() {
        return DEFAULT;
    }

    /**
     * @param chance the requested composting chance; snapped to the nearest {@link CompostTier}
     */
    public static CompostableItemTrait withChance(float chance) {
        return new CompostableItemTrait(CompostTier.nearest(chance));
    }

    public static CompostableItemTrait withTier(CompostTier tier) {
        return new CompostableItemTrait(tier);
    }

    public final CompostTier tier;
    public final float compostingChance;

    private CompostableItemTrait(CompostTier tier) {
        this.tier = tier;
        this.compostingChance = tier.chance;
    }

    @Override
    public ItemTraitKey key() {
        return KEY;
    }

    @Override
    public GenericItemTrait forRuntime() {
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
    public void configure(ItemDefinition<Item, ? extends ItemDefinition<Item, ?>> definition) {
        super.configure(definition);
        // Tags the item as compostable at datagen. Mirrors CompostableBlockTrait.configure(); the composting
        // chance itself is resolved at runtime from the trait via chanceFor below.
        definition.addTags(CommonItemTags.COMPOSTABLE);
    }

    /**
     * Resolves the composting chance carried directly by {@code item}'s runtime trait, or a negative value
     * when the item carries no compostable trait. Consumed by {@code ComposterBlockMixin} (and, eventually, a
     * item-compostable datagen oracle) as the trait-based fallback for both vanilla's
     * {@code ComposterBlock.COMPOSTABLES} lookup and {@link org.betterx.bclib.trait.block.CompostableBlockTrait}.
     *
     * @param item the item a composter is being filled with
     * @return the trait's composting chance in {@code (0,1]}, or {@code -1} if the item has no compostable trait
     */
    public static float chanceFor(Item item) {
        final List<GenericItemTrait> traits = ItemTrait.getRuntimeTraits(item, KEY);
        if (traits != null && !traits.isEmpty() && traits.get(0) instanceof CompostableItemTrait trait) {
            return trait.compostingChance;
        }
        return -1.0f;
    }

    /**
     * @param item the item a composter is being filled with
     * @return {@code true} if {@code item} carries a compostable trait
     */
    public static boolean isCompostable(Item item) {
        return chanceFor(item) >= 0.0f;
    }
}
