package org.betterx.bclib.api.v3.datagen;

import net.minecraft.world.level.storage.loot.LootTable;

public interface LootDropProvider {
    void getDroppedItemsBCL(LootTable.Builder builder);
}
