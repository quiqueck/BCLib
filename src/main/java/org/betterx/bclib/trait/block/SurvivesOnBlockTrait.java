package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import org.betterx.wover.block.api.trait.BlockTraitKey;
import org.betterx.wover.block.api.trait.GenericBlockTrait;
import org.betterx.wover.block.impl.trait.BlockTraitImpl;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class SurvivesOnBlockTrait extends BlockTraitImpl<Block, GenericBlockTrait> implements GenericBlockTrait {
    public static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "survives_on");
    private static final Map<TagKey<Block>, SurvivesOnBlockTrait> CACHE = new HashMap<>();

    public static SurvivesOnBlockTrait withTag(TagKey<Block> survivalTag) {
        return CACHE.computeIfAbsent(survivalTag, SurvivesOnBlockTrait::new);
    }

    public final TagKey<Block> survivalTag;

    private SurvivesOnBlockTrait(TagKey<Block> survivalTag) {
        this.survivalTag = survivalTag;
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
        return state.is(survivalTag);
    }
}
