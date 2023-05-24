package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public class BaseBlockNotFull extends BaseBlock {
    public BaseBlockNotFull(Properties settings) {
        super(settings);
    }

    public boolean canSuffocate(BlockState state, BlockGetter view, BlockPos pos) {
        return false;
    }

    public boolean isSimpleFullBlock(BlockState state, BlockGetter view, BlockPos pos) {
        return false;
    }

    public boolean allowsSpawning(BlockState state, BlockGetter view, BlockPos pos, EntityType<?> type) {
        return false;
    }

    public static class Wood extends BaseBlockNotFull implements BehaviourWood {
        public Wood(Properties settings) {
            super(settings);
        }
    }

    public static class Stone extends BaseBlockNotFull implements BehaviourStone {
        public Stone(Properties settings) {
            super(settings);
        }
    }

    public static class Metal extends BaseBlockNotFull implements BehaviourMetal {
        public Metal(Properties settings) {
            super(settings);
        }
    }
}
