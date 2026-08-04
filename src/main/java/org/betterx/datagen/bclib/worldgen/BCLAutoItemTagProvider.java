package org.betterx.datagen.bclib.worldgen;

import org.betterx.bclib.BCLib;
import de.ambertation.wover.block.api.BlockRegistry;
import de.ambertation.wover.core.api.ModCore;
import de.ambertation.wover.datagen.api.WoverAutoProvider;
import de.ambertation.wover.datagen.api.WoverTagProvider;
import de.ambertation.wover.item.api.ItemRegistry;
import de.ambertation.wover.tag.api.event.context.ItemTagBootstrapContext;
import de.ambertation.wover.tag.api.event.context.TagBootstrapContext;

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
