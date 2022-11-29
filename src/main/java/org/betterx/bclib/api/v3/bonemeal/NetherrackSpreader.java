package org.betterx.bclib.api.v3.bonemeal;

import org.betterx.bclib.api.v3.tag.BCLBlockTags;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class NetherrackSpreader extends TaggedBonemealBlockSpreader {
    public static final NetherrackSpreader INSTANCE = new NetherrackSpreader();

    protected NetherrackSpreader() {
        super(BCLBlockTags.BONEMEAL_SOURCE_NETHERRACK);
    }

    protected boolean hasCustomBehaviour(BlockState state) {
        return !state.is(Blocks.WARPED_NYLIUM) && !state.is(Blocks.CRIMSON_NYLIUM);
    }
}
