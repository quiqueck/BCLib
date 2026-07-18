package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import org.betterx.wover.block.api.trait.BlockTraitKey;
import org.betterx.wover.block.api.trait.GenericBlockTrait;
import org.betterx.wover.block.impl.trait.BlockTraitImpl;

import net.minecraft.world.level.block.Block;

/**
 * Marker trait: a {@link net.minecraft.world.level.block.VegetationBlock} carrying this survives on
 * <em>any</em> block with a sturdy up-face, rather than only on the blocks a
 * {@link SurvivesOnBlockTrait} lists. Intended for decoration-friendly plants that should be
 * placeable almost anywhere. Honoured by {@code VegetationBlockMixin}; carries no data.
 */
public class SurvivesOnSolidTrait extends BlockTraitImpl<Block, GenericBlockTrait> implements GenericBlockTrait {
    public static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "survives_on_solid");
    public static final SurvivesOnSolidTrait DEFAULT = new SurvivesOnSolidTrait();

    private SurvivesOnSolidTrait() {
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
