package org.betterx.bclib.blocks;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class BCLBlockProperties {
    public static final EnumProperty<BlockProperties.TripleShape> TRIPLE_SHAPE = BlockProperties.TRIPLE_SHAPE;
    public static final IntegerProperty SIZE = IntegerProperty.create("size", 0, 7);
    public static final IntegerProperty AGE_THREE = BlockStateProperties.AGE_2;
    public static final IntegerProperty AGE_FOUR = BlockStateProperties.AGE_3;
}
