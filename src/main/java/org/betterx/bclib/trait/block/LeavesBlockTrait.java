package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import org.betterx.wover.block.api.BlockDefinition;
import org.betterx.wover.block.api.client.trait.BlockModelTrait;
import org.betterx.wover.block.api.client.trait.ClientBlockTraits;
import org.betterx.wover.block.api.trait.*;
import org.betterx.wover.block.impl.trait.BlockTraitImpl;
import org.betterx.wover.core.api.ModCore;

import org.betterx.wover.tag.api.predefined.CommonBlockTags;
import org.betterx.wover.tag.api.predefined.CommonItemTags;
import org.betterx.wover.tag.api.predefined.MineableTags;

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
        return Combiner.of(
                // Leaves are full/cube-ish blocks and must stay grid-aligned, so opt out of the
                // plant X-Z offset that PlantBlockTrait now enables for standalone ground plants.
                PlantBlockTrait.withColor(color, walkable, BlockBehaviour.OffsetType.NONE),
                new LeavesBlockTrait(lightLevel, wet),
                BlockTraits.MINEABLE_WITH.needsShears(),
                saplingDropChance < 0
                        ? BlockTraits.LOOT_TABLE.dropLeaves(saplingBlock)
                        : BlockTraits.LOOT_TABLE.dropLeaves(saplingDropChance, saplingBlock),
                ClientBlockTraits.RENDER_LAYER.cutout(),
                CompostableBlockTrait.withChance(0.3f),
                BlockTraits.FLAMMABLE.withDefault(),
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
                generator.createFlatItem(block);
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
                .addTags(BlockTags.LEAVES, CommonBlockTags.LEAVES, MineableTags.HOE)
                .addItemTags(ItemTags.LEAVES, CommonItemTags.LEAVES);

        if (lightLevel > 0) {
            definition.lightLevel(state -> lightLevel);
        }
    }
}
