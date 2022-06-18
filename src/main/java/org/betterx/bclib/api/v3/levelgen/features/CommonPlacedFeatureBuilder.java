package org.betterx.bclib.api.v3.levelgen.features;

import org.betterx.bclib.api.v2.levelgen.features.config.PlaceFacingBlockConfig;
import org.betterx.bclib.api.v2.levelgen.features.placement.*;
import org.betterx.bclib.api.v2.tag.CommonBlockTags;

import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.material.Material;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

abstract class CommonPlacedFeatureBuilder<F extends Feature<FC>, FC extends FeatureConfiguration, T extends CommonPlacedFeatureBuilder<F, FC, T>> {
    protected final List<PlacementModifier> modifications = new LinkedList<>();


    /**
     * Add feature placement modifier. Used as a condition for feature how to generate.
     *
     * @param modifiers {@link PlacementModifier}s to add.
     * @return same {@link CommonPlacedFeatureBuilder} instance.
     */
    public T modifier(PlacementModifier... modifiers) {
        for (var m : modifiers)
            modifications.add(m);
        return (T) this;
    }

    /**
     * Add feature placement modifier. Used as a condition for feature how to generate.
     *
     * @param modifiers {@link PlacementModifier}s to add.
     * @return same {@link CommonPlacedFeatureBuilder} instance.
     */
    public T modifier(List<PlacementModifier> modifiers) {
        modifications.addAll(modifiers);
        return (T) this;
    }

    /**
     * Generate feature in certain iterations (per chunk).
     *
     * @param count how many times feature will be generated in chunk.
     * @return same {@link CommonPlacedFeatureBuilder} instance.
     */
    public T count(int count) {
        return modifier(CountPlacement.of(count));
    }

    /**
     * Generate feature in certain iterations (per chunk). Count can be between 0 and max value.
     *
     * @param count maximum amount of iterations per chunk.
     * @return same {@link CommonPlacedFeatureBuilder} instance.
     */
    public T countMax(int count) {
        return modifier(CountPlacement.of(UniformInt.of(0, count)));
    }

    public T countRange(int min, int max) {
        return modifier(CountPlacement.of(UniformInt.of(min, max)));
    }

    /**
     * Generate points in a stencil pattern
     *
     * @return same {@link CommonPlacedFeatureBuilder} instance.
     */
    public T stencil() {
        return modifier(Stencil.all());
    }

    /**
     * Generate points in a stencil pattern and selecting (on average) only every 4th
     *
     * @return same {@link CommonPlacedFeatureBuilder} instance.
     */
    public T stencilOneIn4() {
        return modifier(Stencil.oneIn4());
    }

    /**
     * Generate points for every xz-Coordinate in a chunk. Be carefuller, this is quite expensive!
     *
     * @return same {@link CommonPlacedFeatureBuilder} instance.
     */
    public T all() {
        return modifier(All.simple());
    }

    /**
     * Generate feature in certain iterations (per chunk).
     * Feature will be generated on all layers (example - Nether plants).
     *
     * @param count how many times feature will be generated in chunk layers.
     * @return same {@link CommonPlacedFeatureBuilder} instance.
     */
    @SuppressWarnings("deprecation")
    public T onEveryLayer(int count) {
        return modifier(CountOnEveryLayerPlacement.of(count));
    }

    /**
     * Generate feature in certain iterations (per chunk). Count can be between 0 and max value.
     * Feature will be generated on all layers (example - Nether plants).
     *
     * @param count maximum amount of iterations per chunk layers.
     * @return same {@link CommonPlacedFeatureBuilder} instance.
     */
    @SuppressWarnings("deprecation")
    public T onEveryLayerMax(int count) {
        return modifier(CountOnEveryLayerPlacement.of(UniformInt.of(0, count)));
    }

    public T onEveryLayer() {
        return modifier(OnEveryLayer.simple());
    }

    public T onEveryLayerMin4() {
        return modifier(OnEveryLayer.min4());
    }

    public T underEveryLayer() {
        return modifier(UnderEveryLayer.simple());
    }

    public T underEveryLayerMin4() {
        return modifier(UnderEveryLayer.min4());
    }

    /**
     * Will place feature once every n-th attempts (in average).
     *
     * @param n amount of attempts.
     * @return same {@link CommonPlacedFeatureBuilder} instance.
     */
    public T onceEvery(int n) {
        return modifier(RarityFilter.onAverageOnceEvery(n));
    }

    /**
     * Restricts feature generation only to biome where feature was added.
     *
     * @return same {@link CommonPlacedFeatureBuilder} instance.
     */
    public T onlyInBiome() {
        return modifier(BiomeFilter.biome());
    }

    /**
     * Randomize the xz-Coordinates
     *
     * @return same {@link CommonPlacedFeatureBuilder} instance.
     */
    public T squarePlacement() {
        return modifier(InSquarePlacement.spread());
    }


