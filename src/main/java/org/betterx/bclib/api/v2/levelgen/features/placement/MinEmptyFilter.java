package org.betterx.bclib.api.v2.levelgen.features.placement;

import org.betterx.bclib.util.BlocksHelper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.Random;

@Deprecated(forRemoval = true)
public class MinEmptyFilter extends PlacementFilter {
    private static final MinEmptyFilter DOWN = new MinEmptyFilter(Direction.DOWN, 2);
    private static final MinEmptyFilter UP = new MinEmptyFilter(Direction.UP, 2);
    public static final Codec<MinEmptyFilter> CODEC = RecordCodecBuilder.create((instance) -> instance
            .group(
                    Direction.CODEC.fieldOf("dir").orElse(Direction.DOWN).forGetter((p) -> p.direction),
                    Codec.intRange(1, 32).fieldOf("dist").orElse(12).forGetter((p) -> p.distance)
            )
            .apply(instance, MinEmptyFilter::new));

    private final Direction direction;
    private final int distance;

    protected MinEmptyFilter(Direction direction, int distance) {
        this.direction = direction;
        this.distance = distance;
    }

    public static PlacementModifier down() {
        return DOWN;
    }

    public static PlacementModifier down(int dist) {
        return new MinEmptyFilter(Direction.DOWN, dist);
    }

    public static PlacementModifier up() {
        return UP;
    }

    public static PlacementModifier up(int dist) {
        return new MinEmptyFilter(Direction.UP, dist);
    }

    @Override
    protected boolean shouldPlace(PlacementContext ctx, Random Random, BlockPos pos) {
        return BlocksHelper.isFreeSpace(
                ctx.getLevel(),
                pos.relative(direction),
                direction,
                distance - 1,
                BlocksHelper::isFree
        );
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifiers.MIN_EMPTY_FILTER;
    }
}
