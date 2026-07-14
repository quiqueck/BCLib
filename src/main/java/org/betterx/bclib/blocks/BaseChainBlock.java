package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.interfaces.RenderLayerProvider;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * The block model is no longer provided implicitly - register {@code ModelTraitLibrary.chain()} (or an
 * equivalent {@code ClientBlockTraits.MODEL} trait) at the registration site of any block that needs one.
 */
public abstract class BaseChainBlock extends ChainBlock implements RenderLayerProvider, DropSelfLootProvider<BaseChainBlock> {
    public BaseChainBlock(MapColor color) {
        this(Properties.ofFullCopy(Blocks.CHAIN).mapColor(color));
    }

    public BaseChainBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BCLRenderLayer getRenderLayer() {
        return BCLRenderLayer.CUTOUT;
    }

    public static class Metal extends BaseChainBlock implements BehaviourMetal {

        public Metal(MapColor color) {
            super(color);
        }

        public Metal(Properties properties) {
            super(properties);
        }
    }
}
