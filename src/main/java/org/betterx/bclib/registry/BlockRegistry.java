package org.betterx.bclib.registry;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.config.PathConfig;
import org.betterx.bclib.interfaces.CustomItemProvider;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;

public class BlockRegistry extends BaseRegistry<Block> {
    public BlockRegistry(PathConfig config) {
        super(config);
    }

    @Override
    public Block register(ResourceLocation id, Block block) {
        if (!config.getBooleanRoot(id.getNamespace(), true)) {
            BCLib.LOGGER.warning("Block " + id + " disabled");
            return block;
        }
        block = Registry.register(BuiltInRegistries.BLOCK, id, block);

        BlockItem item = null;
        if (block instanceof CustomItemProvider) {
            item = ((CustomItemProvider) block).getCustomItem(id, makeItemSettings());
        } else {
            item = new BlockItem(block, makeItemSettings());

        }
        registerBlockItem(id, item);
        if (block.defaultBlockState().ignitedByLava()
                && FlammableBlockRegistry.getDefaultInstance()
                                         .get(block)
                                         .getBurnChance() == 0) {
            FlammableBlockRegistry.getDefaultInstance().add(block, 5, 5);
        }
        
        getModBlocks(id.getNamespace()).add(block);

        return block;
    }

    public Block registerBlockOnly(ResourceLocation id, Block block) {
        if (!config.getBooleanRoot(id.getNamespace(), true)) {
            return block;
        }
        getModBlocks(id.getNamespace()).add(block);
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    private Item registerBlockItem(ResourceLocation id, BlockItem item) {
        registerItem(id, item);
        return item;
    }

    @Override
    public void registerItem(ResourceLocation id, Item item) {
        if (item != null && item != Items.AIR) {
            Registry.register(BuiltInRegistries.ITEM, id, item);
            getModBlockItems(id.getNamespace()).add(item);
        }
    }
}
