package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import org.betterx.wover.block.api.trait.BlockTraitKey;
import org.betterx.wover.block.api.trait.GenericBlockTrait;
import org.betterx.wover.block.impl.trait.BlockTraitImpl;

import net.minecraft.world.level.block.Block;

public class CompostableBlockTrait extends BlockTraitImpl<Block, GenericBlockTrait> implements GenericBlockTrait {
    public static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "compostable");
    private static final CompostableBlockTrait DEFAULT = new CompostableBlockTrait(0.1f);

    public static CompostableBlockTrait withDefault() {
        return DEFAULT;
    }

    public static CompostableBlockTrait withChance(float chance) {
        return new CompostableBlockTrait(chance);
    }

    public final float compostingChance;

    private CompostableBlockTrait(float compostingChance) {
        this.compostingChance = compostingChance;
    }

    @Override
    public BlockTraitKey key() {
        return KEY;
    }

    @Override
    public GenericBlockTrait forRuntime() {
        return this;
    }
}
