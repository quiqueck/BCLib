package org.betterx.bclib.trait.block;

import org.betterx.wover.block.api.client.trait.BlockModelTrait;
import org.betterx.wover.block.api.trait.BlockTraitLookup;
import org.betterx.wover.sets.api.blocks.BlockSet;
import org.betterx.wover.sets.api.blocks.types.Log;

import net.minecraft.resources.ResourceLocation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.Nullable;

/**
 * A wover {@link Log} slot whose model is generated as a weighted, multi-variant pillar (see
 * {@link WeightedPillarModelTrait}) instead of wover's default single-variant log model. Keeps the log's default
 * block/recipe/tags; only its {@code buildModel} differs, so a wood set opts into the randomized-log look by
 * replacing its {@code LOG}/{@code STRIPPED_LOG} slot with this.
 * <p>
 * The {@code _2..._N} variant models this references must exist as (hand-authored) resources; {@code weights.length}
 * must equal the number of variants (base + {@code _2..._length}).
 */
public class WeightedLog extends Log {
    private final int[] weights;
    @Nullable
    private final ResourceLocation sideTexture;
    @Nullable
    private final ResourceLocation endTexture;

    /**
     * @param stripable whether this log can be stripped; also selects {@code LOG} vs {@code STRIPPED_LOG} as the slot
     * @param weights   the per-variant weights (base + {@code _2..._length}); see {@link WeightedPillarModelTrait#log}
     */
    public WeightedLog(boolean stripable, int[] weights) {
        this(stripable, weights, null, null);
    }

    /**
     * @param stripable   whether this log can be stripped
     * @param weights     the per-variant weights (base + {@code _2..._length})
     * @param sideTexture optional explicit side texture; {@code null} uses the block's own {@code _side}
     * @param endTexture  optional explicit end texture; {@code null} uses the block's own {@code _top}
     */
    public WeightedLog(
            boolean stripable,
            int[] weights,
            @Nullable ResourceLocation sideTexture,
            @Nullable ResourceLocation endTexture
    ) {
        super(stripable);
        this.weights = weights;
        this.sideTexture = sideTexture;
        this.endTexture = endTexture;
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected BlockModelTrait buildModel(BlockSet<?> set, BlockTraitLookup blockTraitLookup) {
        return WeightedPillarModelTrait.log(weights, sideTexture, endTexture);
    }
}
