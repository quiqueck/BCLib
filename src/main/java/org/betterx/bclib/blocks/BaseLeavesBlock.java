package org.betterx.bclib.blocks;

import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BaseLeavesBlock extends TintedParticleLeavesBlock {
    public BaseLeavesBlock(
            float particleChance,
            BlockBehaviour.Properties properties
    ) {
        super(particleChance, properties);
    }

    public BaseLeavesBlock(
            BlockBehaviour.Properties properties
    ) {
        this(0.01F, properties);
    }
}
