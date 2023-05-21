package org.betterx.bclib.blocks;

import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.complexmaterials.BehaviourBuilders;
import org.betterx.bclib.interfaces.RenderLayerProvider;
import org.betterx.bclib.interfaces.behaviours.BehaviourLeaves;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class SimpleLeavesBlock extends BaseBlockNotFull implements RenderLayerProvider, BehaviourLeaves {
    public SimpleLeavesBlock(MapColor color) {
        this(
                BehaviourBuilders
                        .createLeaves(color, true)
                        .sound(SoundType.GRASS)
        );
    }

    public SimpleLeavesBlock(MapColor color, int light) {
        this(
                BehaviourBuilders
                        .createLeaves(color, true)
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