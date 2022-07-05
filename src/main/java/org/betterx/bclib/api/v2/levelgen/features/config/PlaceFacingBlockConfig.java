package org.betterx.bclib.api.v2.levelgen.features.config;

import net.minecraft.core.Direction;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import java.util.List;


/**
 * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.config.PlaceFacingBlockConfig} instead
 */
@Deprecated(forRemoval = true)
public class PlaceFacingBlockConfig extends org.betterx.bclib.api.v3.levelgen.features.config.PlaceFacingBlockConfig {

    public PlaceFacingBlockConfig(Block block, List<Direction> dir) {
        super(block, dir);
    }

    public PlaceFacingBlockConfig(BlockState state, List<Direction> dir) {
        super(state, dir);
    }

    public PlaceFacingBlockConfig(List<BlockState> states, List<Direction> dir) {
        super(states, dir);
    }

    public PlaceFacingBlockConfig(SimpleWeightedRandomList<BlockState> blocks, List<Direction> dir) {
        super(blocks, dir);
    }

    public PlaceFacingBlockConfig(BlockStateProvider blocks, List<Direction> dir) {
        super(blocks, dir);
    }
}
