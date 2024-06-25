package org.betterx.datagen.bclib.worldgen;

import org.betterx.wover.core.api.ModCore;
import org.betterx.wover.datagen.api.WoverTagProvider;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;

import net.minecraft.world.level.block.Block;

public class BlockTagProvider extends WoverTagProvider.ForBlocks {
    public BlockTagProvider(ModCore modCore) {
        super(modCore);
    }

    @Override
    public void prepareTags(TagBootstrapContext<Block> context) {

    }
}
