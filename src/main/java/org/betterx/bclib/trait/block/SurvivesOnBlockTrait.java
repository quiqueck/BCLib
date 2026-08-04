package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.config.Configs;
import de.ambertation.wover.block.api.trait.BlockTrait;
import de.ambertation.wover.block.api.trait.BlockTraitKey;
import de.ambertation.wover.block.api.trait.GenericBlockTrait;
import de.ambertation.wover.block.impl.trait.BlockTraitImpl;

import net.minecraft.ChatFormatting;
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

import com.google.common.collect.Lists;

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
     * The blocks this trait accepts, as a human-readable, comma-separated list, in the same shape used for
     * the tooltip.
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
     * {@code block}, so a block does not have to implement any special interface just to get it. Traits
     * are OR-ed by {@link #survivesOn}, so their descriptions are concatenated.
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
        appendHoverText(description, "tooltip.bclib.place_on", consumer);
    }

    /**
     * The formatting half of {@link #appendHoverText(Block, Consumer)}, split out so any caller with a
     * ready-made description (comma-separated block list) renders an identical tooltip.
     *
     * @param description     the comma-separated block list
     * @param prefixComponent the translation key to wrap it in
     * @param consumer        receives the tooltip lines
     */
    @Environment(EnvType.CLIENT)
    private static void appendHoverText(String description, String prefixComponent, Consumer<Component> consumer) {
        if (!Configs.CLIENT_CONFIG.survivesOnHint()) return;
        final int MAX_LINES = 7;
        List<String> lines = splitLines(description);
        if (lines.size() == 1) {
            consumer.accept(Component.translatable(prefixComponent, lines.get(0))
                                     .withStyle(ChatFormatting.GREEN));
        } else if (lines.size() > 1) {
            consumer.accept(Component.translatable(prefixComponent, "").withStyle(ChatFormatting.GREEN));
            for (int i = 0; i < Math.min(lines.size(), MAX_LINES); i++) {
                String line = lines.get(i);
                if (i == MAX_LINES - 1 && i < lines.size() - 1) line += " ...";
                consumer.accept(Component.literal("  " + line).withStyle(ChatFormatting.GREEN));
            }
        }
    }

    @Environment(EnvType.CLIENT)
    private static List<String> splitLines(String input) {
        final int MAX_LEN = 45;
        List<String> lines = Lists.newArrayList();

        while (input.length() > MAX_LEN) {
            int idx = input.lastIndexOf(",", MAX_LEN);
            if (idx >= 0) {
                lines.add(input.substring(0, idx + 1).trim());
                input = input.substring(idx + 1).trim();
            } else {
                break;
            }
        }
        lines.add(input.trim());

        return lines;
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
