package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import org.betterx.wover.block.api.BlockDefinition;
import org.betterx.wover.block.api.client.trait.BlockModelTrait;
import org.betterx.wover.block.api.client.trait.ClientBlockTraits;
import org.betterx.wover.block.api.trait.*;
import org.betterx.wover.block.impl.trait.BlockTraitImpl;
import org.betterx.wover.core.api.ModCore;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;

public class WaterSeedBlockTrait extends BlockTraitImpl<Block, GenericBlockTrait> {
    private static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "water_seed");

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
                ModCore.isDatagen() ? ClientModel.build(lightLevel) : null,
                CompostableBlockTrait.withDefault(),
                BlockTraits.FLAMMABLE.withDefault()
        ).combine();
    }

    /**
     * Kept in a separate class file (not just an @Environment(CLIENT)-guarded expression):
     * merely creating this lambda - even without ever invoking it - requires resolving the
     * client-only WoverBlockModelGenerators parameter type at the invokedynamic bootstrap site,
     * which throws immediately on a dedicated server. Gating with ModCore.isDatagen() keeps that
     * bootstrap instruction from ever executing there. See PathBlockTrait for the same pattern.
     */
    @Environment(EnvType.CLIENT)
    private static class ClientModel {
        private static BlockModelTrait build(int lightLevel) {
            return ClientBlockTraits.MODEL.with(
                    ((key, block, generator) -> {
                        generator.vanillaGenerator.createCrossBlock(
                                block,
                                lightLevel > 0
                                        ? BlockModelGenerators.PlantType.EMISSIVE_NOT_TINTED
                                        : BlockModelGenerators.PlantType.NOT_TINTED
                        );
                        generator.delegateItemModel(block);
                    })
            );
        }
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
