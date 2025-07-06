package org.betterx.bclib.util;

import org.betterx.wover.tag.api.predefined.MineableTags;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum LegacyTiers {
    WOOD(0, null, ToolMaterial.WOOD),
    STONE(1, BlockTags.NEEDS_STONE_TOOL, ToolMaterial.STONE),
    IRON(2, BlockTags.NEEDS_IRON_TOOL, ToolMaterial.IRON),
    DIAMOND(3, BlockTags.NEEDS_DIAMOND_TOOL, ToolMaterial.DIAMOND),
    GOLD(0, MineableTags.NEEDS_GOLD_TOOL, ToolMaterial.GOLD),
    NETHERITE(4, MineableTags.NEEDS_NETHERITE_TOOL, ToolMaterial.NETHERITE);

    public final int level;
    @Nullable
    public final TagKey<Block> toolRequirementTag;
    @NotNull
    final ToolMaterial tier;

    LegacyTiers(int level, @Nullable TagKey<Block> toolRequirementTag, @NotNull ToolMaterial tier) {
        this.level = level;
        this.toolRequirementTag = toolRequirementTag;
        this.tier = tier;
    }

    public static Optional<LegacyTiers> forTier(ToolMaterial tier) {
        for (LegacyTiers legacyTier : values()) {
            if (legacyTier.tier == tier) {
                return Optional.of(legacyTier);
            }
        }
        return Optional.empty();
    }


    public static Optional<LegacyTiers> forLevel(int level) {
        for (LegacyTiers legacyTier : values()) {
            if (legacyTier.level == level) {
                return Optional.of(legacyTier);
            }
        }
        return Optional.empty();
    }
}