package org.betterx.bclib.furniture.block;

import org.betterx.bclib.behaviours.BehaviourHelper;
import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.client.models.BCLModels;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
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

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        return SHAPE;
    }

    public static class Wood extends BaseTaburet implements BehaviourWood {
        public Wood(Block block) {
            super(block);
        }
    }

    public static class Stone extends BaseTaburet implements BehaviourStone {
        public Stone(Block block) {
            super(block);
        }
    }

    public static class Metal extends BaseTaburet implements BehaviourMetal {
        public Metal(Block block) {
            super(block);
        }
    }

    public static BaseTaburet from(Block source) {
        return BehaviourHelper.from(source, Wood::new, Stone::new, Metal::new);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void provideBlockModels(WoverBlockModelGenerators generators) {
        BCLModels.createTaburetBlockModel(generators, this, this.baseMaterial);
    }
}
