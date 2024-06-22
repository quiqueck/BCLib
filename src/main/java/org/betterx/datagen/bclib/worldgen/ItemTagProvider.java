package org.betterx.datagen.bclib.worldgen;

import org.betterx.bclib.behaviours.interfaces.BehaviourCompostable;
import org.betterx.bclib.behaviours.interfaces.BehaviourLeaves;
import org.betterx.bclib.behaviours.interfaces.BehaviourSaplingLike;
import org.betterx.bclib.behaviours.interfaces.BehaviourSeedLike;
import org.betterx.bclib.items.tool.*;
import org.betterx.wover.block.api.BlockRegistry;
import org.betterx.wover.core.api.ModCore;
import org.betterx.wover.datagen.api.WoverTagProvider;
import org.betterx.wover.item.api.ItemRegistry;
import org.betterx.wover.tag.api.event.context.ItemTagBootstrapContext;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;
import org.betterx.wover.tag.api.predefined.CommonItemTags;
import org.betterx.wover.tag.api.predefined.ToolTags;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class ItemTagProvider extends WoverTagProvider.ForItems {
    public ItemTagProvider(ModCore modCore) {
        super(modCore);
    }

    private static void processItemCommon(TagBootstrapContext<Item> context, Item item) {
        if (item instanceof BaseShovelItem) {
            context.add(item, ToolTags.FABRIC_SHOVELS, ItemTags.SHOVELS);
        } else if (item instanceof BaseSwordItem) {
            context.add(item, ToolTags.FABRIC_SWORDS, ItemTags.SWORDS);
        } else if (item instanceof BasePickaxeItem) {
            context.add(item, ToolTags.FABRIC_PICKAXES, ItemTags.PICKAXES);
        } else if (item instanceof BaseAxeItem) {
            context.add(item, ToolTags.FABRIC_AXES, ItemTags.AXES);
        } else if (item instanceof BaseHoeItem) {
            context.add(item, ToolTags.FABRIC_HOES, ItemTags.HOES);
        } else if (item instanceof BaseShearsItem) {
            context.add(item, ToolTags.FABRIC_SHEARS, CommonItemTags.SHEARS);
        }
    }

    private static void processBlockItemCommon(TagBootstrapContext<Item> context, Block block) {
        Item item = block.asItem();
        if (item == null || item == Items.AIR) return;

        if (block instanceof BehaviourCompostable c) {
            context.add(item, CommonItemTags.COMPOSTABLE);
        }

        if (block instanceof BehaviourSeedLike) {
            context.add(item, CommonItemTags.SEEDS);
        }

        if (block instanceof BehaviourSaplingLike) {
            context.add(item, CommonItemTags.SAPLINGS, ItemTags.SAPLINGS);
        }

        if (block instanceof BehaviourLeaves) {
            context.add(item, ItemTags.LEAVES, CommonItemTags.LEAVES);
        }
    }

    public static void processBlockItemCommon(TagBootstrapContext<Item> context, ModCore modCore) {
        ItemRegistry.forMod(modCore)
                    .allItems()
                    .forEach(item -> processItemCommon(context, item));


        BlockRegistry.forMod(modCore)
                     .allBlocks()
                     .forEach(block -> processBlockItemCommon(context, block));
    }


    @Override
    protected void prepareTags(ItemTagBootstrapContext context) {
        processBlockItemCommon(context, modCore);
    }
}
