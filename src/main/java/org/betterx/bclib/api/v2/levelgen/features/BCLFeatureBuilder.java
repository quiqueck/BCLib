package org.betterx.bclib.api.v2.levelgen.features;

import org.betterx.bclib.api.v2.levelgen.features.placement.*;
import org.betterx.bclib.api.v2.tag.CommonBlockTags;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.material.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BCLFeatureBuilder<FC extends FeatureConfiguration, F extends Feature<FC>> {
    private final List<PlacementModifier> modifications = new ArrayList<>(5);
    private final ResourceLocation featureID;
    private Decoration decoration = Decoration.VEGETAL_DECORATION;
    private final F feature;
    private BlockStateProvider provider;

    private BCLFeatureBuilder(ResourceLocation featureID, F feature) {
        this.featureID = featureID;
        this.feature = feature;
    }

    /**
     * Starts a new {@link BCLFeature} builder.
     *
     * @param featureID {@link ResourceLocation} feature identifier.
     * @param feature   {@link Feature} to construct.
     * @return {@link BCLFeatureBuilder} instance.
     */
    public static BCLFeatureBuilder start(ResourceLocation featureID, Feature<?> feature) {
        return new BCLFeatureBuilder(featureID, feature);
    }

    public static BCLFeatureBuilder<SimpleBlockConfiguration, SimpleBlockFeature> start(
            ResourceLocation featureID,
            Block block
    ) {
        return start(featureID, BlockStateProvider.simple(block));
    }

    public static BCLFeatureBuilder<SimpleBlockConfiguration, SimpleBlockFeature> start(
            ResourceLocation featureID,
            BlockState state
    ) {
        return start(featureID, BlockStateProvider.simple(state));
    }

    public static BCLFeatureBuilder<SimpleBlockConfiguration, SimpleBlockFeature> start(
            ResourceLocation featureID,
            BlockStateProvider provider
    ) {
        BCLFeatureBuilder<SimpleBlockConfiguration, SimpleBlockFeature> builder = new BCLFeatureBuilder(
                featureID,
                Feature.SIMPLE_BLOCK
        );
        builder.provider = provider;
        return builder;
    }

    /**
     * Set generation step for the feature. Default is {@code VEGETAL_DECORATION}.
     *
     * @param decoration {@link Decoration} step.
     * @return same {@link BCLFeatureBuilder} instance.
     */
    public BCLFeatureBuilder decoration(Decoration decoration) {
        this.decoration = decoration;
        return this;
    }

    /**
     * Add feature placement modifier. Used as a condition for feature how to generate.
     *
     * @param modifier {@link PlacementModifier}.
     * @return same {@link BCLFeatureBuilder} instance.
     */
    public BCLFeatureBuilder modifier(PlacementModifier modifier) {
        modifications.add(modifier);
        return this;
    }

    public BCLFeatureBuilder modifier(List<PlacementModifier> modifiers) {
        modifications.addAll(modifiers);
        return this;
    }

    /**
     * Generate feature in certain iterations (per chunk).
     *
     * @param count how many times feature will be generated in chunk.
     * @return same {@link BCLFeatureBuilder} instance.
     */
    public BCLFeatureBuilder count(int count) {
        return modifier(CountPlacement.of(count));
    }

    /**
     * Generate feature in certain iterations (per chunk). Count can be between 0 and max value.
     *
     * @param count maximum amount of iterations per chunk.
     * @return same {@link BCLFeatureBuilder} instance.
     */
    public BCLFeatureBuilder countMax(int count) {
        return modifier(CountPlacement.of(UniformInt.of(0, count)));
    }

    public BCLFeatureBuilder countRange(int min, int max) {
        return modifier(CountPlacement.of(UniformInt.of(min, max)));
    }

    /**
     * Generate feature in certain iterations (per chunk).
     * Feature will be generated on all layers (example - Nether plants).
     *
     * @param count how many times feature will be generated in chunk layers.
     * @return same {@link BCLFeatureBuilder} instance.
     */
    @SuppressWarnings("deprecation")
    public BCLFeatureBuilder countLayers(int count) {
        return modifier(CountOnEveryLayerPlacement.of(count));
    }

    /**
     * Generate feature in certain iterations (per chunk). Count can be between 0 and max value.
     * Feature will be generated on all layers (example - Nether plants).
     *
     * @param count maximum amount of iterations per chunk layers.
     * @return same {@link BCLFeatureBuilder} instance.
     */
    @SuppressWarnings("deprecation")
    public BCLFeatureBuilder countLayersMax(int count) {
        return modifier(CountOnEveryLayerPlacement.of(UniformInt.of(0, count)));
    }

    /**
     * Will place feature once every n-th attempts (in average).
     *
     * @param n amount of attempts.
     * @return same {@link BCLFeatureBuilder} instance.
     */
    public BCLFeatureBuilder onceEvery(int n) {
        return modifier(RarityFilter.onAverageOnceEvery(n));
    }

    /**
     * Restricts feature generation only to biome where feature was added.
     *
     * @return same {@link BCLFeatureBuilder} instance.
     */
    public BCLFeatureBuilder onlyInBiome() {
        return modifier(BiomeFilter.biome());
    }

    public BCLFeatureBuilder squarePlacement() {
        return modifier(InSquarePlacement.spread());
    }

    public BCLFeatureBuilder stencil() {
        return modifier(Stencil.all());
    }

    public BCLFeatureBuilder all() {
        return modifier(All.simple());
    }

    public BCLFeatureBuilder stencilOneIn4() {
        return modifier(Stencil.oneIn4());
    }

    /**
     * Select random height that is 10 above min Build height and 10 below max generation height
     *
     * @return The instance it was called on
     */
    public BCLFeatureBuilder randomHeight10FromFloorCeil() {
        return modifier(PlacementUtils.RANGE_10_10);
    }

    /**
     * Select random height that is 4 above min Build height and 10 below max generation height
     *
     * @return The instance it was called on
     */
    public BCLFeatureBuilder randomHeight4FromFloorCeil() {
        return modifier(PlacementUtils.RANGE_4_4);
    }

    /**
     * Select random height that is 8 above min Build height and 10 below max generation height
     *
     * @return The instance it was called on
     */
    public BCLFeatureBuilder randomHeight8FromFloorCeil() {
        return modifier(PlacementUtils.RANGE_8_8);
    }

    /**
     * Select random height that is above min Build height and 10 below max generation height
     *
     * @return The instance it was called on
     */
    public BCLFeatureBuilder randomHeight() {
        return modifier(PlacementUtils.FULL_RANGE);
    }

    public BCLFeatureBuilder isEmptyAbove4() {
        return modifier(IsEmptyAboveSampledFilter.emptyAbove4());
    }

    public BCLFeatureBuilder isEmptyAbove2() {
        return modifier(IsEmptyAboveSampledFilter.emptyAbove2());
    }

    public BCLFeatureBuilder isEmptyAbove() {
        return modifier(IsEmptyAboveSampledFilter.emptyAbove());
    }

    public BCLFeatureBuilder isEmptyBelow4() {
        return modifier(IsEmptyAboveSampledFilter.emptyBelow4());
    }

    public BCLFeatureBuilder isEmptyBelow2() {
        return modifier(IsEmptyAboveSampledFilter.emptyBelow2());
    }

    public BCLFeatureBuilder isEmptyBelow() {
        return modifier(IsEmptyAboveSampledFilter.emptyBelow());
    }

    public BCLFeatureBuilder isEmptyAbove(int d1, int d2) {
        return modifier(new IsEmptyAboveSampledFilter(d1, d2));
    }

    public BCLFeatureBuilder onEveryLayer() {
        return modifier(OnEveryLayer.simple());
    }

    public BCLFeatureBuilder underEveryLayer() {
        return modifier(UnderEveryLayer.simple());
    }

    public BCLFeatureBuilder spreadHorizontal(IntProvider p) {
        return modifier(RandomOffsetPlacement.horizontal(p));
    }

    public BCLFeatureBuilder spreadVertical(IntProvider p) {
        return modifier(RandomOffsetPlacement.horizontal(p));
    }

    public BCLFeatureBuilder spread(IntProvider horizontal, IntProvider vertical) {
        return modifier(RandomOffsetPlacement.of(horizontal, vertical));
    }

    public BCLFeatureBuilder offset(Direction dir) {
        return modifier(Offset.inDirection(dir));
    }

    public BCLFeatureBuilder offset(Vec3i dir) {
        return modifier(new Offset(dir));
    }

    /**
     * Cast a downward ray with max {@code distance} length to find the next solid Block.
     *
     * @param distance The maximum search Distance
     * @return The instance it was called on
     * @see #findSolidSurface(Direction, int) for Details
     */
    public BCLFeatureBuilder findSolidFloor(int distance) {
        return modifier(FindSolidInDirection.down(distance));
    }

    public BCLFeatureBuilder noiseBasedCount(float noiseLevel, int belowNoiseCount, int aboveNoiseCount) {
        return modifier(NoiseThresholdCountPlacement.of(noiseLevel, belowNoiseCount, aboveNoiseCount));
    }

    public BCLFeatureBuilder extendDown(int min, int max) {
        return modifier(new Extend(Direction.DOWN, UniformInt.of(min, max)));
    }

    public BCLFeatureBuilder inBasinOf(BlockPredicate... predicates) {
        return modifier(new IsBasin(BlockPredicate.anyOf(predicates)));
    }

    public BCLFeatureBuilder inOpenBasinOf(BlockPredicate... predicates) {
        return modifier(IsBasin.openTop(BlockPredicate.anyOf(predicates)));
    }

    public BCLFeatureBuilder is(BlockPredicate... predicates) {
        return modifier(new Is(BlockPredicate.anyOf(predicates), Optional.empty()));
    }

    public BCLFeatureBuilder isAbove(BlockPredicate... predicates) {
        return modifier(new Is(BlockPredicate.anyOf(predicates), Optional.of(Direction.DOWN.getNormal())));
    }

    public BCLFeatureBuilder isUnder(BlockPredicate... predicates) {
        return modifier(new Is(BlockPredicate.anyOf(predicates), Optional.of(Direction.UP.getNormal())));
    }

    public BCLFeatureBuilder findSolidCeil(int distance) {
        return modifier(FindSolidInDirection.up(distance));
    }

    public BCLFeatureBuilder hasMinimumDownwardSpace() {
        return modifier(MinEmptyFilter.down());
    }

    public BCLFeatureBuilder hasMinimumUpwardSpace() {
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
    public BCLFeatureBuilder findSolidSurface(Direction dir, int distance) {
        return modifier(new FindSolidInDirection(dir, distance));
    }

    public BCLFeatureBuilder findSolidSurface(List<Direction> dir, int distance, boolean randomSelect) {
        return modifier(new FindSolidInDirection(dir, distance, randomSelect));
    }

    public BCLFeatureBuilder heightmap() {
        return modifier(PlacementUtils.HEIGHTMAP);
    }

    public BCLFeatureBuilder heightmapTopSolid() {
        return modifier(PlacementUtils.HEIGHTMAP_TOP_SOLID);
    }

    public BCLFeatureBuilder heightmapWorldSurface() {
        return modifier(PlacementUtils.HEIGHTMAP_WORLD_SURFACE);
    }

    /**
     * Builds a new {@link BCLFeature} instance. Features will be registered during this process.
     *
     * @param configuration any {@link FeatureConfiguration} for provided {@link Feature}.
     * @return created {@link BCLFeature} instance.
     */
    public BCLFeature buildAndRegister(FC configuration) {
        PlacementModifier[] modifiers = modifications.toArray(new PlacementModifier[modifications.size()]);
        return new BCLFeature(featureID, feature, decoration, configuration, modifiers);
    }

    /**
     * Builds a new {@link BCLFeature} instance with {@code NONE} {@link FeatureConfiguration}.
     * Features will be registered during this process.
     *
     * @return created {@link BCLFeature} instance.
     */
    public BCLFeature buildAndRegister() {
        if (this.feature == Feature.SIMPLE_BLOCK && provider != null)
            return buildAndRegister((FC) new SimpleBlockConfiguration(provider));
        return buildAndRegister((FC) FeatureConfiguration.NONE);
    }

    @Deprecated(forRemoval = true)
    public BCLFeature build(FC configuration) {
        return buildAndRegister(configuration);
    }

    @Deprecated(forRemoval = true)
    public BCLFeature build() {
        return buildAndRegister();
    }
}
