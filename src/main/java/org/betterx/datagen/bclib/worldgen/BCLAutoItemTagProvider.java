package org.betterx.datagen.bclib.worldgen;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.behaviours.interfaces.BehaviourCompostable;
import org.betterx.wover.block.api.BlockRegistry;
import org.betterx.wover.core.api.ModCore;
import org.betterx.wover.datagen.api.WoverAutoProvider;
import org.betterx.wover.datagen.api.WoverTagProvider;
import org.betterx.wover.item.api.ItemRegistry;
import org.betterx.wover.tag.api.event.context.ItemTagBootstrapContext;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;
import org.betterx.wover.tag.api.predefined.CommonItemTags;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class BCLAutoItemTagProvider extends WoverTagProvider.ForItems implements WoverAutoProvider {
    public BCLAutoItemTagProvider(ModCore modCore) {
        super(modCore);
    }

    private static void processItemCommon(TagBootstrapContext<Item> context, Item item) {

    }

    private static void processBlockItemCommon(TagBootstrapContext<Item> context, Block block) {
        Item item = block.asItem();
        if (item == null || item == Items.AIR) return;

        if (block instanceof BehaviourCompostable c) {
            context.add(item, CommonItemTags.COMPOSTABLE);
        }
    }

    private static void processBlockItemCommon(TagBootstrapContext<Item> context, ModCore modCore) {
        BCLib.C.LOG.debug("Processing Items for " + modCore.namespace);
        ItemRegistry.forMod(modCore)
                    .allItems()
                    .forEach(item -> processItemCommon(context, item));


        BlockRegistry.forMod(modCore)
                     .allBlocks()
                     .forEach(block -> processBlockItemCommon(context, block));
    }


    @Override
    public void prepareTags(ItemTagBootstrapContext context) {
        processBlockItemCommon(context, modCore);
    }
}
