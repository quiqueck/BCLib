package org.betterx.bclib.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockSetType;

public class WoodenPressurePlateBlock extends BasePressurePlateBlock {
    public WoodenPressurePlateBlock(Block source, BlockSetType type) {
        super(Sensitivity.EVERYTHING, source, type);
    }
}
