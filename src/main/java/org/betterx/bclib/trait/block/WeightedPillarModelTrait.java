package org.betterx.bclib.trait.block;

import de.ambertation.wover.block.api.client.trait.BlockModelTrait;
import de.ambertation.wover.block.api.client.trait.ClientBlockTraits;
import de.ambertation.wover.core.api.ModCore;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

/**
 * A weighted, multi-variant extension of wover's default log/bark model trait
 * ({@link de.ambertation.wover.block.api.client.model.ModelTraitLibrary#log}/{@code bark}).
 * <p>
 * wover's default log trait emits a single-variant, axis-rotated pillar (a {@code cube_column} model for the
 * vertical axis plus a {@code cube_column_horizontal} model for the horizontal axes). That loses the randomized
 * log look BetterNether and BetterEnd shipped before the wover migration, where each axis is a <em>weighted list
 * of variant models</em> ({@code <name>}, {@code <name>_2}, {@code <name>_3}, ...) so the same log placed in bulk
 * varies its texture/mirroring.
 * <p>
 * This trait restores that: it generates the same base {@code cube_column} model wover's default trait does (so
 * the item model and vertical render are unchanged), then emits a blockstate whose every axis references the base
 * model plus its {@code _2..._N} sibling models with the supplied weights, rotated by the standard
 * {@link BlockModelGenerators#createRotatedPillar() axis rotation}. The {@code _2..._N} variant models themselves
 * stay hand-authored (they are bespoke per family - different mirroring, texture suffixes and weights - see the
 * recovered {@code betterend} references); this trait only owns the base model, the weighted blockstate and the
 * item model.
 * <p>
 * A wood set opts in by replacing its {@code LOG}/{@code BARK} slots with {@link WeightedLog}/{@link WeightedBark}.
 * Like wover's {@code ModelTraitLibrary}, every public factory returns {@code null} outside a datagen environment;
 * the client-only vanilla datagen types are only touched from {@link Impl}, which is loaded solely when
 * {@link ModCore#isDatagen()} is {@code true} (see {@link PathBlockTrait} for the same guard pattern).
 */
public class WeightedPillarModelTrait {
    /**
     * A weighted-variant log model: a {@code cube_column} base model (side/end from the block's own
     * {@code _side}/{@code _top} textures, unless overridden) plus a weighted blockstate referencing that base and
     * its {@code _2..._N} siblings.
     *
     * @param weights   the per-variant weights; {@code weights.length} is the number of variants (base +
     *                  {@code _2..._length}). Weight {@code weights[0]} applies to the base model, {@code weights[i]}
     *                  to model {@code _<i+1>}.
     * @param sideTexture optional explicit side texture (all four sides); {@code null} uses the block's own
     *                    {@code _side} texture
     * @param endTexture  optional explicit end texture (top/bottom); {@code null} uses the block's own {@code _top}
     *                    texture
     * @return the model trait, or {@code null} outside of datagen
     */
    public static BlockModelTrait log(
            int[] weights,
            @Nullable Identifier sideTexture,
            @Nullable Identifier endTexture
    ) {
        return ModCore.isDatagen() ? Impl.column(weights, sideTexture, endTexture, false, null) : null;
    }

    /**
     * A weighted-variant bark model: like {@link #log}, but the base model's side and end textures both default to
     * the matching log block's {@code _side} texture (matching wover's default bark trait).
     *
     * @param logBlock    supplies the matching log block whose {@code _side} texture is reused
     * @param weights     the per-variant weights (see {@link #log})
     * @param sideTexture optional explicit texture for all faces; {@code null} uses the log block's {@code _side}
     * @param endTexture  optional explicit end texture; {@code null} uses the log block's {@code _side}
     * @return the model trait, or {@code null} outside of datagen
     */
    public static BlockModelTrait bark(
            Supplier<Block> logBlock,
            int[] weights,
            @Nullable Identifier sideTexture,
            @Nullable Identifier endTexture
    ) {
        return ModCore.isDatagen() ? Impl.column(weights, sideTexture, endTexture, true, logBlock) : null;
    }

    @Environment(EnvType.CLIENT)
    private static class Impl {
        private static BlockModelTrait column(
                int[] weights,
                @Nullable Identifier sideOverride,
                @Nullable Identifier endOverride,
                boolean bark,
                @Nullable Supplier<Block> logBlock
        ) {
            return ClientBlockTraits.MODEL.with((key, block, generator) -> {
                final Identifier side;
                final Identifier end;
                if (sideOverride != null) {
                    side = sideOverride;
                    end = endOverride != null ? endOverride : sideOverride;
                } else if (bark) {
                    final var tex = TextureMapping.getBlockTexture(logBlock.get()).sprite();
                    side = tex.withSuffix("_side");
                    end = tex.withSuffix("_side");
                } else {
                    final var tex = TextureMapping.getBlockTexture(block).sprite();
                    side = tex.withSuffix("_side");
                    end = tex.withSuffix("_top");
                }

                final var mapping = new TextureMapping()
                        .put(TextureSlot.SIDE, new Material(side))
                        .put(TextureSlot.END, new Material(end));
                final var baseModel = ModelTemplates.CUBE_COLUMN.create(block, mapping, generator.modelOutput());

                final var baseLocation = ModelLocationUtils.getModelLocation(block);
                final var weighted = WeightedList.<Variant>builder();
                weighted.add(BlockModelGenerators.plainModel(baseModel), weights[0]);
                for (int i = 1; i < weights.length; i++) {
                    weighted.add(
                            BlockModelGenerators.plainModel(baseLocation.withSuffix("_" + (i + 1))),
                            weights[i]
                    );
                }

                generator.acceptBlockState(BlockModelGenerators.createAxisAlignedPillarBlock(
                        block,
                        new MultiVariant(weighted.build())
                ));
                generator.delegateItemModel(block, baseModel);
            });
        }
    }
}
