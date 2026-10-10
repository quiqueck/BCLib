package org.betterx.bclib.api.v2;

import org.betterx.bclib.trait.block.CompostableBlockTrait;
import org.betterx.bclib.trait.item.CompostableItemTrait;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Compostability lookups.
 * <p>
 * Up to 26.1 this put items into vanilla's static {@code ComposterBlock.COMPOSTABLES} map at any time, which
 * is why {@link #allowCompost} existed. 26.3 deleted that map: an item is compostable iff it carries the
 * {@code minecraft:compostable} component, and default components are baked onto {@code Item.Properties} when
 * the item is built. There is no longer any way to make an already-registered item compostable, so
 * compostability has to be declared at registration - through {@link CompostableBlockTrait} for a block or
 * {@link CompostableItemTrait} for a plain item, both of which set the component for you.
 * <p>
 * What remains here is the read side: {@link #compostChance(Item)} reports what BCLib declared, which is what
 * the datagen oracles and tests use. The game itself never calls it - it reads the component.
 *
 * @see CompostableBlockTrait
 * @see CompostableItemTrait
 */
public class ComposterAPI {
    private ComposterAPI() {
    }

    /**
     * @deprecated Impossible on 26.3 and later: compostability is a default item component, which can only be
     * set while the item is being built. Attach a {@link CompostableBlockTrait} to the block instead.
     */
    @Deprecated(forRemoval = true)
    public static Block allowCompost(float chance, Block block) {
        throw new UnsupportedOperationException(
                "Compostability can no longer be added after registration - 26.3 replaced"
                        + " ComposterBlock.COMPOSTABLES with the minecraft:compostable item component."
                        + " Add a CompostableBlockTrait to the block instead."
        );
    }

    /**
     * @deprecated Impossible on 26.3 and later: compostability is a default item component, which can only be
     * set while the item is being built. Attach a {@link CompostableItemTrait} to the item instead.
     */
    @Deprecated(forRemoval = true)
    public static Item allowCompost(float chance, Item item) {
        throw new UnsupportedOperationException(
                "Compostability can no longer be added after registration - 26.3 replaced"
                        + " ComposterBlock.COMPOSTABLES with the minecraft:compostable item component."
                        + " Add a CompostableItemTrait to the item instead."
        );
    }

    /**
     * The composting chance BCLib declared for {@code item}: the block's {@link CompostableBlockTrait} first,
     * then the item's own {@link CompostableItemTrait}.
     * <p>
     * This is the <em>requested</em> chance after snapping, i.e. the chance of the tier the item's component
     * points at. It is a report, not the mechanism: the composter reads the component.
     *
     * @param item the item to inspect
     * @return the chance in {@code (0,1]}, or {@code -1} when BCLib declared nothing for the item
     */
    public static float compostChance(Item item) {
        if (item == null) return -1.0f;

        final float blockChance = CompostableBlockTrait.chanceFor(item);
        if (blockChance >= 0.0f) return blockChance;

        return CompostableItemTrait.chanceFor(item);
    }
}
