package org.betterx.bclib.interfaces;

import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public interface SurvivesOnTags extends SurvivesOnSpecialGround {
    List<TagKey<Block>> getSurvivableTags();

    @Override
    default String getSurvivableBlocksString() {
        return getSurvivableTags()
                .stream()
                .map(tag -> Registry.BLOCK.getTag(tag))
                .filter(named -> named.isPresent())
                .map(named -> named.get())
                .flatMap(named -> named.stream())
                .filter(block -> block != Blocks.AIR && block != null)
                .map(block -> {
                    ItemStack stack = new ItemStack(block.value());
                    if (stack.hasCustomHoverName()) return stack.getHoverName().getString();
                    else return block.value().getName().getString();
                })
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.joining(", "));
    }

    @Override
    default boolean isSurvivable(BlockState state) {
        return getSurvivableTags().stream().anyMatch(tag -> state.is(tag));
    }
}
