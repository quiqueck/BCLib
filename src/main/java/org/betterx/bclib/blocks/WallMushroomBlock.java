package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.BehaviourBuilders;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

import com.google.common.collect.Lists;

import java.util.List;

public abstract class WallMushroomBlock extends BaseWallPlantBlock {
    public WallMushroomBlock(int light) {
        super(BehaviourBuilders.createPlant()
                               .destroyTime(0.2F)
                               .lightLevel(state -> light)
                               .sound(SoundType.WOOD)
        );
    }

    protected WallMushroomBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return Lists.newArrayList(new ItemStack(this));
    }

    @Override
    public boolean isSupport(LevelReader world, BlockPos pos, BlockState blockState, Direction direction) {
        return blockState.isSolid() && blockState.isFaceSturdy(world, pos, direction);
    }
}
