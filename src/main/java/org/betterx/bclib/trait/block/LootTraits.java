package org.betterx.bclib.trait.block;

import de.ambertation.wover.block.api.trait.BlockTraits;
import de.ambertation.wover.block.api.trait.behaviour.LootTableTrait;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import org.jetbrains.annotations.Nullable;

/**
 * Generic {@link BlockTraits#LOOT_TABLE} factories that wover's own {@code LOOT_TABLE} builder does not offer.
 * <p>
 * These are thin factories that build a {@link LootTable} directly (there is nothing to delegate to in wover) and
 * are hoisted here from BetterNether's {@code NetherLoot} so both mods can share them. Mod-specific loot tables
 * (anything referencing a particular mod's blocks/items) stay in that mod's own loot class.
 * <p>
 * Like every {@code LOOT_TABLE} trait, each factory returns {@code null} outside of a datagen environment, so the
 * result must be handed to a definition through {@code addTrait} (which drops nulls), not collected into a
 * {@code List.of(...)}.
 */
public class LootTraits {
    /**
     * A plain self-drop with <b>no</b> {@code survives_explosion} condition.
     * <p>
     * Reproduces byte-for-byte the table bclib's old {@code DropSelfLootProvider} generated for a block that is
     * <b>not</b> explosion-resistant (the common case). Deliberately <b>not</b>
     * {@link BlockTraits#LOOT_TABLE}'s {@code dropSelf()} shortcut: that one routes through vanilla's
     * {@code createSingleItemTable}, which adds the {@code survives_explosion} condition and so would change the
     * committed table. Used to move blocks off the deprecated {@code DropSelfLootProvider} interface without
     * regenerating their loot.
     *
     * @return the loot trait, or {@code null} outside of a datagen environment
     */
    public static @Nullable LootTableTrait dropSelfNoExplosion() {
        return BlockTraits.LOOT_TABLE.with((tableKey, blockKey, block, provider) -> LootTable
                .lootTable()
                .withPool(LootPool
                        .lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(block))));
    }
}
