package org.betterx.bclib.api.v3.levelgen.features;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.levelgen.structures.StructurePlacementType;
import org.betterx.bclib.api.v2.levelgen.structures.StructureWorldNBT;
import org.betterx.bclib.api.v2.poi.BCLPoiType;
import org.betterx.bclib.api.v3.levelgen.features.config.PillarFeatureConfig;
import org.betterx.bclib.api.v3.levelgen.features.config.PlaceFacingBlockConfig;
import org.betterx.bclib.api.v3.levelgen.features.config.SequenceFeatureConfig;
import org.betterx.bclib.api.v3.levelgen.features.config.TemplateFeatureConfig;
import org.betterx.bclib.api.v3.levelgen.features.features.PillarFeature;
import org.betterx.bclib.api.v3.levelgen.features.features.PlaceBlockFeature;
import org.betterx.bclib.api.v3.levelgen.features.features.SequenceFeature;
import org.betterx.bclib.api.v3.levelgen.features.features.TemplateFeature;
import org.betterx.bclib.blocks.BlockProperties;
import org.betterx.bclib.util.FullReferenceHolder;
import org.betterx.bclib.util.Triple;

import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public abstract class BCLFeatureBuilder<F extends Feature<FC>, FC extends FeatureConfiguration> {
    @FunctionalInterface
    public interface HolderBuilder<F extends Feature<FC>, FC extends FeatureConfiguration> {
        Holder<ConfiguredFeature<FC, F>> apply(
                ResourceLocation id,
                ConfiguredFeature<FC, F> feature
        );
    }

    @FunctionalInterface
    public interface FeatureBuilder<F extends Feature<FC>, FC extends FeatureConfiguration, B extends BCLConfigureFeature<F, FC>> {
        B create(ResourceLocation id, Holder<ConfiguredFeature<FC, F>> configuredFeature);
    }

    static ConcurrentLinkedQueue<BCLConfigureFeature.Unregistered<?, ?>> UNBOUND_FEATURES = new ConcurrentLinkedQueue<>();

    /**
     * Starts a new {@link BCLFeature} builder.
     *
     * @param featureID {@link ResourceLocation} feature identifier.
     * @param feature   {@link Feature} to construct.
     * @return {@link BCLFeatureBuilder} instance.
     */
    public static <F extends Feature<FC>, FC extends FeatureConfiguration> WithConfiguration<F, FC> start(
            ResourceLocation featureID,
            F feature
    ) {
        return new WithConfiguration<>(featureID, feature);
    }

    public static ForSimpleBlock start(
            ResourceLocation featureID,
            Block block
    ) {
        return start(featureID, BlockStateProvider.simple(block));
    }

    public static ForSimpleBlock start(
            ResourceLocation featureID,
            BlockState state
    ) {
        return start(featureID, BlockStateProvider.simple(state));
    }

    public static ForSimpleBlock start(
            ResourceLocation featureID,
            BlockStateProvider provider
    ) {
        return new ForSimpleBlock(
                featureID,
                (SimpleBlockFeature) Feature.SIMPLE_BLOCK,
                provider
        );
    }

    public static WeightedBlock startWeighted(ResourceLocation featureID) {
        return new WeightedBlock(
                featureID,
                (SimpleBlockFeature) Feature.SIMPLE_BLOCK
        );
    }

    public static WeightedBlockPatch startWeightedRandomPatch(ResourceLocation featureID) {
        return new WeightedBlockPatch(
                featureID,
                (RandomPatchFeature) Feature.RANDOM_PATCH
        );
    }

    public static WeightedBlockPatch startBonemealPatch(
            ResourceLocation featureID
    ) {
        return startWeightedRandomPatch(featureID).likeDefaultBonemeal();
    }

    public static RandomPatch startRandomPatch(
            ResourceLocation featureID,
            Holder<PlacedFeature> featureToPlace
    ) {
        return new RandomPatch(
                featureID,
                (RandomPatchFeature) Feature.RANDOM_PATCH,
                featureToPlace
        );
    }

    public static AsRandomSelect startRandomSelect(
            ResourceLocation featureID
    ) {
        return new AsRandomSelect(
                featureID,
                (RandomSelectorFeature) Feature.RANDOM_SELECTOR
        );
    }

    public static AsMultiPlaceRandomSelect startRandomSelect(
            ResourceLocation featureID,
            AsMultiPlaceRandomSelect.Placer placementModFunction
    ) {
        return new AsMultiPlaceRandomSelect(
                featureID,
                (RandomSelectorFeature) Feature.RANDOM_SELECTOR,
                placementModFunction
        );
    }

    public static NetherForrestVegetation startNetherVegetation(
            ResourceLocation featureID
    ) {
        return new NetherForrestVegetation(
                featureID,
                (NetherForestVegetationFeature) Feature.NETHER_FOREST_VEGETATION
        );
    }

    public static NetherForrestVegetation startBonemealNetherVegetation(ResourceLocation featureID) {
        return new NetherForrestVegetation(
                featureID,
                (NetherForestVegetationFeature) Feature.NETHER_FOREST_VEGETATION
        ).spreadHeight(1).spreadWidth(3);
    }

    public static WithTemplates startWithTemplates(ResourceLocation featureID) {
        return new WithTemplates(
                featureID,
                (TemplateFeature<TemplateFeatureConfig>) BCLFeature.TEMPLATE
        );
    }

    public static AsBlockColumn<BlockColumnFeature> startColumn(ResourceLocation featureID) {
        return new AsBlockColumn<>(
                featureID,
                (BlockColumnFeature) Feature.BLOCK_COLUMN
        );
    }

    public static AsPillar startPillar(
            ResourceLocation featureID,
            PillarFeatureConfig.KnownTransformers transformer
    ) {
        return new AsPillar(
                featureID,
                (PillarFeature) BCLFeature.PILLAR,
                transformer
        );
    }

    public static AsSequence startSequence(ResourceLocation featureID) {
        return new AsSequence(
                featureID,
                (SequenceFeature) BCLFeature.SEQUENCE
        );
    }

    public static AsOre startOre(ResourceLocation featureID) {
        return new AsOre(
                featureID,
                (OreFeature) Feature.ORE
        );
    }

    public static FacingBlock startFacing(ResourceLocation featureID) {
        return new FacingBlock(
                featureID,
                (PlaceBlockFeature<PlaceFacingBlockConfig>) BCLFeature.PLACE_BLOCK
        );
    }


    protected final ResourceLocation featureID;
    private final F feature;

    private BCLFeatureBuilder(ResourceLocation featureID, F feature) {
        this.featureID = featureID;
        this.feature = feature;
    }


    /**
     * Internally used by the builder. Normally you should not have to call this method directly as it is
     * handled by {@link #buildAndRegister(BootstapContext)}
     *
     * @param id       The ID to register this feature with
     * @param cFeature The configured Feature
     * @param <F>      The Feature Class
     * @param <FC>     The FeatureConfiguration Class
     * @return The Holder for the new Feature
     */
    public static <F extends Feature<FC>, FC extends FeatureConfiguration> Holder<ConfiguredFeature<FC, F>> register(
            BootstapContext<ConfiguredFeature<?, ?>> ctx,
            ResourceLocation id,
            ConfiguredFeature<FC, F> cFeature
    ) {
        ResourceKey<ConfiguredFeature<?, ?>> key = ResourceKey.create(Registries.CONFIGURED_FEATURE, id);
        return (Holder<ConfiguredFeature<FC, F>>) (Object) ctx.register(key, cFeature);
    }

    public abstract FC createConfiguration();

    protected BCLConfigureFeature<F, FC> buildAndCreateHolder(HolderBuilder<F, FC> holderBuilder) {
        return buildAndCreateHolder(
                (featureID, holder) -> new BCLConfigureFeature<>(featureID, holder, true),
                holderBuilder
        );
    }

    protected <B extends BCLConfigureFeature<F, FC>> B buildAndCreateHolder(
            FeatureBuilder<F, FC, B> featureBuilder,
            HolderBuilder<F, FC> holderBuilder
    ) {
        FC config = createConfiguration();
        if (config == null) {
            throw new IllegalStateException("Feature configuration for " + featureID + " can not be null!");
        }
        ConfiguredFeature<FC, F> cFeature = new ConfiguredFeature<>(feature, config);
        Holder<ConfiguredFeature<FC, F>> holder = holderBuilder.apply(featureID, cFeature);
        return featureBuilder.create(featureID, holder);
    }

    public BCLConfigureFeature<F, FC> buildAndRegister(BootstapContext<ConfiguredFeature<?, ?>> bootstrapCtx) {
        return buildAndCreateHolder((featureID, cFeature) -> register(bootstrapCtx, featureID, cFeature));
    }

    public BCLConfigureFeature<F, FC> buildInline() {
        return buildAndCreateHolder(
                (featureID, cFeature) -> Holder.direct(cFeature)
        );
    }

    public BCLConfigureFeature.Unregistered<F, FC> build() {
        final var res = buildAndCreateHolder(
                (featureID, holder) -> new BCLConfigureFeature.Unregistered<>(featureID, holder),
                (featureID, cFeature) -> (FullReferenceHolder<ConfiguredFeature<FC, F>>) (Object) FullReferenceHolder.create(
                        Registries.CONFIGURED_FEATURE,
                        featureID,
                        cFeature
                )
        );
        UNBOUND_FEATURES.add(res);
        return res;
    }

    public static void registerUnbound(BootstapContext<ConfiguredFeature<?, ?>> bootstapContext) {
        UNBOUND_FEATURES.forEach(u -> u.register(bootstapContext));
        UNBOUND_FEATURES.clear();
    }

    public BCLInlinePlacedBuilder<F, FC> inlinePlace() {
        BCLConfigureFeature<F, FC> f = buildInline();
        return BCLInlinePlacedBuilder.place(f);
    }

    public Holder<PlacedFeature> inlinePlace(BCLInlinePlacedBuilder<F, FC> placer) {
        BCLConfigureFeature<F, FC> f = buildInline();
        return placer.build(f).getPlacedFeature();
    }

    public static class AsOre extends BCLFeatureBuilder<OreFeature, OreConfiguration> {
        private final List<OreConfiguration.TargetBlockState> targetStates = new LinkedList<>();
        private int size = 6;
        private float discardChanceOnAirExposure = 0;

        private AsOre(ResourceLocation featureID, OreFeature feature) {
            super(featureID, feature);
        }

        public AsOre add(Block containedIn, Block ore) {
            return this.add(containedIn, ore.defaultBlockState());
        }

        public AsOre add(Block containedIn, BlockState ore) {
            return this.add(new BlockMatchTest(containedIn), ore);
        }

        public AsOre add(RuleTest containedIn, Block ore) {
            return this.add(containedIn, ore.defaultBlockState());
        }

        public AsOre add(RuleTest containedIn, BlockState ore) {
            targetStates.add(OreConfiguration.target(
                    containedIn,
                    ore
            ));
            return this;
        }

        public AsOre veinSize(int size) {
            this.size = size;
            return this;
        }

        public AsOre discardChanceOnAirExposure(float chance) {
            this.discardChanceOnAirExposure = chance;
            return this;
        }

        @Override
        public OreConfiguration createConfiguration() {
            return new OreConfiguration(targetStates, size, discardChanceOnAirExposure);
        }
    }

    public static class AsPillar extends BCLFeatureBuilder<PillarFeature, PillarFeatureConfig> {
        private IntProvider maxHeight;
        private IntProvider minHeight;
        private BlockStateProvider stateProvider;

        private final PillarFeatureConfig.KnownTransformers transformer;
        private Direction direction = Direction.UP;
        private BlockPredicate allowedPlacement = BlockPredicate.ONLY_IN_AIR_PREDICATE;

        private AsPillar(
                @NotNull ResourceLocation featureID,
                @NotNull PillarFeature feature,
                @NotNull PillarFeatureConfig.KnownTransformers transformer
        ) {
            super(featureID, feature);
            this.transformer = transformer;
        }

        public AsPillar allowedPlacement(BlockPredicate predicate) {
            this.allowedPlacement = predicate;
            return this;
        }

        public AsPillar direction(Direction v) {
            this.direction = v;
            return this;
        }

        public AsPillar blockState(Block v) {
            return blockState(BlockStateProvider.simple(v.defaultBlockState()));
        }

        public AsPillar blockState(BlockState v) {
            return blockState(BlockStateProvider.simple(v));
        }

        public AsPillar blockState(BlockStateProvider v) {
            this.stateProvider = v;
            return this;
        }

        public AsPillar maxHeight(int v) {
            this.maxHeight = ConstantInt.of(v);
            return this;
        }

        public AsPillar maxHeight(IntProvider v) {
            this.maxHeight = v;
            return this;
        }

        public AsPillar minHeight(int v) {
            this.minHeight = ConstantInt.of(v);
            return this;
        }

        public AsPillar minHeight(IntProvider v) {
            this.minHeight = v;
            return this;
        }


        @Override
        public PillarFeatureConfig createConfiguration() {
            if (stateProvider == null) {
                throw new IllegalStateException("A Pillar Features need a stateProvider");
            }
            if (maxHeight == null) {
                throw new IllegalStateException("A Pillar Features need a height");
            }
            if (minHeight == null) minHeight = ConstantInt.of(0);
            return new PillarFeatureConfig(
                    minHeight,
                    maxHeight,
                    direction,
                    allowedPlacement,
                    stateProvider,
                    transformer
            );
        }
    }

    public static class AsSequence extends BCLFeatureBuilder<SequenceFeature, SequenceFeatureConfig> {
        private final List<Holder<PlacedFeature>> features = new LinkedList<>();

        private AsSequence(
                @NotNull ResourceLocation featureID,
                @NotNull SequenceFeature feature
        ) {
            super(featureID, feature);
        }


        public AsSequence add(org.betterx.bclib.api.v3.levelgen.features.BCLFeature<?, ?> p) {
            return add(p.placedFeature);
        }

        public AsSequence add(Holder<PlacedFeature> p) {
            features.add(p);
            return this;
        }

        @Override
        public SequenceFeatureConfig createConfiguration() {
            return new SequenceFeatureConfig(features);
        }
    }

    public static class AsBlockColumn<FF extends Feature<BlockColumnConfiguration>> extends BCLFeatureBuilder<FF, BlockColumnConfiguration> {
        private final List<BlockColumnConfiguration.Layer> layers = new LinkedList<>();
        private Direction direction = Direction.UP;
        private BlockPredicate allowedPlacement = BlockPredicate.ONLY_IN_AIR_PREDICATE;
        private boolean prioritizeTip = false;

        private AsBlockColumn(
                @NotNull ResourceLocation featureID,
                @NotNull FF feature
        ) {
            super(featureID, feature);
        }

        public AsBlockColumn<FF> add(int height, Block block) {
            return add(ConstantInt.of(height), BlockStateProvider.simple(block));
        }

        public AsBlockColumn<FF> add(int height, BlockState state) {
            return add(ConstantInt.of(height), BlockStateProvider.simple(state));
        }

        public AsBlockColumn<FF> add(int height, BlockStateProvider state) {
            return add(ConstantInt.of(height), state);
        }

        protected static SimpleWeightedRandomList<BlockState> buildWeightedList(BlockState state) {
            return SimpleWeightedRandomList
                    .<BlockState>builder()
                    .add(state, 1)
                    .build();
        }

        public final AsBlockColumn<FF> addRandom(int height, BlockState... states) {
            return this.addRandom(ConstantInt.of(height), states);
        }

        public final AsBlockColumn<FF> addRandom(IntProvider height, BlockState... states) {
            var builder = SimpleWeightedRandomList.<BlockState>builder();
            for (BlockState state : states) builder.add(state, 1);
            return add(height, new WeightedStateProvider(builder.build()));
        }

        public AsBlockColumn<FF> add(IntProvider height, Block block) {
            return add(height, BlockStateProvider.simple(block));
        }

        public AsBlockColumn<FF> add(IntProvider height, BlockState state) {
            return add(height, BlockStateProvider.simple(state));
        }

        public AsBlockColumn<FF> add(IntProvider height, BlockStateProvider state) {
            layers.add(new BlockColumnConfiguration.Layer(height, state));
            return this;
        }

        public AsBlockColumn<FF> addTripleShape(BlockState state, IntProvider midHeight) {
            return this
                    .add(1, state.setValue(BlockProperties.TRIPLE_SHAPE, BlockProperties.TripleShape.BOTTOM))
                    .add(midHeight, state.setValue(BlockProperties.TRIPLE_SHAPE, BlockProperties.TripleShape.MIDDLE))
                    .add(1, state.setValue(BlockProperties.TRIPLE_SHAPE, BlockProperties.TripleShape.TOP));
        }

        public AsBlockColumn<FF> addTripleShapeUpsideDown(BlockState state, IntProvider midHeight) {
            return this
                    .add(1, state.setValue(BlockProperties.TRIPLE_SHAPE, BlockProperties.TripleShape.TOP))
                    .add(midHeight, state.setValue(BlockProperties.TRIPLE_SHAPE, BlockProperties.TripleShape.MIDDLE))
                    .add(1, state.setValue(BlockProperties.TRIPLE_SHAPE, BlockProperties.TripleShape.BOTTOM));
        }

        public AsBlockColumn<FF> addBottomShapeUpsideDown(BlockState state, IntProvider midHeight) {
            return this
                    .add(midHeight, state.setValue(BlockProperties.BOTTOM, false))
                    .add(1, state.setValue(BlockProperties.BOTTOM, true));
        }

        public AsBlockColumn<FF> addBottomShape(BlockState state, IntProvider midHeight) {
            return this
                    .add(1, state.setValue(BlockProperties.BOTTOM, true))
                    .add(midHeight, state.setValue(BlockProperties.BOTTOM, false));
        }

        public AsBlockColumn<FF> addTopShapeUpsideDown(BlockState state, IntProvider midHeight) {
            return this
                    .add(1, state.setValue(BlockProperties.TOP, true))
                    .add(midHeight, state.setValue(BlockProperties.TOP, false));
        }

        public AsBlockColumn<FF> addTopShape(BlockState state, IntProvider midHeight) {
            return this
                    .add(midHeight, state.setValue(BlockProperties.TOP, false))
                    .add(1, state.setValue(BlockProperties.TOP, true));
        }

        public AsBlockColumn<FF> direction(Direction dir) {
            direction = dir;
            return this;
        }

        public AsBlockColumn<FF> prioritizeTip() {
            return this.prioritizeTip(true);
        }

        public AsBlockColumn<FF> prioritizeTip(boolean v) {
            prioritizeTip = v;
            return this;
        }

        public AsBlockColumn<FF> allowedPlacement(BlockPredicate v) {
            allowedPlacement = v;
            return this;
        }

        @Override
        public BlockColumnConfiguration createConfiguration() {
            return new BlockColumnConfiguration(layers, direction, allowedPlacement, prioritizeTip);
        }
    }

    public static class WithTemplates extends BCLFeatureBuilder<TemplateFeature<TemplateFeatureConfig>, TemplateFeatureConfig> {
        private final List<StructureWorldNBT> templates = new LinkedList<>();

        private WithTemplates(
                @NotNull ResourceLocation featureID,
                @NotNull TemplateFeature<TemplateFeatureConfig> feature
        ) {
            super(featureID, feature);
        }

        public WithTemplates add(
                ResourceLocation location,
                int offsetY,
                StructurePlacementType type,
                float chance
        ) {
            templates.add(TemplateFeatureConfig.cfg(location, offsetY, type, chance));
            return this;
        }

        @Override
        public TemplateFeatureConfig createConfiguration() {
            return new TemplateFeatureConfig(templates);
        }
    }

    public static class NetherForrestVegetation extends BCLFeatureBuilder<NetherForestVegetationFeature, NetherForestVegetationConfig> {
        private SimpleWeightedRandomList.Builder<BlockState> blocks;
        private WeightedStateProvider stateProvider;
        private int spreadWidth = 8;
        private int spreadHeight = 4;

        private NetherForrestVegetation(
                @NotNull ResourceLocation featureID,
                @NotNull NetherForestVegetationFeature feature
        ) {
            super(featureID, feature);
        }

        public NetherForrestVegetation spreadWidth(int v) {
            spreadWidth = v;
            return this;
        }

        public NetherForrestVegetation spreadHeight(int v) {
            spreadHeight = v;
            return this;
        }

        public NetherForrestVegetation addAllStates(Block block, int weight) {
            Set<BlockState> states = BCLPoiType.getBlockStates(block);
            states.forEach(s -> add(block.defaultBlockState(), Math.max(1, weight / states.size())));
            return this;
        }

        public NetherForrestVegetation addAllStatesFor(IntegerProperty prop, Block block, int weight) {
            Collection<Integer> values = prop.getPossibleValues();
            values.forEach(s -> add(block.defaultBlockState().setValue(prop, s), Math.max(1, weight / values.size())));
            return this;
        }

        public NetherForrestVegetation add(Block block, int weight) {
            return add(block.defaultBlockState(), weight);
        }

        public NetherForrestVegetation add(BlockState state, int weight) {
            if (stateProvider != null) {
                throw new IllegalStateException("You can not add new state once a WeightedStateProvider was built. (" + state + ", " + weight + ")");
            }
            if (blocks == null) {
                blocks = SimpleWeightedRandomList.builder();
            }
            blocks.add(state, weight);
            return this;
        }

        public NetherForrestVegetation provider(WeightedStateProvider provider) {
            if (blocks != null) {
                throw new IllegalStateException(
                        "You can not set a WeightedStateProvider after states were added manually.");
            }
            stateProvider = provider;
            return this;
        }

        @Override
        public NetherForestVegetationConfig createConfiguration() {
            if (stateProvider == null && blocks == null) {
                throw new IllegalStateException("NetherForestVegetationConfig needs at least one BlockState");
            }
            if (stateProvider == null) stateProvider = new WeightedStateProvider(blocks.build());
            return new NetherForestVegetationConfig(stateProvider, spreadWidth, spreadHeight);
        }
    }

    public static class RandomPatch extends BCLFeatureBuilder<RandomPatchFeature, RandomPatchConfiguration> {
        private final Holder<PlacedFeature> featureToPlace;
        private int tries = 96;
        private int xzSpread = 7;
        private int ySpread = 3;

        private RandomPatch(
                @NotNull ResourceLocation featureID,
                @NotNull RandomPatchFeature feature,
                @NotNull Holder<PlacedFeature> featureToPlace
        ) {
            super(featureID, feature);
            this.featureToPlace = featureToPlace;
        }

        public RandomPatch likeDefaultNetherVegetation() {
            return likeDefaultNetherVegetation(8, 4);
        }

        public RandomPatch likeDefaultNetherVegetation(int xzSpread, int ySpread) {
            this.xzSpread = xzSpread;
            this.ySpread = ySpread;
            tries = xzSpread * xzSpread;
            return this;
        }

        public RandomPatch tries(int v) {
            tries = v;
            return this;
        }

        public RandomPatch spreadXZ(int v) {
            xzSpread = v;
            return this;
        }

        public RandomPatch spreadY(int v) {
            ySpread = v;
            return this;
        }


        @Override
        public RandomPatchConfiguration createConfiguration() {
            return new RandomPatchConfiguration(tries, xzSpread, ySpread, featureToPlace);
        }
    }

    public static class WithConfiguration<F extends Feature<FC>, FC extends FeatureConfiguration> extends BCLFeatureBuilder<F, FC> {
        private FC configuration;

        private WithConfiguration(
                @NotNull ResourceLocation featureID,
                @NotNull F feature
        ) {
            super(featureID, feature);
        }

        public WithConfiguration<F, FC> configuration(FC config) {
            this.configuration = config;
            return this;
        }


        @Override
        public FC createConfiguration() {
            if (configuration == null) {
                //Moonlight Lib seems to trigger a load of our data before
                //NoneFeatureConfiguration.NONE is initialized. This Code
                // is meant to prevent that...
                if (NoneFeatureConfiguration.NONE != null)
                    return (FC) NoneFeatureConfiguration.NONE;

                return (FC) NoneFeatureConfiguration.INSTANCE;
            }
            return configuration;
        }
    }

    public static class FacingBlock extends BCLFeatureBuilder<PlaceBlockFeature<PlaceFacingBlockConfig>, PlaceFacingBlockConfig> {
        private final SimpleWeightedRandomList.Builder<BlockState> stateBuilder = SimpleWeightedRandomList.builder();
        BlockState firstState;
        private int count = 0;
        private List<Direction> directions = PlaceFacingBlockConfig.HORIZONTAL;

        private FacingBlock(
                @NotNull ResourceLocation featureID,
                @NotNull PlaceBlockFeature<PlaceFacingBlockConfig> feature
        ) {
            super(featureID, feature);
        }


        public FacingBlock allHorizontal() {
            directions = PlaceFacingBlockConfig.HORIZONTAL;
            return this;
        }

        public FacingBlock allVertical() {
            directions = PlaceFacingBlockConfig.VERTICAL;
            return this;
        }

        public FacingBlock allDirections() {
            directions = PlaceFacingBlockConfig.ALL;
            return this;
        }

        public FacingBlock add(Block block) {
            return add(block, 1);
        }

        public FacingBlock add(BlockState state) {
            return this.add(state, 1);
        }

        public FacingBlock add(Block block, int weight) {
            return add(block.defaultBlockState(), weight);
        }

        public FacingBlock add(BlockState state, int weight) {
            if (firstState == null) firstState = state;
            count++;
            stateBuilder.add(state, weight);
            return this;
        }

        public FacingBlock addAllStates(Block block, int weight) {
            Set<BlockState> states = BCLPoiType.getBlockStates(block);
            states.forEach(s -> add(block.defaultBlockState(), Math.max(1, weight / states.size())));
            return this;
        }

        public FacingBlock addAllStatesFor(IntegerProperty prop, Block block, int weight) {
            Collection<Integer> values = prop.getPossibleValues();
            values.forEach(s -> add(block.defaultBlockState().setValue(prop, s), Math.max(1, weight / values.size())));
            return this;
        }


        @Override
        public PlaceFacingBlockConfig createConfiguration() {
            BlockStateProvider provider = null;
            if (count == 1) {
                provider = SimpleStateProvider.simple(firstState);
            } else {
                SimpleWeightedRandomList<BlockState> list = stateBuilder.build();
                if (!list.isEmpty()) {
                    provider = new WeightedStateProvider(list);
                }
            }

            if (provider == null) {
                throw new IllegalStateException("Facing Blocks need a State Provider.");
            }
            return new PlaceFacingBlockConfig(provider, directions);
        }
    }

    public static class ForSimpleBlock extends BCLFeatureBuilder<SimpleBlockFeature, SimpleBlockConfiguration> {
        private final BlockStateProvider provider;

        private ForSimpleBlock(
                @NotNull ResourceLocation featureID,
                @NotNull SimpleBlockFeature feature,
                @NotNull BlockStateProvider provider
        ) {
            super(featureID, feature);
            this.provider = provider;
        }

        @Override
        public SimpleBlockConfiguration createConfiguration() {
            return new SimpleBlockConfiguration(provider);
        }
    }

    public static class WeightedBlockPatch extends WeightedBaseBlock<RandomPatchFeature, RandomPatchConfiguration, WeightedBlockPatch> {

        private BlockPredicate groundType = null;
        private boolean isEmpty = true;
        private int tries = 96;
        private int xzSpread = 7;
        private int ySpread = 3;

        protected WeightedBlockPatch(
                @NotNull ResourceLocation featureID,
                @NotNull RandomPatchFeature feature
        ) {
            super(featureID, feature);
        }

        public WeightedBlockPatch isEmpty() {
            return this.isEmpty(true);
        }

        public WeightedBlockPatch isEmpty(boolean value) {
            this.isEmpty = value;
            return this;
        }

        public WeightedBlockPatch isOn(BlockPredicate predicate) {
            this.groundType = predicate;
            return this;
        }

        public WeightedBlockPatch isEmptyAndOn(BlockPredicate predicate) {
            return this.isEmpty().isOn(predicate);
        }

        public WeightedBlockPatch likeDefaultNetherVegetation() {
            return likeDefaultNetherVegetation(8, 4);
        }

        public WeightedBlockPatch likeDefaultNetherVegetation(int xzSpread, int ySpread) {
            this.xzSpread = xzSpread;
            this.ySpread = ySpread;
            tries = xzSpread * xzSpread;
            return this;
        }

        public WeightedBlockPatch likeDefaultBonemeal() {
            return this.tries(9)
                       .spreadXZ(3)
                       .spreadY(1);
        }

        public WeightedBlockPatch tries(int v) {
            tries = v;
            return this;
        }

        public WeightedBlockPatch spreadXZ(int v) {
            xzSpread = v;
            return this;
        }

        public WeightedBlockPatch spreadY(int v) {
            ySpread = v;
            return this;
        }

        @Override
        public RandomPatchConfiguration createConfiguration() {
            BCLInlinePlacedBuilder<Feature<SimpleBlockConfiguration>, SimpleBlockConfiguration> blockFeature = BCLFeatureBuilder
                    .start(
                            new ResourceLocation(featureID.getNamespace(), "tmp_" + featureID.getPath()),
                            Feature.SIMPLE_BLOCK
                    )
                    .configuration(new SimpleBlockConfiguration(new WeightedStateProvider(stateBuilder.build())))
                    .inlinePlace();

            if (isEmpty) blockFeature.isEmpty();
            if (groundType != null) blockFeature.isOn(groundType);

            return new RandomPatchConfiguration(tries, xzSpread, ySpread, blockFeature.build().getPlacedFeature());
        }
    }

    public static class WeightedBlock extends WeightedBaseBlock<SimpleBlockFeature, SimpleBlockConfiguration, WeightedBlock> {
        private WeightedBlock(
                @NotNull ResourceLocation featureID,
                @NotNull SimpleBlockFeature feature
        ) {
            super(featureID, feature);
        }

        @Override
        public SimpleBlockConfiguration createConfiguration() {
            return new SimpleBlockConfiguration(new WeightedStateProvider(stateBuilder.build()));
        }
    }


    private abstract static class WeightedBaseBlock<F extends Feature<FC>, FC extends FeatureConfiguration, W extends WeightedBaseBlock> extends BCLFeatureBuilder<F, FC> {
        SimpleWeightedRandomList.Builder<BlockState> stateBuilder = SimpleWeightedRandomList.builder();

        protected WeightedBaseBlock(
                @NotNull ResourceLocation featureID,
                @NotNull F feature
        ) {
            super(featureID, feature);
        }

        public W add(Block block, int weight) {
            return add(block.defaultBlockState(), weight);
        }

        public W add(BlockState state, int weight) {
            stateBuilder.add(state, weight);
            return (W) this;
        }

        public W addAllStates(Block block, int weight) {
            Set<BlockState> states = BCLPoiType.getBlockStates(block);
            states.forEach(s -> add(block.defaultBlockState(), Math.max(1, weight / states.size())));
            return (W) this;
        }

        public W addAllStatesFor(IntegerProperty prop, Block block, int weight) {
            Collection<Integer> values = prop.getPossibleValues();
            values.forEach(s -> add(block.defaultBlockState().setValue(prop, s), Math.max(1, weight / values.size())));
            return (W) this;
        }
    }

    public static class AsRandomSelect extends BCLFeatureBuilder<RandomSelectorFeature, RandomFeatureConfiguration> {
        private final List<WeightedPlacedFeature> features = new LinkedList<>();
        private Holder<PlacedFeature> defaultFeature;

        private AsRandomSelect(
                @NotNull ResourceLocation featureID,
                @NotNull RandomSelectorFeature feature
        ) {
            super(featureID, feature);
        }


        public AsRandomSelect add(Holder<PlacedFeature> feature, float weight) {
            features.add(new WeightedPlacedFeature(feature, weight));
            return this;
        }

        public AsRandomSelect defaultFeature(Holder<PlacedFeature> feature) {
            defaultFeature = feature;
            return this;
        }

        @Override
        public RandomFeatureConfiguration createConfiguration() {
            return new RandomFeatureConfiguration(features, defaultFeature);
        }
    }

    public static class AsMultiPlaceRandomSelect extends BCLFeatureBuilder<RandomSelectorFeature, RandomFeatureConfiguration> {
        public interface Placer {
            Holder<PlacedFeature> place(
                    BCLInlinePlacedBuilder<SimpleBlockFeature, SimpleBlockConfiguration> placer,
                    int id
            );
        }

        private final List<Triple<BlockStateProvider, Float, Integer>> features = new LinkedList<>();

        private final Placer modFunction;

        private AsMultiPlaceRandomSelect(
                @NotNull ResourceLocation featureID,
                @NotNull RandomSelectorFeature feature,
                @NotNull Placer mod
        ) {
            super(featureID, feature);
            this.modFunction = mod;
        }

        private static int featureCounter = 0;
        private static int lastID = 0;

        public AsMultiPlaceRandomSelect addAllStates(Block block, int weight) {
            return addAllStates(block, weight, lastID + 1);
        }

        public AsMultiPlaceRandomSelect addAll(int weight, Block... blocks) {
            return addAll(weight, lastID + 1, blocks);
        }

        public AsMultiPlaceRandomSelect addAllStatesFor(IntegerProperty prop, Block block, int weight) {
            return addAllStatesFor(prop, block, weight, lastID + 1);
        }

        public AsMultiPlaceRandomSelect add(Block block, float weight) {
            return add(BlockStateProvider.simple(block), weight);
        }

        public AsMultiPlaceRandomSelect add(BlockState state, float weight) {
            return add(BlockStateProvider.simple(state), weight);
        }

        public AsMultiPlaceRandomSelect add(BlockStateProvider provider, float weight) {
            return add(provider, weight, lastID + 1);
        }


        public AsMultiPlaceRandomSelect addAllStates(Block block, int weight, int id) {
            Set<BlockState> states = BCLPoiType.getBlockStates(block);
            SimpleWeightedRandomList.Builder<BlockState> builder = SimpleWeightedRandomList.builder();
            states.forEach(s -> builder.add(block.defaultBlockState(), 1));

            this.add(new WeightedStateProvider(builder.build()), weight, id);
            return this;
        }

        public AsMultiPlaceRandomSelect addAll(int weight, int id, Block... blocks) {
            SimpleWeightedRandomList.Builder<BlockState> builder = SimpleWeightedRandomList.builder();
            for (Block block : blocks) {
                builder.add(block.defaultBlockState(), 1);
            }

            this.add(new WeightedStateProvider(builder.build()), weight, id);
            return this;
        }

        public AsMultiPlaceRandomSelect addAllStatesFor(IntegerProperty prop, Block block, int weight, int id) {
            Collection<Integer> values = prop.getPossibleValues();
            SimpleWeightedRandomList.Builder<BlockState> builder = SimpleWeightedRandomList.builder();
            values.forEach(s -> builder.add(block.defaultBlockState().setValue(prop, s), 1));
            this.add(new WeightedStateProvider(builder.build()), weight, id);
            return this;
        }

        public AsMultiPlaceRandomSelect add(Block block, float weight, int id) {
            return add(BlockStateProvider.simple(block), weight, id);
        }

        public AsMultiPlaceRandomSelect add(BlockState state, float weight, int id) {
            return add(BlockStateProvider.simple(state), weight, id);
        }

        public AsMultiPlaceRandomSelect add(BlockStateProvider provider, float weight, int id) {
            features.add(new Triple<>(provider, weight, id));
            lastID = Math.max(lastID, id);
            return this;
        }

        private Holder<PlacedFeature> place(BlockStateProvider p, int id) {
            var builder = BCLFeatureBuilder
                    .start(BCLib.makeID("temp_select_feature" + (featureCounter++)), p)
                    .inlinePlace();
            return modFunction.place(builder, id);
        }

        @Override
        public RandomFeatureConfiguration createConfiguration() {
            if (modFunction == null) {
                throw new IllegalStateException("AsMultiPlaceRandomSelect needs a placement.modification Function");
            }
            float sum = this.features.stream().map(p -> p.second).reduce(0.0f, Float::sum);
            List<WeightedPlacedFeature> features = this.features.stream()
                                                                .map(p -> new WeightedPlacedFeature(
                                                                        this.place(p.first, p.third),
                                                                        p.second / sum
                                                                ))
                                                                .toList();


            return new RandomFeatureConfiguration(
                    features.subList(0, features.size() - 1),
                    features.get(features.size() - 1).feature
            );
        }
    }
}


