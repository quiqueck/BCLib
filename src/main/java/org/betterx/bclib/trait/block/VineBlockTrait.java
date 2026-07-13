package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import org.betterx.wover.block.api.BlockDefinition;
import org.betterx.wover.block.api.BlockProperties;
import org.betterx.wover.block.api.client.trait.ClientBlockTraits;
import org.betterx.wover.block.api.trait.*;
import org.betterx.wover.block.impl.trait.BlockTraitImpl;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

import java.util.List;

public class VineBlockTrait extends BlockTraitImpl<Block, GenericBlockTrait> {
    private static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "vine");

    public static List<BlockTrait<?, ?>> withDefault() {
        return withLight(0);

    }

    public static List<BlockTrait<?, ?>> withLight(int lightLevel) {
        return withColor(MapColor.PLANT, lightLevel, false);
    }

    public static List<BlockTrait<?, ?>> withLight(int lightLevel, boolean onlyBottomIsLit) {
        return withColor(MapColor.PLANT, lightLevel, false);
    }

    public static List<BlockTrait<?, ?>> withColor(MapColor color) {
        return withColor(color, 0, false);
    }

    public static List<BlockTrait<?, ?>> withColor(MapColor color, int lightLevel) {
        return withColor(color, lightLevel, false);
    }

    public static List<BlockTrait<?, ?>> withColor(MapColor color, int lightLevel, boolean onlyBottomIsLit) {
        return Combiner.of(
                PlantBlockTrait.withColor(color, false),
                new VineBlockTrait(lightLevel, onlyBottomIsLit),
                BlockTraits.MINEABLE_WITH.needsHoe(),
                BlockTraits.MINEABLE_WITH.needsShears(),
                BlockTraits.CLIMBABLE.withDefault(),
                BlockTraits.LOOT_TABLE.dropWithSilktouchOrHoeOrShears(),
                ClientBlockTraits.RENDER_LAYER.cutout(),
                ClientBlockTraits.MODEL.with(
                        ((key, block, generator) -> {
                            generator.createCubeModel(block);
                            generator.createFlatItem(block);
                        })
                ),
                CompostableBlockTrait.withDefault(),
                BlockTraits.FLAMMABLE.withDefault()
        ).combine();
    }

    public final int lightLevel;
    public final boolean onlyBottomIsLit;

    private VineBlockTrait(int lightLevel, boolean onlyBottomIsLit) {
        this.lightLevel = lightLevel;
        this.onlyBottomIsLit = onlyBottomIsLit;
    }

    @Override
    public BlockTraitKey key() {
        return KEY;
    }

    @Override
    public void configure(BlockDefinition<Block, ? extends BlockDefinition<Block, ?>> definition) {
        super.configure(definition);

        definition.randomTicks();

        if (lightLevel > 0) {
            definition.lightLevel((state) ->
                    onlyBottomIsLit
                            ? (
                            state.getValue(BlockProperties.TRIPLE_SHAPE) == BlockProperties.TripleShape.BOTTOM
                                    ? lightLevel
                                    : 0)
                            : lightLevel);
        }
    }
}