package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.BehaviourHelper;
import org.betterx.bclib.interfaces.tools.AddMineableAxe;
import org.betterx.bclib.interfaces.tools.AddMineablePickaxe;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;

public abstract class BaseRotatedPillarBlock extends RotatedPillarBlock implements DropSelfLootProvider<BaseRotatedPillarBlock> {
    protected BaseRotatedPillarBlock(Properties settings) {
        super(settings);
    }

    protected BaseRotatedPillarBlock(Block block) {
        this(Properties.ofFullCopy(block));
    }

    public static class Wood extends BaseRotatedPillarBlock implements AddMineableAxe {
        protected final boolean flammable;

        public Wood(Properties settings, boolean flammable) {
            super(flammable ? settings.ignitedByLava() : settings);
            this.flammable = flammable;
        }

        public Wood(Block block, boolean flammable) {
            this(Properties.ofFullCopy(block), flammable);
        }
    }

    public static class Stone extends BaseRotatedPillarBlock implements AddMineablePickaxe {
        public Stone(Properties settings) {
            super(settings);
        }

        public Stone(Block block) {
            super(block);
        }
    }

    public static class Metal extends BaseRotatedPillarBlock implements AddMineablePickaxe {
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
