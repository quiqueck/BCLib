package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import org.betterx.wover.block.api.trait.BlockTraitKey;
import org.betterx.wover.block.api.trait.GenericBlockTrait;
import org.betterx.wover.block.impl.trait.BlockTraitImpl;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
