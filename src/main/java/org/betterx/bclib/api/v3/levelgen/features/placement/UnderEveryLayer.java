package org.betterx.bclib.api.v3.levelgen.features.placement;

import org.betterx.bclib.util.BlocksHelper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.Optional;
import java.util.stream.Stream;

public class UnderEveryLayer
        extends PlacementModifier {
    private static final UnderEveryLayer INSTANCE = new UnderEveryLayer(Optional.empty(), Optional.empty());
    private static final UnderEveryLayer INSTANCE_MIN_4 = new UnderEveryLayer(Optional.of(4), Optional.empty());
    public static final Codec<UnderEveryLayer> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Codec.INT.optionalFieldOf("min").forGetter(o -> o.minHeight),
                    Codec.INT.optionalFieldOf("max").forGetter(o -> o.maxHeight)
            ).apply(instance, UnderEveryLayer::new));


    private final Optional<Integer> minHeight;
    private final Optional<Integer> maxHeight;

    protected UnderEveryLayer(Optional<Integer> minHeight, Optional<Integer> maxHeight) {
        this.minHeight = minHeight;

        this.maxHeight = maxHeight;
    }

    public static UnderEveryLayer simple() {
        return INSTANCE;
    }

    public static UnderEveryLayer min4() {
        return INSTANCE_MIN_4;
    }

    @Override
    public Stream<BlockPos> getPositions(
            PlacementContext ctx,
            RandomSource random,
            BlockPos pos
    ) {

        Stream.Builder<BlockPos> builder = Stream.builder();

        final int z = pos.getZ();
        final int x = pos.getX();
        final int levelHeight = ctx.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        final int minLevelHeight = ctx.getMinBuildHeight();
        int y = maxHeight.map(h -> Math.min(levelHeight, h)).orElse(levelHeight);
        final int minHeight = this.minHeight.map(h -> Math.max(minLevelHeight, h)).orElse(minLevelHeight);

        int layerY;
        do {
            layerY = findUnderGroundYPosition(ctx, x, y, z, minHeight);
            if (layerY != Integer.MAX_VALUE) {
                builder.add(new BlockPos(x, layerY, z));
                y = layerY - 1;
            }

        } while (layerY != Integer.MAX_VALUE);
        return builder.build();
    }

    @Override
    public PlacementModifierType<UnderEveryLayer> type() {
        return PlacementModifiers.UNDER_EVERY_LAYER;
    }

    private static int findUnderGroundYPosition(PlacementContext ctx, int x, int startY, int z, int minHeight) {
        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos(x, startY, z);
        BlockState nowState = ctx.getBlockState(mPos);
        for (int y = startY; y >= minHeight + 1; --y) {
            mPos.setY(y - 1);
            BlockState belowState = ctx.getBlockState(mPos);
            if (BlocksHelper.isTerrain(nowState) && BlocksHelper.isFreeOrFluid(belowState) && !nowState.is(Blocks.BEDROCK)) {
                return mPos.getY();
            }
            nowState = belowState;
        }
        return Integer.MAX_VALUE;
    }
}
