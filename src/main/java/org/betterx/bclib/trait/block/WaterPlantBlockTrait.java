package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import org.betterx.wover.block.api.BlockDefinition;
import org.betterx.wover.block.api.trait.BlockTraitKey;
import org.betterx.wover.block.api.trait.GenericBlockTrait;
import org.betterx.wover.block.impl.trait.BlockTraitImpl;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class WaterPlantBlockTrait extends BlockTraitImpl<Block, GenericBlockTrait> {
    public static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "water_plant");


    public static WaterPlantBlockTrait withDefault() {
        return new WaterPlantBlockTrait(MapColor.WATER, false);
    }

    public static WaterPlantBlockTrait withColor(MapColor color) {
        return new WaterPlantBlockTrait(color, false);
    }

    public static WaterPlantBlockTrait withColor(MapColor color, boolean walkable) {
        return new WaterPlantBlockTrait(color, walkable);
    }

    public final MapColor color;
    public final boolean walkable;

    private WaterPlantBlockTrait(MapColor color, boolean walkable) {
        this.color = color;
        this.walkable = walkable;
    }

    @Override
    public BlockTraitKey key() {
        return KEY;
    }

    @Override
    public void configure(BlockDefinition<Block, ? extends BlockDefinition<Block, ?>> definition) {
        super.configure(definition);

        definition
                .mapColor(color)
                .instabreak()
                .noOcclusion()
                .sound(SoundType.WET_GRASS)
                .offsetType(BlockBehaviour.OffsetType.XZ)
                .pushReaction(PushReaction.DESTROY);

        if (!walkable) {
            definition.noCollission();
        }
    }
}