    /**
     * Select random height that is 10 above min Build height and 10 below max generation height
     *
     * @return The instance it was called on
     */
    public T randomHeight10FromFloorCeil() {
        return modifier(PlacementUtils.RANGE_10_10);
    }

    /**
     * Select random height that is 4 above min Build height and 10 below max generation height
     *
     * @return The instance it was called on
     */
    public T randomHeight4FromFloorCeil() {
        return modifier(PlacementUtils.RANGE_4_4);
    }

    /**
     * Select random height that is 8 above min Build height and 10 below max generation height
     *
     * @return The instance it was called on
     */
    public T randomHeight8FromFloorCeil() {
        return modifier(PlacementUtils.RANGE_8_8);
    }

    /**
     * Select random height that is above min Build height and 10 below max generation height
     *
     * @return The instance it was called on
     */
    public T randomHeight() {
        return modifier(PlacementUtils.FULL_RANGE);
    }

    public T isEmptyAbove4() {
        return modifier(IsEmptyAboveSampledFilter.emptyAbove4());
    }

    public T isEmptyAbove2() {
        return modifier(IsEmptyAboveSampledFilter.emptyAbove2());
    }

    public T isEmptyAbove() {
        return modifier(IsEmptyAboveSampledFilter.emptyAbove());
    }

    public T isEmptyBelow4() {
        return modifier(IsEmptyAboveSampledFilter.emptyBelow4());
    }

    public T isEmptyBelow2() {
        return modifier(IsEmptyAboveSampledFilter.emptyBelow2());
    }

    public T isEmptyBelow() {
        return modifier(IsEmptyAboveSampledFilter.emptyBelow());
    }

    public T isEmptyAbove(int d1, int d2) {
        return modifier(new IsEmptyAboveSampledFilter(d1, d2));
    }

    public T spreadHorizontal(IntProvider p) {
        return modifier(RandomOffsetPlacement.horizontal(p));
    }

    public T spreadVertical(IntProvider p) {
        return modifier(RandomOffsetPlacement.horizontal(p));
    }

    public T spread(IntProvider horizontal, IntProvider vertical) {
        return modifier(RandomOffsetPlacement.of(horizontal, vertical));
    }

    public T offset(Direction dir) {
        return modifier(Offset.inDirection(dir));
    }

    public T offset(Vec3i dir) {
        return modifier(new Offset(dir));
    }

    /**
     * Cast a downward ray with max {@code distance} length to find the next solid Block.
     *
     * @param distance The maximum search Distance
     * @return The instance it was called on
     * @see #findSolidSurface(Direction, int) for Details
     */
    public T findSolidFloor(int distance) {
        return modifier(FindSolidInDirection.down(distance));
    }

    public T noiseBasedCount(float noiseLevel, int belowNoiseCount, int aboveNoiseCount) {
        return modifier(NoiseThresholdCountPlacement.of(noiseLevel, belowNoiseCount, aboveNoiseCount));
    }

    public T extendDown(int min, int max) {
        return modifier(new Extend(Direction.DOWN, UniformInt.of(min, max)));
    }

    public T inBasinOf(BlockPredicate... predicates) {
        return modifier(new IsBasin(BlockPredicate.anyOf(predicates)));
    }

    public T inOpenBasinOf(BlockPredicate... predicates) {
        return modifier(IsBasin.openTop(BlockPredicate.anyOf(predicates)));
    }

    public T is(BlockPredicate... predicates) {
        return modifier(new Is(BlockPredicate.anyOf(predicates), Optional.empty()));
    }

    public T isAbove(BlockPredicate... predicates) {
        return modifier(new Is(BlockPredicate.anyOf(predicates), Optional.of(Direction.DOWN.getNormal())));
    }

    public T isUnder(BlockPredicate... predicates) {
        return modifier(new Is(BlockPredicate.anyOf(predicates), Optional.of(Direction.UP.getNormal())));
    }

    public T findSolidCeil(int distance) {
        return modifier(FindSolidInDirection.up(distance));
    }

    public T hasMinimumDownwardSpace() {
        return modifier(MinEmptyFilter.down());
    }

    public T hasMinimumUpwardSpace() {
        return modifier(MinEmptyFilter.up());
    }


    /**
     * Cast a ray with max {@code distance} length to find the next solid Block. The ray will travel through replaceable
     * Blocks (see {@link Material#isReplaceable()}) and will be accepted if it hits a block with the
     * {@link CommonBlockTags#TERRAIN}-tag
     *
     * @param dir      The direction the ray is cast
     * @param distance The maximum search Distance
     * @return The instance it was called on
     * @see #findSolidSurface(Direction, int) for Details
     */
    public T findSolidSurface(Direction dir, int distance) {
        return modifier(new FindSolidInDirection(dir, distance));
    }

