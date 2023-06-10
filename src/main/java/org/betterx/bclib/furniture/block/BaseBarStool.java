package org.betterx.bclib.furniture.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BaseBarStool extends AbstractChair {
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 16, 12);

    public BaseBarStool(Block block) {
        super(block, 15);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        return SHAPE;
    }
}