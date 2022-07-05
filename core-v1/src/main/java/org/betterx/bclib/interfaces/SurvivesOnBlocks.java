package org.betterx.bclib.interfaces;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public interface SurvivesOnBlocks extends SurvivesOnSpecialGround {
    List<Block> getSurvivableBlocks();

    @Override
    default String getSurvivableBlocksString() {
        return getSurvivableBlocks()
                .stream()
                .filter(block -> block != Blocks.AIR && block != null)
                .map(block -> {
                    ItemStack stack = new ItemStack(block);
                    if (stack.hasCustomHoverName()) return stack.getHoverName().getString();
                    else return block.getName().getString();
                })
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.joining(", "));
    }

    @Override
    default boolean isSurvivable(BlockState state) {
        return getSurvivableBlocks().contains(state.getBlock());
    }
}
