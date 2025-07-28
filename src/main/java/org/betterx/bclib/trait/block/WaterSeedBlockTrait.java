package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import org.betterx.wover.block.api.BlockDefinition;
import org.betterx.wover.block.api.client.trait.ClientBlockTraits;
import org.betterx.wover.block.api.trait.*;
import org.betterx.wover.block.impl.trait.BlockTraitImpl;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

import java.util.List;

public class WaterSeedBlockTrait extends BlockTraitImpl<Block, GenericBlockTrait> {
    private static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "sapling");

    public static List<BlockTrait<?, ?>> withDefault() {
        return withLight(0);
    }

    public static List<BlockTrait<?, ?>> withLight(int lightLevel) {
        return withColor(MapColor.WATER, lightLevel);
    }

    public static List<BlockTrait<?, ?>> withColor(MapColor color) {
        return withColor(color, 0);
    }

    public static List<BlockTrait<?, ?>> withColor(MapColor color, int lightLevel) {
        return Combiner.of(
                WaterPlantBlockTrait.withColor(color),
                new WaterSeedBlockTrait(lightLevel),
                BlockTraits.MINEABLE_WITH.needsHoe(),
                BlockTraits.LOOT_TABLE.dropSelf(),
                ClientBlockTraits.RENDER_LAYER.cutout(),
                ClientBlockTraits.MODEL.with(
                        ((key, block, generator) -> {
                            generator.vanillaGenerator.createCrossBlock(
                                    block,
                                    lightLevel > 0
                                            ? BlockModelGenerators.PlantType.EMISSIVE_NOT_TINTED
                                            : BlockModelGenerators.PlantType.NOT_TINTED
                            );
                            generator.delegateItemModel(block);
                        })
                ),
                CompostableBlockTrait.withDefault(),
                BlockTraits.FLAMMABLE.withDefault()
        ).combine();
    }

    public final int lightLevel;

    private WaterSeedBlockTrait(int lightLevel) {
        this.lightLevel = lightLevel;
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
            definition.lightLevel(state -> lightLevel);
        }
    }
}
