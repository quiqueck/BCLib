package org.betterx.bclib.blocks;

import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class BlockProperties {
    public static final EnumProperty<de.ambertation.wover.block.api.BlockProperties.TripleShape> TRIPLE_SHAPE
            = de.ambertation.wover.block.api.BlockProperties.TRIPLE_SHAPE;
    public static final EnumProperty<de.ambertation.wover.block.api.BlockProperties.PentaShape> PENTA_SHAPE = de.ambertation.wover.block.api.BlockProperties.PENTA_SHAPE;

    public static final BooleanProperty TRANSITION = de.ambertation.wover.block.api.BlockProperties.TRANSITION;
    public static final BooleanProperty HAS_LIGHT = de.ambertation.wover.block.api.BlockProperties.HAS_LIGHT;
    public static final BooleanProperty IS_FLOOR = de.ambertation.wover.block.api.BlockProperties.IS_FLOOR;
    public static final BooleanProperty NATURAL = de.ambertation.wover.block.api.BlockProperties.NATURAL;
    public static final BooleanProperty ACTIVE = de.ambertation.wover.block.api.BlockProperties.ACTIVE;
    public static final BooleanProperty SMALL = de.ambertation.wover.block.api.BlockProperties.SMALL;

    public static final IntegerProperty DEFAULT_ANVIL_DURABILITY = de.ambertation.wover.block.api.BlockProperties.DEFAULT_ANVIL_DURABILITY;
    public static final IntegerProperty ROTATION = de.ambertation.wover.block.api.BlockProperties.ROTATION;
    public static final IntegerProperty FULLNESS = de.ambertation.wover.block.api.BlockProperties.FULLNESS;
    public static final IntegerProperty COLOR = de.ambertation.wover.block.api.BlockProperties.COLOR;
    public static final IntegerProperty SIZE = de.ambertation.wover.block.api.BlockProperties.SIZE;
    public static final IntegerProperty AGE = de.ambertation.wover.block.api.BlockProperties.AGE;
    public static final IntegerProperty AGE_THREE = de.ambertation.wover.block.api.BlockProperties.AGE_THREE;
    public static final BooleanProperty BOTTOM = de.ambertation.wover.block.api.BlockProperties.BOTTOM;
    public static final BooleanProperty TOP = de.ambertation.wover.block.api.BlockProperties.TOP;
}
