package org.betterx.bclib.api.v3.levelgen.features.placement;

import org.betterx.bclib.util.BlocksHelper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.List;
import java.util.stream.Stream;

public class FindSolidInDirection extends PlacementModifier {

    public static final Codec<FindSolidInDirection> CODEC = RecordCodecBuilder
            .create((instance) -> instance.group(
                                                  ExtraCodecs.nonEmptyList(Direction.CODEC.listOf())
                                                             .fieldOf("dir")
                                                             .orElse(List.of(Direction.DOWN))
                                                             .forGetter(a -> a.direction),
                                                  Codec.intRange(1, 32).fieldOf("dist").orElse(12).forGetter((p) -> p.maxSearchDistance),
                                                  Codec.BOOL.fieldOf("random:select").orElse(true).forGetter(p -> p.randomSelect)
                                          )
                                          .apply(
                                                  instance,
                                                  FindSolidInDirection::new
                                          ));
    protected static final FindSolidInDirection DOWN = new FindSolidInDirection(Direction.DOWN, 6);
    protected static final FindSolidInDirection UP = new FindSolidInDirection(Direction.UP, 6);
    private final List<Direction> direction;
    private final int maxSearchDistance;

    private final boolean randomSelect;
    private final IntProvider provider;


    public FindSolidInDirection(Direction direction, int maxSearchDistance) {
        this(List.of(direction), maxSearchDistance, false);
    }

    public FindSolidInDirection(List<Direction> direction, int maxSearchDistance) {
        this(direction, maxSearchDistance, direction.size() > 1);
    }

    public FindSolidInDirection(List<Direction> direction, int maxSearchDistance, boolean randomSelect) {
        this.direction = direction;
        this.maxSearchDistance = maxSearchDistance;
        this.provider = UniformInt.of(0, direction.size() - 1);
        this.randomSelect = randomSelect;
    }

    public static PlacementModifier down() {
        return DOWN;
    }

    public static PlacementModifier up() {
        return UP;
    }

    public static PlacementModifier down(int dist) {
        if (dist == DOWN.maxSearchDistance) return DOWN;
        return new FindSolidInDirection(Direction.DOWN, dist);
    }

    public static PlacementModifier up(int dist) {
        if (dist == UP.maxSearchDistance) return UP;
        return new FindSolidInDirection(Direction.UP, dist);
    }

    public Direction randomDirection(RandomSource random) {
        return direction.get(provider.sample(random));
    }

    @Override
    public Stream<BlockPos> getPositions(
            PlacementContext placementContext,
            RandomSource randomSource,
            BlockPos blockPos
    ) {
        var builder = Stream.<BlockPos>builder();
        if (randomSelect) {
            submitSingle(placementContext, blockPos, builder, randomDirection(randomSource));
        } else {
            for (Direction d : direction) {
                submitSingle(placementContext, blockPos, builder, d);
            }
        }

        return builder.build();
    }

    private void submitSingle(
            PlacementContext placementContext,
            BlockPos blockPos,
            Stream.Builder<BlockPos> builder,
            Direction d
    ) {
        int searchDist;
        BlockPos.MutableBlockPos POS = blockPos.mutable();
        if (d == Direction.EAST) { //+x
            searchDist = Math.min(maxSearchDistance, 15 - SectionPos.sectionRelative(blockPos.getX()));
        } else if (d == Direction.WEST) { //-x
            searchDist = Math.min(maxSearchDistance, SectionPos.sectionRelative(blockPos.getX()));
        } else if (d == Direction.SOUTH) { //+z
            searchDist = Math.min(maxSearchDistance, 15 - SectionPos.sectionRelative(blockPos.getZ()));
        } else if (d == Direction.NORTH) { //-z
            searchDist = Math.min(maxSearchDistance, SectionPos.sectionRelative(blockPos.getZ()));
        } else {
            searchDist = maxSearchDistance;
        }
        if (BlocksHelper.findOnSurroundingSurface(
                placementContext.getLevel(),
                POS,
                d,
                searchDist,
                BlocksHelper::isTerrain
        )) {
            builder.add(POS);
        }
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifiers.SOLID_IN_DIR;
    }
}
