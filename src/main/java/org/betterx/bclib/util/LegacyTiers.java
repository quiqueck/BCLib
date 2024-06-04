package org.betterx.bclib.util;

import org.betterx.worlds.together.tag.v3.MineableTags;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum LegacyTiers {
    WOOD(0, null, Tiers.WOOD),
    STONE(1, BlockTags.NEEDS_STONE_TOOL, Tiers.STONE),
    IRON(2, BlockTags.NEEDS_IRON_TOOL, Tiers.IRON),
    DIAMOND(3, BlockTags.NEEDS_DIAMOND_TOOL, Tiers.DIAMOND),
    GOLD(0, MineableTags.NEEDS_GOLD_TOOL, Tiers.GOLD),
    NETHERITE(4, MineableTags.NEEDS_NETHERITE_TOOL, Tiers.NETHERITE);

    public final int level;
    @Nullable
    public final TagKey<Block> toolRequirementTag;
    @NotNull
    Tier tier;

    LegacyTiers(int level, @Nullable TagKey<Block> toolRequirementTag, @NotNull Tier tier) {
        this.level = level;
        this.toolRequirementTag = toolRequirementTag;
        this.tier = tier;
    }

    public static Optional<LegacyTiers> forTier(Tier tier) {
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