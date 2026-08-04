package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import de.ambertation.wover.block.api.BlockDefinition;
import de.ambertation.wover.block.api.client.trait.BlockModelTrait;
import de.ambertation.wover.block.api.client.trait.ClientBlockTraits;
import de.ambertation.wover.block.api.trait.*;
import de.ambertation.wover.block.impl.trait.BlockTraitImpl;
import de.ambertation.wover.core.api.ModCore;

import de.ambertation.wover.tag.api.predefined.CommonBlockTags;
import de.ambertation.wover.tag.api.predefined.CommonItemTags;
import de.ambertation.wover.tag.api.predefined.MineableTags;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import org.jetbrains.annotations.Nullable;

public class LeavesBlockTrait extends BlockTraitImpl<Block, GenericBlockTrait> {
    private static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "leaves");

    public static List<BlockTrait<?, ?>> withDefault() {
        return withColor(MapColor.PLANT, 0, false, 0.0F, null);
    }

    public static List<BlockTrait<?, ?>> withColor(
            MapColor color,
            int lightLevel,
            boolean wet,
            @Nullable Block saplingBlock
    ) {
        return withColor(
                color, lightLevel, wet, -1,
                saplingBlock == null ? Blocks.OAK_SAPLING : saplingBlock
        );
    }

    public static List<BlockTrait<?, ?>> withColor(
            MapColor color,
            float saplingDropChance,
            @Nullable Block saplingBlock
    ) {
        return withColor(color, 0, false, saplingDropChance, saplingBlock);
    }

    /**
     * @param saplingDropChance the probability the sapling drops at fortune level 1, or a negative value to
     *                          use the vanilla leaves curve. This is <em>not</em> a 1-in-N denominator: a
     *                          1-in-16 drop is {@code 1F / 16F}, and vanilla leaves are {@code 0.0625F}.
     *                          Passing a whole number here makes the drop unconditional - see
     *                          {@code LootTableTrait.Builder#dropLeaves(float, Block)}, which rejects it.
     *                          Blocks whose "leaves" are a large volume (tree canopies rather than a thin
     *                          shell) want a rate well below a vanilla leaf's, or a single tree yields
     *                          dozens of saplings.
     */
    public static List<BlockTrait<?, ?>> withColor(
            MapColor color,
            int lightLevel,
            boolean wet,
            float saplingDropChance,
            @Nullable Block saplingBlock
    ) {
        return withColor(color, lightLevel, wet, saplingDropChance, saplingBlock, true);
    }

    public static List<BlockTrait<?, ?>> withColor(
            MapColor color,
            int lightLevel,
            boolean wet,
            float saplingDropChance,
            @Nullable Block saplingBlock,
            boolean generateModel
    ) {
        return withColor(color, lightLevel, wet, saplingDropChance, saplingBlock, generateModel, true);
    }

    /**
     * @param generateModel when {@code false}, the default cube block/item model is <em>not</em> attached, so
     *                      the block can supply its own model (e.g. hand-authored multi-variant leaves routed
     *                      through {@code ModelTraitLibrary.externalModel()}). Every other overload defaults
     *                      this to {@code true} to preserve the vanilla-style cube model for simple leaves.
     * @param walkable      when {@code true} (the default for genuine cube-shaped leaves), the block keeps solid
     *                      collision like vanilla {@code *_leaves}. Decorative, sideways-protruding variants
     *                      (e.g. {@code FurBlock}-based outer leaves) pass {@code false} to stay pass-through.
     */
    public static List<BlockTrait<?, ?>> withColor(
            MapColor color,
            int lightLevel,
            boolean wet,
            float saplingDropChance,
            @Nullable Block saplingBlock,
            boolean generateModel,
            boolean walkable
    ) {
        return withColor(color, lightLevel, wet, saplingDropChance, saplingBlock, generateModel, walkable, true);
    }

    /**
     * @param flammable when {@code true} (every existing overload's default, preserving today's BE
     *                  behaviour), bundles the vanilla-leaves {@code FLAMMABLE.with(30, 60)} trait.
     *                  BetterNether leaves don't burn - its {@code staticLeaves()} preset never carried a
     *                  FLAMMABLE registration - so BN's fold passes {@code false} to suppress it. A second
     *                  {@code FLAMMABLE} trait added elsewhere would no-op (first registration wins), so this
     *                  flag is the only way to keep it off for nether leaves.
     */
    public static List<BlockTrait<?, ?>> withColor(
            MapColor color,
            int lightLevel,
            boolean wet,
            float saplingDropChance,
            @Nullable Block saplingBlock,
            boolean generateModel,
            boolean walkable,
            boolean flammable
    ) {
        return withColor(color, lightLevel, wet, saplingDropChance, saplingBlock, generateModel, walkable, flammable, true);
    }

    /**
     * @param cutout when {@code true} (every existing overload's default, preserving today's BE behaviour),
     *               bundles {@code ClientBlockTraits.RENDER_LAYER.cutout()}. BetterNether's four
     *               {@code staticLeaves()}-preset blocks (willow/rubeus/anchor_tree/nether_sakura) golden as
     *               {@code renderLayer=SOLID} - they're multi-part hand-authored models, not simple
     *               alpha-cutout cubes - so BN's fold passes {@code false} to leave the default (SOLID) render
     *               layer alone.
     */
    public static List<BlockTrait<?, ?>> withColor(
            MapColor color,
            int lightLevel,
            boolean wet,
            float saplingDropChance,
            @Nullable Block saplingBlock,
            boolean generateModel,
            boolean walkable,
            boolean flammable,
            boolean cutout
    ) {
        return Combiner.of(
                // Leaves are full/cube-ish blocks and must stay grid-aligned, so opt out of the
                // plant X-Z offset that PlantBlockTrait now enables for standalone ground plants.
                PlantBlockTrait.withColor(color, walkable, BlockBehaviour.OffsetType.NONE),
                new LeavesBlockTrait(lightLevel, wet),
                BlockTraits.MINEABLE_WITH.needsShears(),
                saplingDropChance < 0
                        ? BlockTraits.LOOT_TABLE.dropLeaves(saplingBlock)
                        : BlockTraits.LOOT_TABLE.dropLeaves(saplingDropChance, saplingBlock),
                cutout ? ClientBlockTraits.RENDER_LAYER.cutout() : null,
                CompostableBlockTrait.withChance(0.3f),
                // Vanilla leaves all burn/spread much faster than the generic block default (5/5) -
                // every *_leaves entry in FireBlock uses 30/60, so match that instead of the flat default.
                flammable ? BlockTraits.FLAMMABLE.with(30, 60) : null,
                generateModel && ModCore.isDatagen() ? ClientModel.build() : null
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
            return ClientBlockTraits.MODEL.with((key, block, generator) -> {
                generator.createCubeModel(block);
                generator.delegateItemModel(block);
            });
        }
    }

    public final int lightLevel;
    public final boolean wet;

    private LeavesBlockTrait(int lightLevel, boolean wet) {
        this.lightLevel = lightLevel;
        this.wet = wet;
    }

    @Override
    public BlockTraitKey key() {
        return KEY;
    }

    @Override
    public void configure(BlockDefinition<Block, ? extends BlockDefinition<Block, ?>> definition) {
        super.configure(definition);

        definition
                // NOT replaceable: leaves are solid blocks, not grass-like. Marking them replaceable let a
                // block placed against a leaf overwrite the leaf (e.g. right-clicking a leaf with stone).
                .strength(0.2f)
                .isValidSpawn(Blocks::ocelotOrParrot)
                .isSuffocating(Blocks::never)
                .isViewBlocking(Blocks::never)
                .isRedstoneConductor(Blocks::never)
                .sound(wet ? SoundType.WET_GRASS : SoundType.GRASS)
                // sword_efficient (WP: mineable-audit §5): vanilla's own leaves are all sword_efficient;
                // neither BetterEnd's nor BetterNether's leaves populated it. Added here since every leaf
                // block in both mods goes through this one trait.
                .addTags(BlockTags.LEAVES, CommonBlockTags.LEAVES, MineableTags.HOE, BlockTags.SWORD_EFFICIENT)
                .addItemTags(ItemTags.LEAVES, CommonItemTags.LEAVES);

        if (lightLevel > 0) {
            definition.lightLevel(state -> lightLevel);
        }
    }
}
