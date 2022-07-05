package org.betterx.bclib.interfaces;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;

import java.util.List;

public interface LootPoolAccessor {
    LootPool bcl_mergeEntries(List<LootPoolEntryContainer> newEntries);
}
