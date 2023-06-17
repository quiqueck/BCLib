package org.betterx.bclib.api.v3.bonemeal;

import org.betterx.bclib.api.v3.levelgen.features.BCLConfigureFeature;
import org.betterx.bclib.api.v3.tag.BCLBlockTags;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class BonemealAPI {
    public static BonemealAPI INSTANCE = new BonemealAPI();
    private final Map<TagKey<Block>, BonemealBlockSpreader> taggedSpreaders;
    private final Map<Block, FeatureSpreader> featureSpreaders;

    private BonemealAPI() {
        taggedSpreaders = new HashMap<>();
        featureSpreaders = new HashMap<>();

        addSpreadableBlocks(BCLBlockTags.BONEMEAL_TARGET_NETHERRACK, NetherrackSpreader.INSTANCE);
        addSpreadableBlocks(BCLBlockTags.BONEMEAL_TARGET_END_STONE, EndStoneSpreader.INSTANCE);
        addSpreadableBlocks(BCLBlockTags.BONEMEAL_TARGET_OBSIDIAN, BCLBlockTags.BONEMEAL_SOURCE_OBSIDIAN);
    }

    /**
     * Bonemeal can be used to spread vegetation to neighbouring blocks.
     * <p>
     * This method allows you to register a block (the type that was clicked with bonemeal) with
     * a {@link BCLConfigureFeature} that will be placed on the bonemeald block
     * <p>
     * You can achieve the same behaviour by implementing {@link BonemealNyliumLike} on your custom
     * BlockClass. This is mainly intended for vanilla Blocks where you need to add bonemeal
     * behaviour
     *
     * @param target            The block-type
     * @param spreadableFeature the feature to place
     */
    public void addSpreadableFeatures(
            Block target,
            @NotNull BCLConfigureFeature<? extends Feature<?>, ?> spreadableFeature
    ) {
        featureSpreaders.put(target, new FeatureSpreader(target, spreadableFeature));
    }

    /**
     * Bonemeal can be used to spread for example Nylium to Netherrack.
     * <p>
     * In this example, Netherrack is the target block which will get replaced by one of the source blocks (like
     * Warped or Crimson Nylium. You can register Tag-Combinations to easily add your own behaviour for custom
     * blocks.
     * <p>
     * When a Block with the Target-Tag
     *
     * @param targetTag If you click a Block with the given Tag using Bonemeal, you will replace it with
     *                  a block from the sourceTag
     * @param sourceTag Blocks with this Tag can replace the Target block if they are in a 3x3 Neighborhood
     *                  centered around the target Block.
     */
    public void addSpreadableBlocks(@NotNull TagKey<Block> targetTag, @NotNull TagKey<Block> sourceTag) {
        taggedSpreaders.put(targetTag, new TaggedBonemealBlockSpreader(sourceTag));
    }

    /**
     * See {@link #addSpreadableBlocks(TagKey, TagKey)} for Details.
     *
     * @param targetTag If you click a Block with the given Tag using Bonemeal, you will replace it with
     *                  *                  a block from the sourceTag
     * @param spreader  The {@link BonemealBlockSpreader}-Object that is called when a corresponding target-Block
     *                  is clicked with Bone-Meal
     */
    public void addSpreadableBlocks(@NotNull TagKey<Block> targetTag, @NotNull BonemealBlockSpreader spreader) {
        taggedSpreaders.put(targetTag, spreader);
    }

    /**
     * When a block is clicked with Bonemeal, this method will be called with the state of the given Block.
     * <p>
     * If the Method returs a valid {@link BonemealBlockSpreader}-Instance, it will override the default behaviour
     * for the BoneMeal, otherwise the vanilla action will be performed.
     *
     * @param state The {@link BlockState} you need to test
     * @return A valid spreader instance, or {@code null}
     */
    @ApiStatus.Internal
    public BonemealBlockSpreader blockSpreaderForState(
            BlockGetter blockGetter,
            BlockPos pos,
            @NotNull BlockState state
    ) {
        for (var e : taggedSpreaders.entrySet()) {
            if (state.is(e.getKey()) && e.getValue().canSpreadAt(blockGetter, pos)) {
                return e.getValue();
            }
        }

        return null;
    }

    @ApiStatus.Internal
    public FeatureSpreader featureSpreaderForState(@NotNull BlockState state) {
        return featureSpreaders.get(state.getBlock());
    }

    @ApiStatus.Internal
    public boolean runSpreaders(ItemStack itemStack, Level level, BlockPos blockPos, boolean forceBonemeal) {
        BlockState blockState = level.getBlockState(blockPos);
        BonemealBlockSpreader spreader = org.betterx.bclib.api.v3.bonemeal.BonemealAPI
                .INSTANCE
                .blockSpreaderForState(level, blockPos, blockState);

        if (spreader != null) {
            if (spreader.isValidBonemealSpreadTarget(level, blockPos, blockState, level.isClientSide)) {
                if (level instanceof ServerLevel) {
                    if (spreader.performBonemealSpread((ServerLevel) level, level.random, blockPos, blockState)) {
                        itemStack.shrink(1);
                    }
                }
                return true;
            }
        }

        FeatureSpreader fSpreader = org.betterx.bclib.api.v3.bonemeal.BonemealAPI
                .INSTANCE
                .featureSpreaderForState(blockState);

        if (fSpreader != null) {
            if (fSpreader.isValidBonemealTarget(level, blockPos, blockState, level.isClientSide)) {
                if (level instanceof ServerLevel) {
                    if (forceBonemeal || fSpreader.isBonemealSuccess(level, level.random, blockPos, blockState)) {
                        fSpreader.performBonemeal((ServerLevel) level, level.random, blockPos, blockState);
                    }
                    itemStack.shrink(1);
                }
                return true;
            }
        }

        return false;
    }
}
