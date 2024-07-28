package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.BehaviourHelper;
import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public abstract class BaseRotatedPillarBlock extends RotatedPillarBlock implements DropSelfLootProvider<BaseRotatedPillarBlock>, BlockModelProvider {
    protected BaseRotatedPillarBlock(Properties settings) {
        super(settings);
    }

    protected BaseRotatedPillarBlock(Block block) {
        this(Properties.ofFullCopy(block));
    }


    @Environment(EnvType.CLIENT)
    @Override
    public void provideBlockModels(WoverBlockModelGenerators generator) {
        generator.createRotatedPillar(this);
    }

    public static class Wood extends BaseRotatedPillarBlock implements BehaviourWood {
        protected final boolean flammable;

        public Wood(Properties settings, boolean flammable) {
            super(flammable ? settings.ignitedByLava() : settings);
            this.flammable = flammable;
        }

        public Wood(Block block, boolean flammable) {
            this(Properties.ofFullCopy(block), flammable);
        }
    }

    public static class Stone extends BaseRotatedPillarBlock implements BehaviourStone {
        public Stone(Properties settings) {
            super(settings);
        }

        public Stone(Block block) {
            super(block);
        }
    }

    public static class Metal extends BaseRotatedPillarBlock implements BehaviourMetal {
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
