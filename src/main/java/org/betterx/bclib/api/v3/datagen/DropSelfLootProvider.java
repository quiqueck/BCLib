package org.betterx.bclib.api.v3.datagen;

import org.betterx.bclib.behaviours.interfaces.BehaviourExplosionResistant;
import org.betterx.wover.loot.api.BlockLootProvider;
import org.betterx.wover.loot.api.LootLookupProvider;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import org.jetbrains.annotations.NotNull;

public interface DropSelfLootProvider<B extends ItemLike> extends BlockLootProvider {
    @Override
    default LootTable.Builder registerBlockLoot(
            @NotNull ResourceLocation location,
            @NotNull LootLookupProvider provider,
            @NotNull ResourceKey<LootTable> tableKey
    ) {
        LootTable.Builder builder = LootTable.lootTable();
        var pool = LootPool.lootPool()
                           .setRolls(ConstantValue.exactly(1.0f))
                           .add(LootItem.lootTableItem((B) this));

        if (this instanceof BehaviourExplosionResistant) {
            pool = pool.when(ExplosionCondition.survivesExplosion());
        }

        return builder.withPool(pool);
    }
}
