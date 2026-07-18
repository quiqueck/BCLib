package org.betterx.bclib.trait.block;

import org.betterx.wover.block.api.client.trait.BlockModelTrait;
import org.betterx.wover.block.api.trait.BlockTraitLookup;
import org.betterx.wover.sets.api.blocks.BlockSet;
import org.betterx.wover.sets.api.blocks.SlotType;
import org.betterx.wover.sets.api.blocks.types.Bark;

import net.minecraft.resources.ResourceLocation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.Nullable;

/**
 * A wover {@link Bark} slot whose model is generated as a weighted, multi-variant pillar (see
 * {@link WeightedPillarModelTrait}) instead of wover's default single-variant bark model. The bark base model's
 * textures default to the matching log block's {@code _side} texture (as wover's default bark trait does), unless
 * overridden. A wood set opts into the randomized-bark look by replacing its {@code BARK}/{@code STRIPPED_BARK}
 * slot with this.
 */
public class WeightedBark extends Bark {
    private final boolean stripable;
    private final int[] weights;
    @Nullable
    private final ResourceLocation sideTexture;
    @Nullable
    private final ResourceLocation endTexture;

    /**
     * @param stripable whether this bark can be stripped; also selects {@code BARK} vs {@code STRIPPED_BARK} as the slot
     * @param weights   the per-variant weights (base + {@code _2..._length}); see {@link WeightedPillarModelTrait#bark}
     */
    public WeightedBark(boolean stripable, int[] weights) {
        this(stripable, weights, null, null);
    }

    /**
     * @param stripable   whether this bark can be stripped
     * @param weights     the per-variant weights (base + {@code _2..._length})
     * @param sideTexture optional explicit texture for all faces; {@code null} uses the log block's {@code _side}
     * @param endTexture  optional explicit end texture; {@code null} uses the log block's {@code _side}
     */
    public WeightedBark(
            boolean stripable,
            int[] weights,
            @Nullable ResourceLocation sideTexture,
            @Nullable ResourceLocation endTexture
    ) {
        super(stripable);
        this.stripable = stripable;
        this.weights = weights;
        this.sideTexture = sideTexture;
        this.endTexture = endTexture;
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected BlockModelTrait buildModel(BlockSet<?> set, BlockTraitLookup blockTraitLookup) {
        return WeightedPillarModelTrait.bark(
                () -> set.getBlock(stripable ? SlotType.LOG : SlotType.STRIPPED_LOG),
                weights,
                sideTexture,
                endTexture
        );
    }
}
