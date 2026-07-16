package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.interfaces.SurvivesOnSpecialGround;
import org.betterx.bclib.interfaces.SurvivesOnBlocks;
import org.betterx.bclib.interfaces.SurvivesOnTags;
import org.betterx.wover.block.api.trait.BlockTrait;
import org.betterx.wover.block.api.trait.BlockTraitKey;
import org.betterx.wover.block.api.trait.GenericBlockTrait;
import org.betterx.wover.block.impl.trait.BlockTraitImpl;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SurvivesOnBlockTrait extends BlockTraitImpl<Block, GenericBlockTrait> implements GenericBlockTrait {
    public static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "survives_on");

    // Cache for tag-based traits (matches BCLib pattern)
    private static final Map<TagKey<Block>, SurvivesOnBlockTrait> TAG_CACHE = new HashMap<>();

    // Cache for block-based traits to avoid creating duplicate instances
    private static final Map<CacheKey, SurvivesOnBlockTrait> BLOCK_CACHE = new HashMap<>();

    // Tag-based survival (original BCLib pattern)
    public final TagKey<Block> survivalTag;

    // Block-based survival (new functionality)
    private final List<Block> survivalBlocks;

    public static SurvivesOnBlockTrait withTag(TagKey<Block> survivalTag) {
        return TAG_CACHE.computeIfAbsent(survivalTag, SurvivesOnBlockTrait::new);
    }

    public static SurvivesOnBlockTrait withBlocks(Block... blocks) {
        CacheKey cacheKey = new CacheKey(blocks);
        return BLOCK_CACHE.computeIfAbsent(cacheKey, k -> new SurvivesOnBlockTrait(List.of(blocks)));
    }

    private SurvivesOnBlockTrait(TagKey<Block> survivalTag) {
        this.survivalTag = survivalTag;
        this.survivalBlocks = null;
    }

    private SurvivesOnBlockTrait(List<Block> blocks) {
        this.survivalTag = null;
        this.survivalBlocks = blocks;
    }

    @Override
    public BlockTraitKey key() {
        return KEY;
    }

    @Override
    public GenericBlockTrait forRuntime() {
        return this;
    }

    public boolean isSurvivable(BlockState state) {
        if (survivalTag != null) {
            return state.is(survivalTag);
        } else if (survivalBlocks != null) {
            return survivalBlocks.contains(state.getBlock());
        }
        return false;
    }

    /**
     * Convenience for block classes: checks whether any {@link SurvivesOnBlockTrait} attached to
     * {@code block} accepts {@code ground} as valid ground. Intended to be called from a vanilla
     * {@code mayPlaceOn}/{@code canSurvive} override, replacing the old {@code SurvivesOn*} interface
     * hierarchy. Not mod-specific - any block registered with a {@link SurvivesOnBlockTrait} can use it.
     *
     * @param block  the (registered) block whose survival trait(s) to consult
     * @param ground the state of the block below / the attachment target
     * @return {@code true} if any attached {@link SurvivesOnBlockTrait} accepts {@code ground}
     */
    public static boolean survivesOn(Block block, BlockState ground) {
        return BlockTrait.runtimeTraits(block)
                         .anyMatch(t -> t instanceof SurvivesOnBlockTrait s && s.isSurvivable(ground));
    }

    /**
     * The blocks this trait accepts, as a human-readable, comma-separated list, in the same shape
     * {@link SurvivesOnTags}/{@link SurvivesOnBlocks} produce for the tooltip.
     *
     * @return the description, or an empty string if the trait resolves to nothing
     */
    public String getSurvivableBlocksString() {
        final Stream<Block> blocks;
        if (survivalTag != null) {
            blocks = BuiltInRegistries.BLOCK
                    .get(survivalTag)
                    .stream()
                    .flatMap(HolderSet.ListBacked::stream)
                    .map(Holder::value);
        } else if (survivalBlocks != null) {
            blocks = survivalBlocks.stream();
        } else {
            return "";
        }

        return blocks
                .filter(block -> block != null && block != Blocks.AIR)
                .map(block -> {
                    final ItemStack stack = new ItemStack(block);
                    if (stack.has(DataComponents.CUSTOM_NAME)) return stack.getHoverName().getString();
                    return block.getName().getString();
                })
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.joining(", "));
    }

    /**
     * Appends the "can be placed on ..." tooltip for every {@link SurvivesOnBlockTrait} attached to
     * {@code block}, so a block does not have to implement {@link SurvivesOnSpecialGround} just to get it.
     * Traits are OR-ed by {@link #survivesOn}, so their descriptions are concatenated.
     *
     * @param block    the (registered) block whose survival trait(s) to describe
     * @param consumer receives the tooltip lines
     */
    @Environment(EnvType.CLIENT)
    public static void appendHoverText(Block block, Consumer<Component> consumer) {
        final String description = BlockTrait
                .runtimeTraits(block)
                .filter(t -> t instanceof SurvivesOnBlockTrait)
                .map(t -> ((SurvivesOnBlockTrait) t).getSurvivableBlocksString())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(", "));
        if (description.isEmpty()) return;
        SurvivesOnSpecialGround.appendHoverText(description, "tooltip.bclib.place_on", consumer);
    }

    // Cache key for block-based traits
    private static class CacheKey {
        private final Block[] blocks;
        private final int hashCode;

        CacheKey(Block[] blocks) {
            this.blocks = blocks.clone();
            Arrays.sort(
                    this.blocks, (a, b) ->
                            System.identityHashCode(a) - System.identityHashCode(b)
            );
            this.hashCode = Arrays.hashCode(this.blocks);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof CacheKey)) return false;
            CacheKey other = (CacheKey) obj;
            return Arrays.equals(this.blocks, other.blocks);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
