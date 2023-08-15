package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.BehaviourBuilders;
import org.betterx.bclib.behaviours.interfaces.BehaviourLeaves;
import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.interfaces.RenderLayerProvider;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class SimpleLeavesBlock extends BaseBlockNotFull implements RenderLayerProvider, BehaviourLeaves {
    public SimpleLeavesBlock(MapColor color) {
        this(
                BehaviourBuilders
                        .createStaticLeaves(color, true)
                        .sound(SoundType.GRASS)
        );
    }

    public SimpleLeavesBlock(MapColor color, int light) {
        this(
                BehaviourBuilders
                        .createStaticLeaves(color, true)
                        .lightLevel(ignored -> light)
                        .sound(SoundType.GRASS)
        );
    }

    public SimpleLeavesBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BCLRenderLayer getRenderLayer() {
        return BCLRenderLayer.CUTOUT;
    }


    @Override
    public float compostingChance() {
        return 0.3f;
    }
}