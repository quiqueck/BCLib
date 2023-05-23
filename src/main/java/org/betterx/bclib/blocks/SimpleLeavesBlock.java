package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.BehaviourBuilders;
import org.betterx.bclib.behaviours.interfaces.BehaviourLeaves;
import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.interfaces.RenderLayerProvider;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

import java.util.List;

public class SimpleLeavesBlock extends BaseBlockNotFull implements RenderLayerProvider, BehaviourLeaves
{
    public SimpleLeavesBlock(MaterialColor color) {
        this(
                Properties
                        .of(Material.LEAVES)
                        .strength(0.2F)
                        .color(color)
                        .sound(SoundType.GRASS)
                        .noOcclusion()
                        .isValidSpawn((state, world, pos, type) -> false)
                        .isSuffocating((state, world, pos) -> false)
                        .isViewBlocking((state, world, pos) -> false)
        );
    }

    public SimpleLeavesBlock(MaterialColor color, int light) {
        this(
                Properties
                        .of(Material.LEAVES)
                        .lightLevel(ignored->light)
                        .color(color)
                        .strength(0.2F)
                        .sound(SoundType.GRASS)
                        .noOcclusion()
                        .isValidSpawn((state, world, pos, type) -> false)
                        .isSuffocating((state, world, pos) -> false)
                        .isViewBlocking((state, world, pos) -> false)
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