package org.betterx.bclib.blocks;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class BlockProperties {
    public static final EnumProperty<TripleShape> TRIPLE_SHAPE = EnumProperty.create("shape", TripleShape.class);
    public static final EnumProperty<PentaShape> PENTA_SHAPE = EnumProperty.create("shape", PentaShape.class);

    public static final BooleanProperty TRANSITION = BooleanProperty.create("transition");
    public static final BooleanProperty HAS_LIGHT = BooleanProperty.create("has_light");
    public static final BooleanProperty IS_FLOOR = BooleanProperty.create("is_floor");
    public static final BooleanProperty NATURAL = BooleanProperty.create("natural");
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty SMALL = BooleanProperty.create("small");

    public static final IntegerProperty DEFAULT_ANVIL_DURABILITY = IntegerProperty.create("durability", 0, 3);
    public static final IntegerProperty DESTRUCTION = IntegerProperty.create("destruction", 0, 2);
    public static final IntegerProperty ROTATION = IntegerProperty.create("rotation", 0, 3);
    public static final IntegerProperty FULLNESS = IntegerProperty.create("fullness", 0, 3);
    public static final IntegerProperty COLOR = IntegerProperty.create("color", 0, 7);
    public static final IntegerProperty SIZE = IntegerProperty.create("size", 0, 7);
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final IntegerProperty AGE_THREE = BlockStateProperties.AGE_2;
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");
    public static final BooleanProperty TOP = BooleanProperty.create("top");

    public enum TripleShape implements StringRepresentable {
        TOP("top", 0), MIDDLE("middle", 1), BOTTOM("bottom", 2);

        private final String name;
        private final int index;

        TripleShape(String name, int index) {
            this.name = name;
            this.index = index;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }

        public int getIndex() {
            return index;
        }

        public static TripleShape fromIndex(int index) {
            return index > 1 ? BOTTOM : index == 1 ? MIDDLE : TOP;
        }
    }

    public enum PentaShape implements StringRepresentable {
        BOTTOM("bottom"), PRE_BOTTOM("pre_bottom"), MIDDLE("middle"), PRE_TOP("pre_top"), TOP("top");

        private final String name;

        PentaShape(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
