package org.betterx.bclib.api.v3.datagen;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;

import java.util.List;
import java.util.function.BiConsumer;

public class BlockLootTableProvider extends SimpleFabricLootTableProvider {
    protected final List<String> modIDs;

    public BlockLootTableProvider(
            FabricDataOutput output,
            List<String> modIDs
    ) {
        super(output, LootContextParamSets.BLOCK);
        this.modIDs = modIDs;
    }


    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> biConsumer) {
        for (Block block : BuiltInRegistries.BLOCK) {
            if (block instanceof LootDropProvider dropper) {
                ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
                if (id != null && modIDs.contains(id.getNamespace())) {
                    LootTable.Builder builder = LootTable.lootTable();
                    dropper.getDroppedItemsBCL(builder);
                    biConsumer.accept(id.withPrefix("blocks/"), builder);
                }
            }
        }
    }
}
