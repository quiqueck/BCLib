package org.betterx.bclib.furniture.block;

import org.betterx.bclib.client.models.BCLModels;
import de.ambertation.wover.block.api.model.WoverBlockModelGenerators;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.NotNull;

public abstract class BaseTaburet extends AbstractChair {
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 10, 14);

    public BaseTaburet(Block block) {
        super(block, 9);
    }

    public BaseTaburet(Block block, BlockBehaviour.Properties settings) {
        super(block, settings, 9);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        return SHAPE;
    }

    public static class Wood extends BaseTaburet {
        public Wood(Block block) {
            super(block);
        }

        public Wood(Block block, BlockBehaviour.Properties settings) {
            super(block, settings);
        }
    }

    public static class Stone extends BaseTaburet {
        public Stone(Block block) {
            super(block);
        }

        public Stone(Block block, BlockBehaviour.Properties settings) {
            super(block, settings);
        }
    }

    public static class Metal extends BaseTaburet {
        public Metal(Block block) {
            super(block);
        }

        public Metal(Block block, BlockBehaviour.Properties settings) {
            super(block, settings);
        }
    }

    /**
     * Generates the block model for a taburet, using {@code baseMaterial}'s texture.
     *
     * @param generator The generator helper to emit the blockstate/model through
     * @param taburetBlock The taburet block to generate the model for
     */
    @Environment(EnvType.CLIENT)
    public static void provideBlockModel(WoverBlockModelGenerators generator, BaseTaburet taburetBlock) {
        BCLModels.createTaburetBlockModel(generator, taburetBlock, taburetBlock.baseMaterial);
    }
}
