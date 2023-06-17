package org.betterx.bclib.furniture.block;

import org.betterx.bclib.behaviours.BehaviourHelper;
import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BaseTaburet extends AbstractChair {
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 10, 14);

    public BaseTaburet(Block block) {
        super(block, 9);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
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
}
