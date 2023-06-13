package org.betterx.bclib.interfaces;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public interface SurvivesOnWater extends SurvivesOnBlocks {
    List<Block> BLOCKS = List.of(Blocks.WATER);

    @Override
    default List<Block> getSurvivableBlocks() {
        return BLOCKS;
    }
}
