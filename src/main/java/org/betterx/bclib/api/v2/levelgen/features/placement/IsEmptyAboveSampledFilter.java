package org.betterx.bclib.api.v2.levelgen.features.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.Random;

/**
 * Tests if there is air at two locations above the tested block position
 */
@Deprecated(forRemoval = true)
public class IsEmptyAboveSampledFilter extends PlacementFilter {
    private static final IsEmptyAboveSampledFilter DEFAULT = new IsEmptyAboveSampledFilter(4, 2);
    private static final IsEmptyAboveSampledFilter DEFAULT1 = new IsEmptyAboveSampledFilter(1, 1);
    private static final IsEmptyAboveSampledFilter DEFAULT2 = new IsEmptyAboveSampledFilter(1, 2);

    private static final IsEmptyAboveSampledFilter BELOW_DEFAULT = new IsEmptyAboveSampledFilter(-4, -2);
    private static final IsEmptyAboveSampledFilter BELOW_DEFAULT1 = new IsEmptyAboveSampledFilter(-1, -1);
    private static final IsEmptyAboveSampledFilter BELOW_DEFAULT2 = new IsEmptyAboveSampledFilter(-1, -2);
    public static final Codec<IsEmptyAboveSampledFilter> CODEC = RecordCodecBuilder.create((instance) -> instance
            .group(
                    Codec.intRange(-32, 32).fieldOf("d1").orElse(4).forGetter((p) -> p.distance1),
                    Codec.intRange(-32, 32).fieldOf("d2").orElse(2).forGetter((p) -> p.distance1)
            )
            .apply(instance, IsEmptyAboveSampledFilter::new));

    public static PlacementFilter emptyAbove4() {
        return DEFAULT;
    }

    public static PlacementFilter emptyAbove2() {
        return DEFAULT2;
    }

    public static PlacementFilter emptyAbove() {
        return DEFAULT1;
    }

    public static PlacementFilter emptyBelow4() {
        return BELOW_DEFAULT;
    }

    public static PlacementFilter emptyBelow2() {
        return BELOW_DEFAULT2;
    }

    public static PlacementFilter emptyBelow() {
        return BELOW_DEFAULT1;
    }

    public IsEmptyAboveSampledFilter(int d1, int d2) {
        this.distance1 = d1;
        this.distance2 = d2;
    }

    private final int distance1;
    private final int distance2;


    @Override
    protected boolean shouldPlace(PlacementContext ctx, Random random, BlockPos pos) {
        WorldGenLevel level = ctx.getLevel();
        return level.isEmptyBlock(pos.above(distance1))
                && (distance1 == distance2 || level.isEmptyBlock(pos.above(distance2)));
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifiers.IS_EMPTY_ABOVE_SAMPLED_FILTER;
    }
}
