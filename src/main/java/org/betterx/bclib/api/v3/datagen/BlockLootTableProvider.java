package org.betterx.bclib.api.v3.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableSubProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class BlockLootTableProvider extends SimpleFabricLootTableSubProvider {
    protected final List<String> modIDs;

    public BlockLootTableProvider(
            FabricPackOutput output,
            CompletableFuture<HolderLookup.Provider> registryLookup,
            List<String> modIDs
    ) {
        super(output, registryLookup, LootContextParamSets.BLOCK);
        this.modIDs = modIDs;
    }


    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
        for (Block block : BuiltInRegistries.BLOCK) {
            if (block instanceof LootDropProvider dropper) {
                Identifier id = BuiltInRegistries.BLOCK.getKey(block);
                if (id != null && modIDs.contains(id.getNamespace())) {
                    LootTable.Builder builder = LootTable.lootTable();
                    dropper.getDroppedItemsBCL(builder);
                    biConsumer.accept(ResourceKey.create(Registries.LOOT_TABLE, id.withPrefix("block/")), builder);
                }
            }
        }
    }
}
