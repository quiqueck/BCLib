package org.betterx.bclib.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;

import java.util.List;

public class LeveledAnvilBlock extends BaseAnvilBlock {
    protected final int level;

    public LeveledAnvilBlock(MapColor color, int level) {
        super(color);
        this.level = level;
    }

    public static int getAnvilCraftingLevel(Block anvil) {
        if (anvil instanceof LeveledAnvilBlock l) return l.getCraftingLevel();
        if (anvil == Blocks.ANVIL || anvil == Blocks.CHIPPED_ANVIL || anvil == Blocks.DAMAGED_ANVIL)
            return Tiers.IRON.getLevel() - 1;
        return 0;
    }

    public static boolean canHandle(Block anvil, int level) {
        return getAnvilCraftingLevel(anvil) >= level;
    }

    public static List<Block> getAnvils() {
        return BuiltInRegistries.BLOCK
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
