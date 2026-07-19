package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.BehaviourHelper;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;

/**
 * The loot table is no longer provided implicitly - register {@code BlockTraits.LOOT_TABLE.dropSelf()} at
 * the registration site of any block that needs one.
 */
public abstract class BaseRotatedPillarBlock extends RotatedPillarBlock {
    protected BaseRotatedPillarBlock(Properties settings) {
        super(settings);
    }

    protected BaseRotatedPillarBlock(Block block) {
        this(Properties.ofFullCopy(block));
    }

    public static class Wood extends BaseRotatedPillarBlock {
        protected final boolean flammable;

        public Wood(Properties settings, boolean flammable) {
            super(flammable ? settings.ignitedByLava() : settings);
            this.flammable = flammable;
        }

        public Wood(Block block, boolean flammable) {
            this(Properties.ofFullCopy(block), flammable);
        }
    }

    public static class Stone extends BaseRotatedPillarBlock {
        public Stone(Properties settings) {
            super(settings);
        }

        public Stone(Block block) {
            super(block);
        }
    }

    public static class Metal extends BaseRotatedPillarBlock {
        public Metal(Properties settings) {
            super(settings);
        }

        public Metal(Block block) {
            super(block);
        }
    }

    public static BaseRotatedPillarBlock from(Block source, boolean flammable) {
        return BehaviourHelper.from(
                source,
                (s) -> new Wood(s, flammable),
                Stone::new,
                Metal::new
        );
    }
}
