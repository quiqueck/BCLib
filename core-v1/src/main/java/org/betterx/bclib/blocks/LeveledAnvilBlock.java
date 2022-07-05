package org.betterx.bclib.blocks;

import net.minecraft.world.level.material.MaterialColor;

public class LeveledAnvilBlock extends BaseAnvilBlock {
    protected final int level;

    public LeveledAnvilBlock(MaterialColor color, int level) {
        super(color);
        this.level = level;
    }

    public int getCraftingLevel() {
        return level;
    }
}
