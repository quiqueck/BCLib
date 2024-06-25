package org.betterx.datagen.bclib.worldgen;

import org.betterx.bclib.api.v3.tag.BCLBlockTags;
import org.betterx.wover.core.api.ModCore;
import org.betterx.wover.datagen.api.WoverTagProvider;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class BoneMealBlockTagProvider extends WoverTagProvider.ForBlocks {
    public BoneMealBlockTagProvider(ModCore modCore) {
        super(modCore);
    }


    @Override
    public void prepareTags(TagBootstrapContext<Block> context) {
        context.add(BCLBlockTags.BONEMEAL_SOURCE_NETHERRACK, Blocks.WARPED_NYLIUM, Blocks.CRIMSON_NYLIUM);
        context.add(BCLBlockTags.BONEMEAL_TARGET_NETHERRACK, Blocks.NETHERRACK);
        context.add(BCLBlockTags.BONEMEAL_TARGET_END_STONE, Blocks.END_STONE);
        context.add(BCLBlockTags.BONEMEAL_TARGET_OBSIDIAN, Blocks.OBSIDIAN);
    }
}
