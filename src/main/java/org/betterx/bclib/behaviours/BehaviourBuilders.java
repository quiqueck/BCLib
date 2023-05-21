package org.betterx.bclib.behaviours;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public class BehaviourBuilders {
    public static BlockBehaviour.Properties createPlant() {
        return BlockBehaviour.Properties.of(Material.PLANT)
                                        .color(MaterialColor.PLANT)
                                        .noCollission()
                                        .instabreak();
    }

    public static BlockBehaviour.Properties createTickingPlant() {
        return createPlant().randomTicks();
    }

    public static BlockBehaviour.Properties createReplaceablePlant() {
        return BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT)
                                        .color(MaterialColor.PLANT)
                                        .noCollission()
                                        .instabreak();
    }

    public static BlockBehaviour.Properties createWaterPlant() {
        return BlockBehaviour.Properties.of(Material.WATER_PLANT)
                                        .noCollission()
                                        .instabreak();
    }

    public static BlockBehaviour.Properties createReplaceableWaterPlant() {
        return BlockBehaviour.Properties.of(Material.REPLACEABLE_WATER_PLANT)
                                        .noCollission()
                                        .instabreak();
    }

    public static BlockBehaviour.Properties createLeaves() {
        return createLeaves(MaterialColor.PLANT);
    }

    public static BlockBehaviour.Properties createLeaves(MaterialColor color) {
        return BlockBehaviour.Properties.of(Material.LEAVES)
                                        .color(color)
                                        .strength(0.2f)
                                        .randomTicks()
                                        .noOcclusion()
                                        .isValidSpawn(Blocks::ocelotOrParrot)
                                        .isSuffocating(Blocks::never)
                                        .isViewBlocking(Blocks::never)
                                        .isRedstoneConductor(Blocks::never);
    }

    public static BlockBehaviour.Properties createStone() {
        return createStone(MaterialColor.STONE);
    }

    public static BlockBehaviour.Properties createStone(MaterialColor color) {
        return BlockBehaviour.Properties.of(Material.STONE)
                                        .color(color);
    }

    public static BlockBehaviour.Properties createSign(MaterialColor color) {
        return BlockBehaviour.Properties.of(Material.WOOD)
                                        .color(color)
                                        .noCollission()
                                        .strength(1.0f);
    }

    public static BlockBehaviour.Properties createWallSign(MaterialColor color, Block dropBlock, boolean flammable) {
        return createSign(color).dropsLike(dropBlock);
    }

    public static BlockBehaviour.Properties createTrapDoor(MaterialColor color, boolean flammable) {
        final BlockBehaviour.Properties p = BlockBehaviour.Properties
                .of(Material.WOOD)
                .color(color)
                .strength(3.0F)
                .noOcclusion()
                .isValidSpawn(Blocks::never);

        return p;
    }

    public static BlockBehaviour.Properties createWallSign(MaterialColor color, Block dropBlock) {
        return createSign(color).dropsLike(dropBlock);

    }
}
