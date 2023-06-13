package org.betterx.bclib.interfaces;

import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface SurvivesOn {
    boolean isSurvivable(BlockState state);
}
