package org.betterx.bclib.api.v3.tag;

import org.betterx.worlds.together.tag.v3.TagManager;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.ApiStatus;

public class BCLBlockTags {
    public static final TagKey<Block> BONEMEAL_SOURCE_NETHERRACK = TagManager.BLOCKS.makeTogetherTag(
            "bonemeal/source/netherrack"
    );
    public static final TagKey<Block> BONEMEAL_TARGET_NETHERRACK = TagManager.BLOCKS.makeTogetherTag(
            "bonemeal/target/netherrack"
    );
    public static final TagKey<Block> BONEMEAL_SOURCE_END_STONE = TagManager.BLOCKS.makeTogetherTag(
            "bonemeal/source/end_stone"
    );
    public static final TagKey<Block> BONEMEAL_TARGET_END_STONE = TagManager.BLOCKS.makeTogetherTag(
            "bonemeal/target/end_stone"
    );
    public static final TagKey<Block> BONEMEAL_SOURCE_OBSIDIAN = TagManager.BLOCKS.makeTogetherTag(
            "bonemeal/source/obsidian"
    );
    public static final TagKey<Block> BONEMEAL_TARGET_OBSIDIAN = TagManager.BLOCKS.makeTogetherTag(
            "bonemeal/target/obsidian"
    );

    @ApiStatus.Internal
    public static void ensureStaticallyLoaded() {
        TagManager.BLOCKS.add(BONEMEAL_SOURCE_NETHERRACK, Blocks.WARPED_NYLIUM, Blocks.CRIMSON_NYLIUM);
        TagManager.BLOCKS.add(BONEMEAL_TARGET_NETHERRACK, Blocks.NETHERRACK);
        TagManager.BLOCKS.add(BONEMEAL_TARGET_END_STONE, Blocks.END_STONE);
        TagManager.BLOCKS.add(BONEMEAL_TARGET_OBSIDIAN, Blocks.OBSIDIAN);
    }
}
