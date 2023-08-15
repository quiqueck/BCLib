package org.betterx.bclib.behaviours;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class BehaviourBuilders {
    public static BlockBehaviour.Properties createPlant() {
        return createPlant(MapColor.PLANT);
    }

    public static BlockBehaviour.Properties createPlant(MapColor color) {
        return createWalkablePlant(color).noCollission();
    }

    public static BlockBehaviour.Properties createWalkablePlant() {
        return createWalkablePlant(MapColor.PLANT);
    }

    public static BlockBehaviour.Properties createWalkablePlant(MapColor color) {
        return BlockBehaviour.Properties
                .of()
                .mapColor(color)
                .noOcclusion()
                .instabreak()
                .sound(SoundType.GRASS)
                .pushReaction(PushReaction.DESTROY);
    }

    public static BlockBehaviour.Properties createVine() {
        return createVine(MapColor.PLANT);
    }

    public static Item.Properties createDisc() {
        return new Item.Properties().stacksTo(1).rarity(Rarity.RARE);
    }

    public static BlockBehaviour.Properties createStaticVine(MapColor color) {
        return createPlant(color)
                .replaceable()
                .noCollission()
                .strength(0.2f)
                .sound(SoundType.VINE);
    }
    public static BlockBehaviour.Properties createVine(MapColor color) {
        return createStaticVine(color)
                .randomTicks();
    }

    public static BlockBehaviour.Properties createGrass(MapColor color) {
        return createPlant(color)
                .noCollission()
                .noOcclusion()
                .offsetType(BlockBehaviour.OffsetType.XZ)
                .sound(SoundType.GRASS);
    }

    public static BlockBehaviour.Properties createSeed(MapColor color) {
        return createPlant(color)
                .noCollission()
                .randomTicks()
                .sound(SoundType.HARD_CROP)
                .offsetType(BlockBehaviour.OffsetType.XZ);
    }

    public static BlockBehaviour.Properties createPlantCover(MapColor color) {
        return createPlant(color).forceSolidOn()
                                 .noCollission()
                                 .replaceable()
                                 .strength(0.2f)
                                 .sound(SoundType.GLOW_LICHEN);
    }

    public static BlockBehaviour.Properties createWaterPlant() {
        return createWaterPlant(MapColor.WATER);
    }

    public static BlockBehaviour.Properties createWaterPlant(MapColor color) {
        return BlockBehaviour.Properties.of()
                                        .mapColor(color)
                                        .instabreak()
                                        .noOcclusion()
                                        .noCollission()
                                        .sound(SoundType.WET_GRASS)
                                        .offsetType(BlockBehaviour.OffsetType.XZ)
                                        .pushReaction(PushReaction.DESTROY);

    }

    public static BlockBehaviour.Properties createReplaceableWaterPlant() {
        return createWaterPlant().replaceable();
    }

    public static BlockBehaviour.Properties createLeaves() {
        return createLeaves(MapColor.PLANT, true);
    }
    public static BlockBehaviour.Properties createStaticLeaves() {
        return createStaticLeaves(MapColor.PLANT, true);
    }

    public static BlockBehaviour.Properties createStaticLeaves(MapColor color, boolean flammable) {
        final BlockBehaviour.Properties p = BlockBehaviour.Properties
                .of()
                .mapColor(color)
                .strength(0.2f)
                .noOcclusion()
                .isValidSpawn(Blocks::ocelotOrParrot)
                .isSuffocating(Blocks::never)
                .isViewBlocking(Blocks::never)
                .pushReaction(PushReaction.DESTROY)
                .isRedstoneConductor(Blocks::never)
                .sound(SoundType.GRASS);
        if (flammable) {
            p.ignitedByLava();
        }
        return p;
    }

    public static BlockBehaviour.Properties createLeaves(MapColor color, boolean flammable) {
        return createStaticLeaves(color, flammable).randomTicks();
    }

    public static BlockBehaviour.Properties createCactus(MapColor color, boolean flammable) {
        final BlockBehaviour.Properties p = BlockBehaviour.Properties
                .of()
                .mapColor(color)
                .randomTicks()
                .strength(0.4F)
                .sound(SoundType.WOOL)
                .pushReaction(PushReaction.DESTROY)
                .noOcclusion();
        if (flammable) {
            p.ignitedByLava();
        }
        return p;
    }

    public static BlockBehaviour.Properties createMetal() {
        return createMetal(MapColor.METAL);
    }

    public static BlockBehaviour.Properties createMetal(MapColor color) {
        return BlockBehaviour.Properties.of()
                                        .mapColor(color)
                                        .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                                        .strength(5.0F, 6.0F)
                                        .sound(SoundType.METAL);
    }

    public static BlockBehaviour.Properties createStone() {
        return createStone(MapColor.STONE);
    }

    public static BlockBehaviour.Properties createStone(MapColor color) {
        return BlockBehaviour.Properties.of()
                                        .mapColor(color)
                                        .strength(1.5F, 6.0F)
                                        .instrument(NoteBlockInstrument.BASEDRUM);
    }

    public static BlockBehaviour.Properties createWood() {
        return createWood(MapColor.WOOD, true);
    }

    public static BlockBehaviour.Properties createWood(MapColor color, boolean flammable) {
        final BlockBehaviour.Properties p = BlockBehaviour.Properties
                .of()
                .mapColor(color)
                .instrument(NoteBlockInstrument.BASS)
                .strength(2.0F)
                .sound(SoundType.WOOD);


        if (flammable) {
            p.ignitedByLava();
        }
        return p;
    }

    public static BlockBehaviour.Properties createSign(MapColor color, boolean flammable) {
        final BlockBehaviour.Properties p = BlockBehaviour.Properties
                .of()
                .mapColor(color)
                .forceSolidOn()
                .instrument(NoteBlockInstrument.BASS)
                .noCollission()
                .strength(1.0f);
        if (flammable) {
            p.ignitedByLava();
        }
        return p;
    }

    public static BlockBehaviour.Properties createWallSign(MapColor color, Block dropBlock, boolean flammable) {
        return createSign(color, flammable).dropsLike(dropBlock);
    }

    public static BlockBehaviour.Properties createTrapDoor(MapColor color, boolean flammable) {
        final BlockBehaviour.Properties p = BlockBehaviour.Properties
                .of()
                .mapColor(color)
                .instrument(NoteBlockInstrument.BASS)
                .strength(3.0F)
                .noOcclusion()
                .isValidSpawn(Blocks::never);
        if (flammable) {
            p.ignitedByLava();
        }
        return p;
    }

    public static BlockBehaviour.Properties createGlass() {
        return BlockBehaviour.Properties
                .of()
                .instrument(NoteBlockInstrument.HAT)
                .strength(0.3F)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .isValidSpawn(Blocks::never)
                .isRedstoneConductor(Blocks::never)
                .isSuffocating(Blocks::never)
                .isViewBlocking(Blocks::never);
    }

    public static BlockBehaviour.Properties createSnow() {
        return BlockBehaviour.Properties
                .of()
                .mapColor(MapColor.SNOW)
                .requiresCorrectToolForDrops()
                .strength(0.2F)
                .sound(SoundType.SNOW);
    }
}
