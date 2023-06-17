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

public abstract class BaseBarStool extends AbstractChair {
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 16, 12);

    public BaseBarStool(Block block) {
        super(block, 15);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        return SHAPE;
    }

    public static class Wood extends BaseBarStool implements BehaviourWood {
        public Wood(Block block) {
            super(block);
        }
    }

    public static class Stone extends BaseBarStool implements BehaviourStone {
        public Stone(Block block) {
            super(block);
        }
    }

    public static class Metal extends BaseBarStool implements BehaviourMetal {
        public Metal(Block block) {
            super(block);
        }
    }

    public static BaseBarStool from(Block source) {
        return BehaviourHelper.from(source, Wood::new, Stone::new, Metal::new);
    }
}