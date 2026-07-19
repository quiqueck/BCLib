package org.betterx.bclib.blocks;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BaseGlassBlock extends BaseBlockNotFull {
    public BaseGlassBlock(Block block) {
        this(block, 0.3f);
    }

    public BaseGlassBlock(Block block, float resistance) {
        super(Properties.ofFullCopy(block)
                        .explosionResistance(resistance)
                        .noOcclusion()
                        .isSuffocating((arg1, arg2, arg3) -> false)
                        .isViewBlocking((arg1, arg2, arg3) -> false));
    }

    /**
     * Threads an already-configured (id-bearing) {@link Properties} through to the block instead of
     * building a fresh (id-less) one from a template block.
     */
    public BaseGlassBlock(Properties settings, float resistance) {
        super(settings
                .explosionResistance(resistance)
                .noOcclusion()
                .isSuffocating((arg1, arg2, arg3) -> false)
                .isViewBlocking((arg1, arg2, arg3) -> false));
    }

    @Environment(EnvType.CLIENT)
    public float getShadeBrightness(BlockState state, BlockGetter view, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState blockState) {
        return true;
    }

    @Environment(EnvType.CLIENT)
    public boolean skipRendering(BlockState state, BlockState neighbor, Direction facing) {
        return neighbor.getBlock() == this || super.skipRendering(state, neighbor, facing);
    }
}