    public T findSolidSurface(List<Direction> dir, int distance, boolean randomSelect) {
        return modifier(new FindSolidInDirection(dir, distance, randomSelect));
    }

    public T heightmap() {
        return modifier(PlacementUtils.HEIGHTMAP);
    }

    public T heightmapTopSolid() {
        return modifier(PlacementUtils.HEIGHTMAP_TOP_SOLID);
    }

    public T heightmapWorldSurface() {
        return modifier(PlacementUtils.HEIGHTMAP_WORLD_SURFACE);
    }

    public T extendXZ(int xzSpread) {
        IntProvider xz = UniformInt.of(0, xzSpread);
        return (T) modifier(
                new ForAll(List.of(
                        new Extend(Direction.NORTH, xz),
                        new Extend(Direction.SOUTH, xz),
                        new Extend(Direction.EAST, xz),
                        new Extend(Direction.WEST, xz)
                )),
                new ForAll(List.of(
                        new Extend(Direction.EAST, xz),
                        new Extend(Direction.WEST, xz),
                        new Extend(Direction.NORTH, xz),
                        new Extend(Direction.SOUTH, xz)
                ))
        );
    }

    public T extendXYZ(int xzSpread, int ySpread) {
        IntProvider xz = UniformInt.of(0, xzSpread);
        return (T) extendXZ(xzSpread).extendDown(1, ySpread);
    }

    public T isEmpty() {
        return modifier(BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE));
    }


    public T is(BlockPredicate predicate) {
        return modifier(BlockPredicateFilter.forPredicate(predicate));
    }

    public T isNextTo(BlockPredicate predicate) {
        return modifier(new IsNextTo(predicate));
    }

    public T belowIsNextTo(BlockPredicate predicate) {
        return modifier(new IsNextTo(predicate, Direction.DOWN.getNormal()));
    }

    public T isNextTo(BlockPredicate predicate, Vec3i offset) {
        return modifier(new IsNextTo(predicate, offset));
    }

    public T isOn(BlockPredicate predicate) {
        return modifier(Is.below(predicate));
    }

    public T isEmptyAndOn(BlockPredicate predicate) {
        return (T) this.isEmpty().isOn(predicate);
    }

    public T isEmptyAndOnNylium() {
        return isEmptyAndOn(BlockPredicates.ONLY_NYLIUM);
    }

    public T isEmptyAndOnNetherGround() {
        return isEmptyAndOn(BlockPredicates.ONLY_NETHER_GROUND);
    }

    public T isUnder(BlockPredicate predicate) {
        return modifier(Is.above(predicate));
    }

    public T isEmptyAndUnder(BlockPredicate predicate) {
        return (T) this.isEmpty().isUnder(predicate);
    }

    public T isEmptyAndUnderNylium() {
        return isEmptyAndUnder(BlockPredicates.ONLY_NYLIUM);
    }

    public T isEmptyAndUnderNetherGround() {
        return isEmptyAndUnder(BlockPredicates.ONLY_NETHER_GROUND);
    }

    public T vanillaNetherGround(int countPerLayer) {
        return (T) this.onEveryLayer(countPerLayer).onlyInBiome();
    }

    public T betterNetherGround(int countPerLayer) {
        return (T) this.count(countPerLayer).squarePlacement().onEveryLayerMin4().onlyInBiome();
    }

    public T betterNetherCeiling(int countPerLayer) {
        return (T) this.count(countPerLayer).squarePlacement().underEveryLayerMin4().onlyInBiome();
    }

    public T betterNetherWall(int countPerLayer) {
        return (T) this.count(countPerLayer)
                       .squarePlacement()
                       .randomHeight4FromFloorCeil()
                       .modifier(new FindSolidInDirection(PlaceFacingBlockConfig.HORIZONTAL, 12, false))
                       .onlyInBiome();
    }

    /**
     * Builds a new inline (not registered) {@link PlacedFeature}.
     *
     * @return created {@link PlacedFeature} instance.
     */
    abstract Holder<PlacedFeature> build();

    public BCLFeatureBuilder.RandomPatch inRandomPatch(ResourceLocation id) {
        return BCLFeatureBuilder.startRandomPatch(id, build());
    }

    public BCLFeatureBuilder.AsSequence then(ResourceLocation id) {
        return BCLFeatureBuilder.startSequence(id).add(build());
    }

    public BCLFeatureBuilder.ForSimpleBlock putBlock(ResourceLocation id, Block block) {
        return BCLFeatureBuilder.start(id, block);
    }

    public BCLFeatureBuilder.ForSimpleBlock putBlock(ResourceLocation id, BlockState state) {
        return BCLFeatureBuilder.start(id, state);
    }

    public BCLFeatureBuilder.ForSimpleBlock putBlock(
            ResourceLocation id,
            BlockStateProvider provider
    ) {
        return BCLFeatureBuilder.start(id, provider);
    }
}
