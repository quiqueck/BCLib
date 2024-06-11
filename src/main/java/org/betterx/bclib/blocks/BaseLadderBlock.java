package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.BehaviourHelper;
import org.betterx.bclib.behaviours.interfaces.BehaviourClimable;
import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.interfaces.RenderLayerProvider;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public abstract class BaseLadderBlock extends LadderBlock implements RenderLayerProvider, BehaviourClimable, DropSelfLootProvider<BaseLadderBlock>, BlockModelProvider {
    protected BaseLadderBlock(Block block) {
        this(Properties.ofFullCopy(block).noOcclusion());
    }

    public BaseLadderBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BCLRenderLayer getRenderLayer() {
        return BCLRenderLayer.CUTOUT;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void provideBlockModels(WoverBlockModelGenerators generators) {
        generators.createLadder(this);
    }

    public static class Wood extends BaseLadderBlock implements BehaviourWood {
        public Wood(Block block) {
            super(block);
        }

        public Wood(Properties properties) {
            super(properties);
        }
    }

    public static class Metal extends BaseLadderBlock implements BehaviourMetal {
        public Metal(Block block) {
            super(block);
        }

        public Metal(Properties properties) {
            super(properties);
        }
    }

    public static BaseLadderBlock from(Block source) {
        return BehaviourHelper.from(source,
                Wood::new, null, Metal::new
        );
    }
}
