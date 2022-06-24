package org.betterx.bclib.api.v2.levelgen.features.placement;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

public class Debug extends PlacementModifier {
    public static final Debug INSTANCE = new Debug();
    public static final Codec<Debug> CODEC = Codec.unit(Debug::new);

    @Override
    public Stream<BlockPos> getPositions(
            PlacementContext placementContext,
            RandomSource randomSource,
            BlockPos blockPos
    ) {
        return Stream.of(blockPos);
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifiers.DEBUG;
    }
}
