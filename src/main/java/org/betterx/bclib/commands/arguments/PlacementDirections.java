package org.betterx.bclib.commands.arguments;

import de.ambertation.wunderlib.math.Float3;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;

public enum PlacementDirections implements StringRepresentable {
    NORTH_OF("northOf", Float3.NORTH),
    EAST_OF("eastOf", Float3.EAST),
    SOUTH_OF("southOf", Float3.SOUTH),
    WEST_OF("westOf", Float3.WEST),
    ABOVE("above", Float3.UP),
    BELOW("below", Float3.DOWN),
    AT("at", null);

    public static final Codec<PlacementDirections> CODEC = StringRepresentable.fromEnum(PlacementDirections::values);

    private final String name;
    public final Float3 dir;

    PlacementDirections(String name, Float3 dir) {
        this.name = name;
        this.dir = dir;
    }

    public BlockPos getOffset() {
        return dir == null || dir == Float3.ZERO ? null : dir.toBlockPos();
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
