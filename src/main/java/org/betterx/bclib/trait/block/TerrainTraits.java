package org.betterx.bclib.trait.block;

import org.betterx.bclib.blocks.BaseTerrainBlock;
import org.betterx.bclib.trait.TraitLists;
import de.ambertation.wover.block.api.client.trait.BlockModelTrait;
import de.ambertation.wover.block.api.client.trait.ClientBlockTraits;
import de.ambertation.wover.block.api.trait.BlockTrait;

import net.minecraft.world.level.block.Block;
import de.ambertation.wover.block.api.trait.BlockTraits;
import de.ambertation.wover.core.api.ModCore;
import de.ambertation.wover.pottable.api.trait.PottableSoilBlockTrait;

import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;

/**
 * The composite trait bundle every {@link BaseTerrainBlock} used to receive through BetterEnd's private
 * {@code registerEndTerrain} helpers: {@link TerrainBlockTrait#DEFAULT} (end-stone strength/tags/sound),
 * {@link PottableSoilBlockTrait#DEFAULT} (pottable soil), the silk-touch terrain loot ({@link #loot()}),
 * and a top/side/bottom block model derived from the block's {@link BaseTerrainBlock#getBaseBlock() base
 * block}.
 * <p>
 * The loot and model both read {@link BaseTerrainBlock#getBaseBlock()} off the concrete block instance, so a
 * subclass with a custom base (e.g. a pallidium block) is honoured automatically - no per-call parameter is
 * needed. {@code mapColor} and any {@code addTags(...)} stay at the registration site because they are plain
 * definition setters, not traits.
 * <p>
 * Phase 5 replaces every {@code registerEndTerrain} call site with a definition chain of the form
 * {@code defineBlock(name, factory).mapColor(color).addTraits(TerrainTraits.full()).addTags(tags)
 * .buildAndRegister()} (or {@link #full(BlockTrait)} for blocks that supply their own model).
 */
public class TerrainTraits {
    private TerrainTraits() {
    }

    /**
     * The terrain composite with the standard top/side/bottom model built from the block's
     * {@link BaseTerrainBlock#getBaseBlock() base block}. The model trait is only attached during datagen
     * (it is {@code null} otherwise, and {@link TraitLists} drops it).
     *
     * @return terrain + pottable-soil + silk-touch loot + default top/side/bottom model
     */
    public static List<BlockTrait<?, ?>> full() {
        return full(ModCore.isDatagen() ? defaultModel() : null);
    }

    /**
     * The terrain composite with a caller-supplied model, for terrain blocks whose textures do not fit the
     * default top/side/bottom model (e.g. amber-moss-style multi-variant sides). A {@code null} model is
     * dropped, so callers may pass {@code datagen ? model : null} directly.
     *
     * @param model the block model trait to use instead of the default (may be {@code null})
     * @return terrain + pottable-soil + silk-touch loot + the supplied model
     */
    public static List<BlockTrait<?, ?>> full(BlockTrait<Block, ?> model) {
        return TraitLists.of(
                TerrainBlockTrait.DEFAULT,
                PottableSoilBlockTrait.DEFAULT,
                loot(),
                model
        );
    }

    /**
     * The loot every {@link BaseTerrainBlock} used to generate through the retired {@code BlockLootProvider}
     * interface: silk-touch drops the block itself, otherwise its base block (read dynamically via
     * {@link BaseTerrainBlock#getBaseBlock()}, so a subclass's custom base is honoured).
     *
     * @return the silk-touch terrain loot trait
     */
    public static BlockTrait<?, ?> loot() {
        return BlockTraits.LOOT_TABLE.with((tableKey, blockKey, block, provider) ->
                provider.dropWithSilkTouch(block, ((BaseTerrainBlock) block).getBaseBlock(), ConstantValue.exactly(1)));
    }

    private static BlockModelTrait defaultModel() {
        return ClientBlockTraits.MODEL.with(
                (key, block, generator) -> {
                    final var terrain = (BaseTerrainBlock) block;
                    generator.createBlockTopSideBottom(terrain.getBaseBlock(), terrain, true);
                }
        );
    }
}
