package org.betterx.bclib.complexmaterials;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class BehaviourBuilders {
    public static BlockBehaviour.Properties createPlant() {
        return BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.PLANT)
                                        .noCollission()
                                        .instabreak()
                                        .pushReaction(PushReaction.DESTROY);
    }

    public static BlockBehaviour.Properties createTickingPlant() {
        return createPlant().randomTicks();
    }

    public static BlockBehaviour.Properties createReplaceablePlant() {
        return createPlant().replaceable();
    }

    public static BlockBehaviour.Properties createWaterPlant() {
        return BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.WATER)
                                        .noCollission()
                                        .instabreak()
                                        .pushReaction(PushReaction.DESTROY);
    }

    public static BlockBehaviour.Properties createReplaceableWaterPlant() {
        return createWaterPlant().replaceable();
    }

    public static BlockBehaviour.Properties createLeaves() {
        return createLeaves(MapColor.PLANT);
    }

    public static BlockBehaviour.Properties createLeaves(MapColor color) {
        return BlockBehaviour.Properties.of()
                                        .mapColor(color)
                                        .strength(0.2f)
                                        .randomTicks()
                                        .noOcclusion()
                                        .isValidSpawn(Blocks::ocelotOrParrot)
                                        .isSuffocating(Blocks::never)
                                        .isViewBlocking(Blocks::never)
                                        .ignitedByLava()
                                        .pushReaction(PushReaction.DESTROY)
                                        .isRedstoneConductor(Blocks::never);
    }

    public static BlockBehaviour.Properties createStone() {
        return createStone(MapColor.STONE);
    }

    public static BlockBehaviour.Properties createStone(MapColor color) {
        return BlockBehaviour.Properties.of()
                                        .mapColor(color)
                                        .instrument(NoteBlockInstrument.BASEDRUM);
    }

    public static BlockBehaviour.Properties createSign(MapColor color) {
        return BlockBehaviour.Properties.of()
                                        .mapColor(color)
                                        .forceSolidOn()
                                        .instrument(NoteBlockInstrument.BASS)
                                        .noCollission()
                                        .strength(1.0f)
                                        .ignitedByLava();
    }

    public static BlockBehaviour.Properties createWallSign(MapColor color, Block dropBlock) {
        return createSign(color).dropsLike(dropBlock);

    }
}
