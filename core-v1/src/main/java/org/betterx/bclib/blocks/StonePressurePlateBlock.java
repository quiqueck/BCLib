package org.betterx.bclib.blocks;

import net.minecraft.world.level.block.Block;

public class StonePressurePlateBlock extends BasePressurePlateBlock {
    public StonePressurePlateBlock(Block source) {
        super(Sensitivity.MOBS, source);
    }
}
