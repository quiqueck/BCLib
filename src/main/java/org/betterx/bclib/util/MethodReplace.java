package org.betterx.bclib.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;

import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

public class MethodReplace {
    private static Function<ItemStack, Boolean> itemReplace;
    private static Function<BlockStateBase, Boolean> blockReplace;
    private static Block block;
    private static Item item;

    public static void addItemReplace(Item item, Function<ItemStack, Boolean> itemReplace) {
        MethodReplace.itemReplace = itemReplace;
        MethodReplace.item = item;
    }

    @Deprecated(forRemoval = true)
    public static void addBlockReplace(Block block, Function<BlockStateBase, Boolean> blockReplace) {
        MethodReplace.blockReplace = blockReplace;
        MethodReplace.block = block;
    }

    @Nullable
    public static Function<ItemStack, Boolean> getItemReplace(Item item) {
        if (MethodReplace.item != item) {
            return null;
        }
        Function<ItemStack, Boolean> replace = itemReplace;
        itemReplace = null;
        return replace;
    }

    @Nullable
    @Deprecated(forRemoval = true)
    public static Function<BlockStateBase, Boolean> getBlockReplace(Block block) {
        if (MethodReplace.block != block) {
            return null;
        }
        Function<BlockStateBase, Boolean> replace = blockReplace;
        blockReplace = null;
        return replace;
    }
}
