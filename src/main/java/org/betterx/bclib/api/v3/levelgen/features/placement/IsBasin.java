package org.betterx.bclib.api.v3.levelgen.features.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.Optional;

public class IsBasin extends PlacementFilter {
    public static final Codec<IsBasin> CODEC = RecordCodecBuilder.create((instance) -> instance
            .group(
                    BlockPredicate.CODEC
                            .fieldOf("predicate")
                            .forGetter(cfg -> cfg.predicate),
                    BlockPredicate.CODEC
                            .optionalFieldOf("top_predicate")
                            .orElse(Optional.empty())
                            .forGetter(cfg -> cfg.topPredicate)
            )
            .apply(instance, IsBasin::new));

    private final BlockPredicate predicate;
    private final Optional<BlockPredicate> topPredicate;

    public IsBasin(BlockPredicate predicate) {
        this(predicate, Optional.empty());
    }

    public IsBasin(BlockPredicate predicate, Optional<BlockPredicate> topPredicate) {
        this.predicate = predicate;
        this.topPredicate = topPredicate;
    }

    public static PlacementFilter simple(BlockPredicate predicate) {
        return new IsBasin(predicate);
    }

    public static IsBasin openTop(BlockPredicate predicate) {
        return new IsBasin(predicate, Optional.of(BlockPredicate.ONLY_IN_AIR_PREDICATE));
    }

    @Override
    protected boolean shouldPlace(PlacementContext ctx, RandomSource random, BlockPos pos) {
        WorldGenLevel level = ctx.getLevel();
        if (topPredicate.isPresent() && !topPredicate.get().test(level, pos.above())) return false;

        return predicate.test(level, pos.below())
                && predicate.test(level, pos.west())
                && predicate.test(level, pos.east())
                && predicate.test(level, pos.north())
                && predicate.test(level, pos.south());
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifiers.IS_BASIN;
    }
}
