package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import de.ambertation.wover.block.api.BlockDefinition;
import de.ambertation.wover.block.api.trait.BlockTraitKey;
import de.ambertation.wover.block.api.trait.GenericBlockTrait;
import de.ambertation.wover.block.impl.trait.BlockTraitImpl;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class WaterPlantBlockTrait extends BlockTraitImpl<Block, GenericBlockTrait> implements GenericBlockTrait {
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

    // Runtime trait so hasRuntimeTrait(WaterPlantBlockTrait.KEY) identifies water plants/seeds for the
    // creative "Plants" tab (BehaviourPlantLike.TAB_PREDICATE), replacing the removed BehaviourWaterPlant.
    @Override
    public GenericBlockTrait forRuntime() {
        return this;
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