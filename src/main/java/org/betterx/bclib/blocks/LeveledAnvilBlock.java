package org.betterx.bclib.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MaterialColor;

import java.util.List;

public class LeveledAnvilBlock extends BaseAnvilBlock {
    protected final int level;

    public LeveledAnvilBlock(MaterialColor color, int level) {
        super(color);
        this.level = level;
    }

    public static int getAnvilCraftingLevel(Block anvil) {
        if (anvil instanceof LeveledAnvilBlock l) return l.getCraftingLevel();
        if (anvil == Blocks.ANVIL || anvil == Blocks.CHIPPED_ANVIL || anvil == Blocks.DAMAGED_ANVIL) return 0;
        return -1;
    }

    public static boolean canHandle(Block anvil, int level) {
        return getAnvilCraftingLevel(anvil) >= level;
    }

    public static List<Block> getAnvils() {
        return Registry.BLOCK
                .stream()
                .filter(b -> b instanceof LeveledAnvilBlock || b == Blocks.ANVIL)
                .toList();
    }

    public static List<FormattedCharSequence> getNamesForLevel(int level) {
        MutableComponent names = getAnvils()
                .stream()
                .filter(b -> canHandle(b, level))
                .map(Block::getName)
                .reduce(
                        null,
                        (p, c) -> p == null ? c : p.append(net.minecraft.network.chat.Component.literal(", ")).append(c)
                );
        if (names == null) return List.of();
        return Minecraft.getInstance().font.split(names, 200);
    }

    public int getCraftingLevel() {
        return level;
    }
}
