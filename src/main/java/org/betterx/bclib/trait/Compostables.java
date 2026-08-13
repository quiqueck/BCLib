package org.betterx.bclib.trait;

import org.betterx.bclib.trait.block.CompostableBlockTrait;
import org.betterx.bclib.trait.item.CompostableItemTrait;

import net.minecraft.world.item.Item;

/**
 * Resolves compostability from the runtime traits, in the order the composter mixins need it: a
 * {@link CompostableBlockTrait} on the item's backing block first, then a {@link CompostableItemTrait} on the
 * item itself.
 * <p>
 * Modded blocks/items are deliberately never added to vanilla's static
 * {@link net.minecraft.world.level.block.ComposterBlock#COMPOSTABLES} map (see the trait classes for why), so
 * every composter code path that consults that map needs the same trait fallback. Mixin classes cannot share
 * private helpers with one another, so the fallback lives here instead of being duplicated per mixin.
 */
public class Compostables {
    /**
     * @param item the item a composter is being filled with
     * @return the composting chance in {@code (0,1]} carried by the item's block trait or its own item trait,
     *         or {@code -1} when neither carries a compostable trait
     */
    public static float chanceFor(Item item) {
        final float blockChance = CompostableBlockTrait.chanceFor(item);
        if (blockChance >= 0.0f) return blockChance;
        return CompostableItemTrait.chanceFor(item);
    }

    /**
     * @param item the item a composter is being filled with
     * @return {@code true} if {@code item} (or its block) carries a compostable trait
     */
    public static boolean isCompostable(Item item) {
        return chanceFor(item) >= 0.0f;
    }
}
