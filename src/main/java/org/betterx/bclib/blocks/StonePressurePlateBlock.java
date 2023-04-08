package org.betterx.bclib.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockSetType;

public class StonePressurePlateBlock extends BasePressurePlateBlock {
    public StonePressurePlateBlock(Block source, BlockSetType type) {
        super(Sensitivity.MOBS, source, type);
    }
}
