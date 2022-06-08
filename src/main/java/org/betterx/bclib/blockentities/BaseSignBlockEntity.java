package org.betterx.bclib.blockentities;

import org.betterx.bclib.registry.BaseBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BaseSignBlockEntity extends SignBlockEntity {
    public BaseSignBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    @Override
    public BlockEntityType<?> getType() {
        return BaseBlockEntities.SIGN;
    }
}