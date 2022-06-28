package org.betterx.bclib.api.v3.levelgen.features.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.Optional;

public class Is extends PlacementFilter {
    public static final Codec<Is> CODEC = RecordCodecBuilder.create((instance) -> instance
            .group(
                    BlockPredicate.CODEC
                            .fieldOf("predicate")
                            .forGetter(cfg -> cfg.predicate),
                    Vec3i.CODEC
                            .optionalFieldOf("offset")
                            .forGetter(cfg -> cfg.offset)
            )
            .apply(instance, Is::new));

    private final BlockPredicate predicate;
    private final Optional<Vec3i> offset;

    public Is(BlockPredicate predicate, Optional<Vec3i> offset) {
        this.predicate = predicate;
        this.offset = offset;
    }

    public static Is simple(BlockPredicate predicate) {
        return new Is(predicate, Optional.empty());
    }

    public static Is below(BlockPredicate predicate) {
        return new Is(predicate, Optional.of(Direction.DOWN.getNormal()));
    }

    public static Is above(BlockPredicate predicate) {
        return new Is(predicate, Optional.of(Direction.UP.getNormal()));
    }

    @Override
    protected boolean shouldPlace(PlacementContext ctx, RandomSource random, BlockPos pos) {
        WorldGenLevel level = ctx.getLevel();
        return predicate.test(level, offset.map(v -> pos.offset(v.getX(), v.getY(), v.getZ())).orElse(pos));
    }

    @Override
    public PlacementModifierType<Is> type() {
        return PlacementModifiers.IS;
    }
}
