package org.betterx.bclib.interfaces;

import net.minecraft.world.level.block.state.BlockBehaviour;

@FunctionalInterface
public interface SettingsExtender {
    BlockBehaviour.Properties amend(BlockBehaviour.Properties props);
}
