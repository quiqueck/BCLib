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

public class OnEveryLayer
        extends PlacementModifier {
    private static final OnEveryLayer INSTANCE = new OnEveryLayer(Optional.empty(), Optional.empty());
    private static final OnEveryLayer INSTANCE_MIN_4 = new OnEveryLayer(Optional.of(4), Optional.empty());
    public static final Codec<OnEveryLayer> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Codec.INT.optionalFieldOf("min").forGetter(o -> o.minHeight),
                    Codec.INT.optionalFieldOf("max").forGetter(o -> o.maxHeight)
            ).apply(instance, OnEveryLayer::new));


    private final Optional<Integer> minHeight;
    private final Optional<Integer> maxHeight;

    protected OnEveryLayer(Optional<Integer> minHeight, Optional<Integer> maxHeight) {
        this.minHeight = minHeight;

        this.maxHeight = maxHeight;
    }

    public static OnEveryLayer simple() {
        return INSTANCE;
    }

    public static OnEveryLayer min4() {
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
            layerY = OnEveryLayer.findOnGroundYPosition(ctx, x, y, z, minHeight);
            if (layerY != Integer.MAX_VALUE) {
                builder.add(new BlockPos(x, layerY, z));
                y = layerY - 1;
            }

        } while (layerY != Integer.MAX_VALUE);
        return builder.build();
    }

    @Override
    public PlacementModifierType<OnEveryLayer> type() {
        return PlacementModifiers.ON_EVERY_LAYER;
    }

    private static int findOnGroundYPosition(PlacementContext ctx, int x, int startY, int z, int minHeight) {
        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos(x, startY, z);
        BlockState nowState = ctx.getBlockState(mPos);
        for (int y = startY; y >= minHeight + 1; --y) {
            mPos.setY(y - 1);
            BlockState belowState = ctx.getBlockState(mPos);
            if (BlocksHelper.isTerrain(belowState) && BlocksHelper.isFreeOrFluid(nowState) && !belowState.is(Blocks.BEDROCK)) {
                return mPos.getY() + 1;
            }
            nowState = belowState;
        }
        return Integer.MAX_VALUE;
    }
}
