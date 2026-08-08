package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import de.ambertation.wover.block.api.BlockDefinition;
import de.ambertation.wover.block.api.trait.BlockTraitKey;
import de.ambertation.wover.block.impl.trait.BlockTraitImpl;

import net.minecraft.world.level.block.Block;

/**
 * Adds {@code randomTicks()} and nothing else. Block classes whose growth/behaviour logic lives in
 * {@code randomTick(...)} must never flip on random ticking themselves by mutating the {@code Properties}
 * passed into their constructor - that property has to come from a trait, applied by the registration site,
 * so it stays visible and overridable there. This is the shared trait for exactly that: attach it wherever a
 * block needs random ticking but isn't already covered by a more specific trait (e.g. {@code VineBlockTrait}
 * or {@code WaterSeedBlockTrait}, which already call {@code definition.randomTicks()} themselves).
 */
public class RandomTicksTrait extends BlockTraitImpl.Generic {
    private static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "random_ticks");
    private static final RandomTicksTrait DEFAULT = new RandomTicksTrait();

    public static RandomTicksTrait withDefault() {
        return DEFAULT;
    }

    private RandomTicksTrait() {}

    @Override
    public BlockTraitKey key() {
        return KEY;
    }

    @Override
    public void configure(BlockDefinition<Block, ? extends BlockDefinition<Block, ?>> definition) {
        super.configure(definition);
        definition.randomTicks();
    }
}
