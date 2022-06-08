package org.betterx.bclib.registry;

import org.betterx.bclib.api.v2.tag.*;
import org.betterx.bclib.blocks.BaseLeavesBlock;
import org.betterx.bclib.blocks.BaseOreBlock;
import org.betterx.bclib.blocks.FeatureSaplingBlock;
import org.betterx.bclib.config.PathConfig;
import org.betterx.bclib.interfaces.CustomItemProvider;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;

public class BlockRegistry extends BaseRegistry<Block> {
    public BlockRegistry(CreativeModeTab creativeTab, PathConfig config) {
        super(creativeTab, config);
    }

    @Override
    public Block register(ResourceLocation id, Block block) {
        if (!config.getBooleanRoot(id.getNamespace(), true)) {
            return block;
        }

        BlockItem item = null;
        if (block instanceof CustomItemProvider) {
            item = ((CustomItemProvider) block).getCustomItem(id, makeItemSettings());
        } else {
            item = new BlockItem(block, makeItemSettings());
        }
        registerBlockItem(id, item);
        if (block.defaultBlockState().getMaterial().isFlammable() && FlammableBlockRegistry.getDefaultInstance()
                                                                                           .get(block)
                                                                                           .getBurnChance() == 0) {
            FlammableBlockRegistry.getDefaultInstance().add(block, 5, 5);
        }

        block = Registry.register(Registry.BLOCK, id, block);
        getModBlocks(id.getNamespace()).add(block);

        if (block instanceof BaseLeavesBlock) {
            TagAPI.addBlockTags(
                    block,
                    NamedBlockTags.LEAVES,
                    CommonBlockTags.LEAVES,
                    NamedMineableTags.HOE,
                    NamedMineableTags.SHEARS
            );
            if (item != null) {
                TagAPI.addItemTags(item, CommonItemTags.LEAVES, NamedItemTags.LEAVES);
            }
        } else if (block instanceof BaseOreBlock) {
            TagAPI.addBlockTags(block, NamedMineableTags.PICKAXE);
        } else if (block instanceof FeatureSaplingBlock) {
            TagAPI.addBlockTags(block, CommonBlockTags.SAPLINGS, NamedBlockTags.SAPLINGS);
            if (item != null) {
                TagAPI.addItemTags(item, CommonItemTags.SAPLINGS, NamedItemTags.SAPLINGS);
            }
        }

        return block;
    }

    public Block registerBlockOnly(ResourceLocation id, Block block) {
        if (!config.getBooleanRoot(id.getNamespace(), true)) {
            return block;
        }
        getModBlocks(id.getNamespace()).add(block);
        return Registry.register(Registry.BLOCK, id, block);
    }

    private Item registerBlockItem(ResourceLocation id, Item item) {
        registerItem(id, item);
        return item;
    }

    @Override
    public void registerItem(ResourceLocation id, Item item) {
        if (item != null && item != Items.AIR) {
            Registry.register(Registry.ITEM, id, item);
            getModBlockItems(id.getNamespace()).add(item);
        }
    }
}
