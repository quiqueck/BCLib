package org.betterx.bclib.api.v2.levelgen.features.config;

import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import java.util.List;

@Deprecated(forRemoval = true)
public abstract class PlaceBlockFeatureConfig extends org.betterx.bclib.api.v3.levelgen.features.config.PlaceBlockFeatureConfig {

    public PlaceBlockFeatureConfig(Block block) {
        super(block);
    }

    public PlaceBlockFeatureConfig(BlockState state) {
        super(state);
    }

    public PlaceBlockFeatureConfig(List<BlockState> states) {
        super(states);
    }

    public PlaceBlockFeatureConfig(SimpleWeightedRandomList<BlockState> blocks) {
        super(blocks);
    }

    public PlaceBlockFeatureConfig(BlockStateProvider blocks) {
        super(blocks);
    }
}
