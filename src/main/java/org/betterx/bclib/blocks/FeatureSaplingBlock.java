package org.betterx.bclib.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class FeatureSaplingBlock<F extends Feature<FC>, FC extends FeatureConfiguration> extends SaplingBlock {

    @FunctionalInterface
    public interface FeatureSupplier<F extends Feature<FC>, FC extends FeatureConfiguration> {
        boolean grow(
                @NotNull ServerLevel level,
                @NotNull BlockPos pos,
                @NotNull BlockState state,
                @NotNull RandomSource random
        );
    }

    /** The shape of a sapling standing on the ground. Protected so a block that can do both can pick. */
    protected static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 14, 12);
    /** The shape of a sapling hanging from a ceiling. */
    protected static final VoxelShape HANGING_SHAPE = Block.box(4, 2, 4, 12, 16, 12);
    private final FeatureSupplier<F, FC> feature;
    private final FeatureSupplier<F, FC> megaFeature;
    private final boolean hangsFromAbove;

    /**
     * What {@link #tryGrowMegaFeature} found, which is not the same question as "did a tree grow".
     * <p>
     * {@link #NO_QUAD} has to be told apart from {@link #NO_ROOM} because they lead to opposite
     * decisions: without a 2x2 the small tree is still the right thing to grow, whereas a player who
     * arranged four saplings and does not have the headroom for the big tree should get nothing rather
     * than a small tree planted in the middle of their arrangement. Vanilla's {@code TreeGrower} draws
     * the same distinction, by returning early instead of falling through.
     */
    private enum MegaGrowth {
        /**
         * No 2x2 of this sapling exists around the triggering position.
         */
        NO_QUAD,
        /**
         * A 2x2 was found and the mega feature generated.
         */
        GROWN,
        /**
         * A 2x2 was found but the mega feature declined to generate; the saplings are left standing.
         */
        NO_ROOM
    }

    public FeatureSaplingBlock(
            BlockBehaviour.Properties properties,
            FeatureSupplier<F, FC> featureSupplier
    ) {
        this(properties, featureSupplier, null, false);
    }

    /**
     * @param hangsFromAbove when {@code true} the sapling attaches to the block <em>above</em> it
     *                       (ceiling-hung, e.g. anchor-tree / nether-sakura branches) and uses a
     *                       taller upward {@link #getShape}; when {@code false} it behaves as a normal
     *                       ground sapling attaching to the block below. This is a construction-time
     *                       parameter (R8): it selects the shape/attachment, which must be known before
     *                       any world exists.
     */
    public FeatureSaplingBlock(
            BlockBehaviour.Properties properties,
            FeatureSupplier<F, FC> featureSupplier,
            boolean hangsFromAbove
    ) {
        this(properties, featureSupplier, null, hangsFromAbove);
    }

    /**
     * @param megaFeatureSupplier the larger variant of the tree, grown instead of {@code featureSupplier}
     *                            when four of these saplings stand in a 2x2 - the same opt-in vanilla uses
     *                            for dark oak. May be {@code null}, which is what every sapling without a
     *                            large variant passes and leaves the block behaving exactly as before.
     * @see #hangsFromAbove
     */
    public FeatureSaplingBlock(
            BlockBehaviour.Properties properties,
            FeatureSupplier<F, FC> featureSupplier,
            FeatureSupplier<F, FC> megaFeatureSupplier,
            boolean hangsFromAbove
    ) {
        super(null, properties);
        this.feature = featureSupplier;
        this.megaFeature = megaFeatureSupplier;
        this.hangsFromAbove = hangsFromAbove;
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        if (hangsFromAbove) {
            final BlockPos target = blockPos.above();
            return this.mayPlaceOn(levelReader.getBlockState(target), levelReader, target);
        }
        return super.canSurvive(blockState, levelReader, blockPos);
    }

    protected boolean growFeature(
            @NotNull ServerLevel world,
            @NotNull BlockPos pos,
            @NotNull BlockState blockState,
            @NotNull RandomSource random
    ) {
        if (feature != null) {
            return feature.grow(world, pos, blockState, random);
        }
        return false;
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess scheduledTickAccess,
            BlockPos pos,
            Direction neighborDirection,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource randomSource
    ) {
        if (!canSurvive(state, level, pos)) return Blocks.AIR.defaultBlockState();
        else return state;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextInt(16) == 0;
    }

    @Override
    public void advanceTree(ServerLevel world, BlockPos pos, BlockState blockState, RandomSource random) {
        if (blockState.getValue(STAGE) == 0) {
            world.setBlock(pos, blockState.cycle(STAGE), 4);
        } else {
            doGrowFeature(world, pos, blockState, random);
        }
    }

    /**
     * Grows the feature right now, skipping the {@code STAGE} step {@link #advanceTree} would spend an
     * application on.
     * <p>
     * {@code performBonemeal} goes through {@code advanceTree}, so a sapling sitting at {@code STAGE 0}
     * takes two applications to become a tree. That second click is the survival pacing; a creative
     * player bypassing the {@link #isBonemealSuccess} roll should not still have to click twice. This is
     * the entry point {@code BoneMealItemMixin} uses for that - {@link #doGrowFeature} itself is
     * {@code protected}, so a mixin in another package cannot reach it.
     *
     * @return {@code true} if the feature placed; {@code false} leaves the sapling untouched, e.g. when
     *         the tree has no room
     */
    public boolean growFeatureNow(
            ServerLevel serverLevel,
            BlockPos blockPos,
            BlockState originalBlockState,
            RandomSource randomSource
    ) {
        return doGrowFeature(serverLevel, blockPos, originalBlockState, randomSource);
    }

    protected boolean doGrowFeature(
            ServerLevel serverLevel,
            BlockPos blockPos,
            BlockState originalBlockState,
            RandomSource randomSource
    ) {
        if (megaFeature != null && megaFeatureEnabled(serverLevel, blockPos, originalBlockState)) {
            final MegaGrowth mega = tryGrowMegaFeature(serverLevel, blockPos, originalBlockState, randomSource);
            if (mega != MegaGrowth.NO_QUAD) {
                return mega == MegaGrowth.GROWN;
            }
        }

        if (feature == null || !smallFeatureEnabled(serverLevel, blockPos)) {
            return false;
        } else {
            BlockState emptyState = serverLevel.getFluidState(blockPos).createLegacyBlock();
            serverLevel.setBlock(blockPos, emptyState, 4);
            ;
            if (growFeature(serverLevel, blockPos, originalBlockState, randomSource)) {
                if (serverLevel.getBlockState(blockPos) == emptyState) {
                    serverLevel.sendBlockUpdated(blockPos, originalBlockState, emptyState, 2);
                }

                return true;
            } else {
                serverLevel.setBlock(blockPos, originalBlockState, 4);
                return false;
            }
        }
    }

    /**
     * Whether the 2x2 variant may grow right now. Subclasses override this to put the large tree behind a
     * gamerule or a config option.
     * <p>
     * Checked <em>before</em> the 2x2 is looked for, not inside {@link #megaFeature}, and the difference
     * matters: a mega feature that declines leaves the four saplings standing (see {@link MegaGrowth}),
     * which is right when the tree has no room but wrong when the player has simply turned it off. Failing
     * here instead falls through to the ordinary path, so a disabled 2x2 grows four small trees.
     */
    protected boolean megaFeatureEnabled(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull BlockState state) {
        return true;
    }

    /**
     * Whether the ordinary single-sapling feature may grow at this position.
     * <p>
     * Exists for blocks whose large variant has a placement the small one does not share - the anchor tree
     * sapling grows its hanging branch from a ceiling but its giant from a 2x2 on the floor, and a lone
     * sapling standing on the floor must therefore grow nothing at all rather than a ceiling-shaped
     * structure out of the ground. Returning {@code false} leaves the sapling untouched and standing.
     */
    protected boolean smallFeatureEnabled(@NotNull ServerLevel level, @NotNull BlockPos pos) {
        return true;
    }

    /**
     * Grows {@link #megaFeature} if the sapling at {@code blockPos} is part of a 2x2 of its own kind.
     * <p>
     * The four offsets are scanned in vanilla's order so that the same arrangement picks the same corner,
     * and the feature is grown from that corner rather than from the triggering sapling: which of the four
     * happened to tick first must not decide where the tree ends up. These features build outward from a
     * single centre and their trunks are several blocks across, so a corner origin still swallows all four
     * sapling positions.
     */
    private MegaGrowth tryGrowMegaFeature(
            ServerLevel serverLevel,
            BlockPos blockPos,
            BlockState originalBlockState,
            RandomSource randomSource
    ) {
        for (int dx = 0; dx >= -1; dx--) {
            for (int dz = 0; dz >= -1; dz--) {
                if (!isQuadOfSaplings(originalBlockState, serverLevel, blockPos, dx, dz)) continue;

                final BlockPos corner = blockPos.offset(dx, 0, dz);
                // Air rather than the fluid state the single-sapling path restores: a sapling standing in
                // a fluid cannot be part of a 2x2 that grows anything here, and vanilla clears the four
                // with air for the same reason.
                final BlockState emptyState = Blocks.AIR.defaultBlockState();
                setQuad(serverLevel, corner, emptyState);

                if (megaFeature.grow(serverLevel, corner, originalBlockState, randomSource)) {
                    // Flag 4 keeps the clearing above off the wire, so any of the four the tree did not
                    // end up covering would still be a sapling on the client. The single-sapling path
                    // repairs the same case for its one position.
                    forEachOfQuad(corner, pos -> {
                        if (serverLevel.getBlockState(pos) == emptyState) {
                            serverLevel.sendBlockUpdated(pos, originalBlockState, emptyState, 2);
                        }
                    });
                    return MegaGrowth.GROWN;
                }

                setQuad(serverLevel, corner, originalBlockState);
                return MegaGrowth.NO_ROOM;
            }
        }
        return MegaGrowth.NO_QUAD;
    }

    private static boolean isQuadOfSaplings(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            int ox,
            int oz
    ) {
        final Block block = state.getBlock();
        return level.getBlockState(pos.offset(ox, 0, oz)).is(block)
                && level.getBlockState(pos.offset(ox + 1, 0, oz)).is(block)
                && level.getBlockState(pos.offset(ox, 0, oz + 1)).is(block)
                && level.getBlockState(pos.offset(ox + 1, 0, oz + 1)).is(block);
    }

    /**
     * Writes {@code state} to all four positions of the 2x2 whose lowest corner is {@code corner}.
     * <p>
     * Flag 4 suppresses neighbour updates, which matters while the quad is half-cleared: a shape update
     * from the first write would reach the other three saplings before the tree has a chance to grow.
     */
    private static void setQuad(ServerLevel level, BlockPos corner, BlockState state) {
        forEachOfQuad(corner, pos -> level.setBlock(pos, state, 4));
    }

    private static void forEachOfQuad(BlockPos corner, Consumer<BlockPos> action) {
        action.accept(corner);
        action.accept(corner.offset(1, 0, 0));
        action.accept(corner.offset(0, 0, 1));
        action.accept(corner.offset(1, 0, 1));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        this.tick(state, world, pos, random);
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        super.tick(state, world, pos, random);
        if (isBonemealSuccess(world, random, pos, state)) {
            performBonemeal(world, random, pos, state);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        return hangsFromAbove ? HANGING_SHAPE : SHAPE;
    }
}
