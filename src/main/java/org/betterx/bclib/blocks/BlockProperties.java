package org.betterx.bclib.blocks;

import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class BlockProperties {
    public static final EnumProperty<org.betterx.wover.block.api.BlockProperties.TripleShape> TRIPLE_SHAPE
            = org.betterx.wover.block.api.BlockProperties.TRIPLE_SHAPE;
    public static final EnumProperty<org.betterx.wover.block.api.BlockProperties.PentaShape> PENTA_SHAPE = org.betterx.wover.block.api.BlockProperties.PENTA_SHAPE;

    public static final BooleanProperty TRANSITION = org.betterx.wover.block.api.BlockProperties.TRANSITION;
    public static final BooleanProperty HAS_LIGHT = org.betterx.wover.block.api.BlockProperties.HAS_LIGHT;
    public static final BooleanProperty IS_FLOOR = org.betterx.wover.block.api.BlockProperties.IS_FLOOR;
    public static final BooleanProperty NATURAL = org.betterx.wover.block.api.BlockProperties.NATURAL;
    public static final BooleanProperty ACTIVE = org.betterx.wover.block.api.BlockProperties.ACTIVE;
    public static final BooleanProperty SMALL = org.betterx.wover.block.api.BlockProperties.SMALL;

    public static final IntegerProperty DEFAULT_ANVIL_DURABILITY = org.betterx.wover.block.api.BlockProperties.DEFAULT_ANVIL_DURABILITY;
    public static final IntegerProperty DESTRUCTION = org.betterx.wover.block.api.BlockProperties.DESTRUCTION;
    public static final IntegerProperty ROTATION = org.betterx.wover.block.api.BlockProperties.ROTATION;
    public static final IntegerProperty FULLNESS = org.betterx.wover.block.api.BlockProperties.FULLNESS;
    public static final IntegerProperty COLOR = org.betterx.wover.block.api.BlockProperties.COLOR;
    public static final IntegerProperty SIZE = org.betterx.wover.block.api.BlockProperties.SIZE;
    public static final IntegerProperty AGE = org.betterx.wover.block.api.BlockProperties.AGE;
    public static final IntegerProperty AGE_THREE = org.betterx.wover.block.api.BlockProperties.AGE_THREE;
    public static final BooleanProperty BOTTOM = org.betterx.wover.block.api.BlockProperties.BOTTOM;
    public static final BooleanProperty TOP = org.betterx.wover.block.api.BlockProperties.TOP;
}
