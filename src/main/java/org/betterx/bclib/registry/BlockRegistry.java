package org.betterx.bclib.registry;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.blocks.BaseLeavesBlock;
import org.betterx.bclib.blocks.BaseOreBlock;
import org.betterx.bclib.blocks.FeatureSaplingBlock;
import org.betterx.bclib.config.PathConfig;
import org.betterx.bclib.interfaces.CustomItemProvider;
import org.betterx.worlds.together.tag.v3.CommonBlockTags;
import org.betterx.worlds.together.tag.v3.CommonItemTags;
import org.betterx.worlds.together.tag.v3.MineableTags;
import org.betterx.worlds.together.tag.v3.TagManager;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
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

        block = Registry.register(BuiltInRegistries.BLOCK, id, block);
        getModBlocks(id.getNamespace()).add(block);

        if (block instanceof BaseLeavesBlock) {
            TagManager.BLOCKS.add(
                    block,
                    BlockTags.LEAVES,
                    CommonBlockTags.LEAVES,
                    MineableTags.HOE,
                    MineableTags.SHEARS
            );
            if (item != null) {
                TagManager.ITEMS.add(item, CommonItemTags.LEAVES, ItemTags.LEAVES);
            }
        } else if (block instanceof BaseOreBlock) {
            TagManager.BLOCKS.add(block, MineableTags.PICKAXE);
        } else if (block instanceof FeatureSaplingBlock) {
            TagManager.BLOCKS.add(block, CommonBlockTags.SAPLINGS, BlockTags.SAPLINGS);
            if (item != null) {
                TagManager.ITEMS.add(item, CommonItemTags.SAPLINGS, ItemTags.SAPLINGS);
            }
        }

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
