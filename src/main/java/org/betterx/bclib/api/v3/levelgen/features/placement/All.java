package org.betterx.bclib.api.v3.levelgen.features.placement;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class All extends PlacementModifier {
    private static final All INSTANCE = new All();
    public static final MapCodec<All> CODEC = MapCodec.unit(All::new);

    @Override
    public Stream<BlockPos> getPositions(
            PlacementContext placementContext,
            RandomSource randomSource,
            BlockPos blockPos
    ) {
        return IntStream.range(0, 16 * 16 - 1).mapToObj(i -> blockPos.offset(i & 0xF, 0, i >> 4));
    }

    public static PlacementModifier simple() {
        return INSTANCE;
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifiers.ALL;
    }
}
