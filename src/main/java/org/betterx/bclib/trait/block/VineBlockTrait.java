package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import org.betterx.wover.block.api.BlockDefinition;
import org.betterx.wover.block.api.BlockProperties;
import org.betterx.wover.block.api.client.trait.BlockModelTrait;
import org.betterx.wover.block.api.client.trait.ClientBlockTraits;
import org.betterx.wover.block.api.trait.*;
import org.betterx.wover.block.impl.trait.BlockTraitImpl;
import org.betterx.wover.core.api.ModCore;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

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
        return withColor(color, lightLevel, onlyBottomIsLit, true);
    }

    /**
     * @param generateModel when {@code false}, the default cube block/item model is <em>not</em> attached, so
     *                      the block can supply its own model (e.g. a hand-authored hanging-vine model routed
     *                      through {@code ModelTraitLibrary.externalModel()}). Every other overload defaults
     *                      this to {@code true} to preserve the vanilla-style cube model for simple vines.
     */
    public static List<BlockTrait<?, ?>> withColor(
            MapColor color,
            int lightLevel,
            boolean onlyBottomIsLit,
            boolean generateModel
    ) {
        return Combiner.of(
                // Vines hang against/along blocks and must stay grid-aligned, so opt out of the
                // plant X-Z offset that PlantBlockTrait now enables for standalone ground plants.
                PlantBlockTrait.withColor(color, false, BlockBehaviour.OffsetType.NONE),
                new VineBlockTrait(lightLevel, onlyBottomIsLit),
                BlockTraits.MINEABLE_WITH.needsHoe(),
                BlockTraits.MINEABLE_WITH.needsShears(),
                BlockTraits.CLIMBABLE.withDefault(),
                VegetationTagTrait.vine(),
                BlockTraits.LOOT_TABLE.dropWithSilktouchOrHoeOrShears(),
                ClientBlockTraits.RENDER_LAYER.cutout(),
                generateModel && ModCore.isDatagen() ? ClientModel.build() : null,
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
        private static BlockModelTrait build() {
            return ClientBlockTraits.MODEL.with(
                    ((key, block, generator) -> {
                        generator.createCubeModel(block);
                        generator.createFlatItem(block);
                    })
            );
        }
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