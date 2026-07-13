package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.BehaviourBuilders;
import org.betterx.wover.block.api.BlockProperties.TripleShape;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Function;

@SuppressWarnings("deprecation")
public class BaseVineBlock extends AbstractVineBlock {
    public static final EnumProperty<TripleShape> SHAPE = BlockProperties.TRIPLE_SHAPE;

    public BaseVineBlock(BlockBehaviour.Properties props) {
        this(props, 0, false);
    }

    public BaseVineBlock(BlockBehaviour.Properties props, int light) {
        this(props, light, false);
    }

    public BaseVineBlock(BlockBehaviour.Properties props, int light, boolean onlyBottomIsLit) {
        this(props, light, onlyBottomIsLit, p -> p);
    }

    public BaseVineBlock(
            BlockBehaviour.Properties props,
            int light,
            boolean onlyBottomIsLit,
            Function<Properties, Properties> propMod
    ) {
        this(
                propMod.apply(BehaviourBuilders
                        .createPlant(props, MapColor.PLANT)
                        .sound(SoundType.GRASS)
                        .lightLevel((state) -> onlyBottomIsLit
                                ? state.getValue(SHAPE) == TripleShape.BOTTOM
                                ? light
                                : 0
                                : light)
                        .offsetType(BlockBehaviour.OffsetType.XZ)),
                32,
                0
        );
    }

    public BaseVineBlock(BlockBehaviour.Properties properties, int maxGrowLength, int spaceBeneath) {
        this(properties, maxGrowLength, spaceBeneath, 0);
    }

    private BaseVineBlock(BlockBehaviour.Properties properties, int maxGrowLength, int spaceBeneath, int growChance) {
        super(properties, maxGrowLength, spaceBeneath, growChance);
        this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, TripleShape.BOTTOM));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager) {
        stateManager.add(SHAPE);
    }

    protected BlockState makeBottomState(BlockState state) {
        return state.setValue(SHAPE, TripleShape.BOTTOM);
    }

    protected BlockState makeMiddleState(BlockState state) {
        return state.setValue(SHAPE, TripleShape.MIDDLE);
    }

    protected BlockState makeTopState(BlockState state) {
        return state.setValue(SHAPE, TripleShape.TOP);
    }

    public static class Growing extends BaseVineBlock {
        public Growing(
                Properties properties,
                int maxGrowLength,
                int spaceBeneath,
                int growChance
        ) {
            super(properties.randomTicks(), maxGrowLength, spaceBeneath, growChance);
        }


        @Override
        public boolean isRandomlyTicking(BlockState state) {
            return state.getValue(SHAPE) == TripleShape.BOTTOM;
        }
    }
}
