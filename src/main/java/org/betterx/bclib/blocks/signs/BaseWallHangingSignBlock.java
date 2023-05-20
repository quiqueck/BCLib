package org.betterx.bclib.blocks.signs;

import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.properties.WoodType;

public class BaseWallHangingSignBlock extends WallHangingSignBlock {
    public BaseWallHangingSignBlock(
            Properties properties,
            WoodType woodType
    ) {
        super(properties, woodType);
    }
}
