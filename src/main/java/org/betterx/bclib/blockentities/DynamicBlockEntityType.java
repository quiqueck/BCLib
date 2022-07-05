package org.betterx.bclib.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

public class DynamicBlockEntityType<T extends BlockEntity> extends BlockEntityType<T> {

    private final Set<Block> validBlocks = Sets.newHashSet();
    private final BlockEntitySupplier<? extends T> factory;

    public DynamicBlockEntityType(BlockEntitySupplier<? extends T> supplier) {
        super(null, Collections.emptySet(), null);
        this.factory = supplier;
    }

    @Override
    @Nullable
    public T create(BlockPos blockPos, BlockState blockState) {
        return factory.create(blockPos, blockState);
    }

    @Override
    public boolean isValid(BlockState blockState) {
        return validBlocks.contains(blockState.getBlock());
    }

    public void registerBlock(Block block) {
        validBlocks.add(block);
    }

    @FunctionalInterface
    public interface BlockEntitySupplier<T extends BlockEntity> {
        T create(BlockPos blockPos, BlockState blockState);
    }
}
