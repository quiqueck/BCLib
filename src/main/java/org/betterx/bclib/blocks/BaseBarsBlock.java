package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.interfaces.RenderLayerProvider;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Collections;
import java.util.List;

/**
 * The block model is no longer provided implicitly - register {@code ModelTraitLibrary.bars()} (or an
 * equivalent {@code ClientBlockTraits.MODEL} trait) at the registration site of any block that needs one.
 */
public abstract class BaseBarsBlock extends IronBarsBlock implements RenderLayerProvider, BehaviourMetal {
    public BaseBarsBlock(Block source) {
        this(Properties.ofFullCopy(source).strength(5.0F, 6.0F).noOcclusion());
    }

    public BaseBarsBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return Collections.singletonList(new ItemStack(this));
    }

    @Environment(EnvType.CLIENT)
    public boolean skipRendering(BlockState state, BlockState stateFrom, Direction direction) {
        if (direction.getAxis().isVertical() && stateFrom.getBlock() == this && !stateFrom.equals(state)) {
            return false;
        }
        return super.skipRendering(state, stateFrom, direction);
    }

    @Override
    public BCLRenderLayer getRenderLayer() {
        return BCLRenderLayer.CUTOUT;
    }

    public static class Metal extends BaseBarsBlock implements BehaviourMetal {

        public Metal(Block source) {
            super(source);
        }

        public Metal(Properties properties) {
            super(properties);
        }
    }
}
