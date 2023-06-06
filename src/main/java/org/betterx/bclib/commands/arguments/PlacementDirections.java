package org.betterx.bclib.commands.arguments;

import de.ambertation.wunderlib.math.Bounds;
import de.ambertation.wunderlib.math.Float3;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;

public enum PlacementDirections implements StringRepresentable {
    NORTH_OF("northOf", Float3.NORTH, PlacementDirections::resetStartNorthSouth),
    EAST_OF("eastOf", Float3.EAST, PlacementDirections::resetStartEastWest),
    SOUTH_OF("southOf", Float3.SOUTH, PlacementDirections::resetStartNorthSouth),
    WEST_OF("westOf", Float3.WEST, PlacementDirections::resetStartEastWest),
    ABOVE("above", Float3.UP, PlacementDirections::resetStartEastWest),
    BELOW("below", Float3.DOWN, PlacementDirections::resetStartEastWest),
    AT("at", null, null);

    public static final Codec<PlacementDirections> CODEC = StringRepresentable.fromEnum(PlacementDirections::values);

    interface ResetStart {
        Float3 calculate(Bounds totalBounds, BlockPos lastStart, int offset);
    }

    private static Float3 resetStartNorthSouth(Bounds totalBounds, BlockPos lastStart, int offset) {
        return Float3.of(totalBounds.max.x + offset, lastStart.getY(), lastStart.getZ());
    }

    private static Float3 resetStartEastWest(Bounds totalBounds, BlockPos lastStart, int offset) {
        return Float3.of(lastStart.getX(), lastStart.getY(), totalBounds.max.z + offset);
    }

    private final String name;
    public final Float3 dir;
    private final ResetStart resetStart;

    PlacementDirections(String name, Float3 dir, ResetStart resetStart) {
        this.name = name;
        this.dir = dir;
        this.resetStart = resetStart;
    }

    public BlockPos getOffset() {
        return dir == null || dir == Float3.ZERO ? null : dir.toBlockPos();
    }

    public int sizeInDirection(Bounds totalBounds) {
        return (int) totalBounds.getSize().mul(dir).length();
    }

    public BlockPos advanceStart(Bounds placedBound, BlockPos lastStart) {
        return advanceStart(placedBound, lastStart, 1);
    }

    public BlockPos advanceStart(Bounds placedBound, BlockPos lastStart, int offset) {
        return lastStart.offset(dir.mul(sizeInDirection(placedBound) + offset).toBlockPos());
    }

    public BlockPos resetStart(Bounds totalBounds, BlockPos lastStart) {
        return resetStart(totalBounds, lastStart, 3);
    }

    public BlockPos resetStart(Bounds totalBounds, BlockPos lastStart, int offset) {
        if (resetStart == null) {
            return lastStart;
        }
        return this.resetStart.calculate(totalBounds, lastStart, offset).toBlockPos();
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
