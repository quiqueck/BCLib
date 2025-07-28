package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import org.betterx.wover.block.api.BlockDefinition;
import org.betterx.wover.block.api.client.trait.ClientBlockTraits;
import org.betterx.wover.block.api.trait.*;
import org.betterx.wover.block.impl.trait.BlockTraitImpl;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.List;

public class PlantBlockTrait extends BlockTraitImpl<Block, GenericBlockTrait> {
    public static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "plant");


    public static PlantBlockTrait withDefault() {
        return new PlantBlockTrait(MapColor.PLANT, false);
    }

    public static PlantBlockTrait withColor(MapColor color) {
        return new PlantBlockTrait(color, false);
    }

    public static PlantBlockTrait withColor(MapColor color, boolean walkable) {
        return new PlantBlockTrait(color, walkable);
    }

    public static List<BlockTrait<?, ?>> compostableWithColor(MapColor color, boolean walkable, boolean flammable) {
        return Combiner.of(
                withColor(color, walkable),
                CompostableBlockTrait.withDefault(),
                flammable ? BlockTraits.FLAMMABLE.withDefault() : null,
                ClientBlockTraits.RENDER_LAYER.cutout(),
                BlockTraits.LOOT_TABLE.dropSelf()
        ).combine();
    }

    public final MapColor color;
    public final boolean walkable;

    private PlantBlockTrait(MapColor color, boolean walkable) {
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

        definition.mapColor(color)
                  .noOcclusion()
                  .instabreak()
                  .sound(SoundType.GRASS)
                  .pushReaction(PushReaction.DESTROY);

        if (!walkable) {
            definition.noCollission();
        }
    }
}
