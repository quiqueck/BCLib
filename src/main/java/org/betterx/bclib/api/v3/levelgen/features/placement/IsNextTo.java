package org.betterx.bclib.api.v3.levelgen.features.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class IsNextTo extends PlacementFilter {
    public static final Codec<IsNextTo> CODEC = RecordCodecBuilder.create((instance) -> instance
            .group(
                    BlockPredicate.CODEC
                            .fieldOf("predicate")
                            .forGetter(cfg -> cfg.predicate),
                    Vec3i.CODEC
                            .optionalFieldOf("offset")
                            .forGetter(cfg -> Optional.of(cfg.offset))
            )
            .apply(instance, IsNextTo::new));

    private final BlockPredicate predicate;
    private final Vec3i offset;

    public IsNextTo(BlockPredicate predicate) {
        this(predicate, Optional.of(Vec3i.ZERO));
    }

    public IsNextTo(BlockPredicate predicate, Optional<Vec3i> offset) {
        this(predicate, offset.orElse(Vec3i.ZERO));
    }

    public IsNextTo(@NotNull BlockPredicate predicate, @NotNull Vec3i offset) {
        this.predicate = predicate;
        this.offset = offset;
    }

    public static PlacementFilter simple(BlockPredicate predicate) {
        return new IsBasin(predicate);
    }

    @Override
    protected boolean shouldPlace(PlacementContext ctx, RandomSource random, BlockPos pos) {
        WorldGenLevel level = ctx.getLevel();

        pos = pos.offset(this.offset);
        return predicate.test(level, pos.west())
                || predicate.test(level, pos.east())
                || predicate.test(level, pos.north())
                || predicate.test(level, pos.south());
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifiers.IS_NEXT_TO;
    }
}
