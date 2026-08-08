package org.betterx.bclib.api.v3.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.ApiStatus;

/**
 * Vanilla tags that lost their Java constant in 26.3 but are still <em>shipped</em> by vanilla as data.
 * <p>
 * 26.3 pruned {@link BlockTags}{@code .COAL_ORES/DIAMOND_ORES/EMERALD_ORES/LAPIS_ORES/REDSTONE_ORES}
 * (it kept {@code GOLD_ORES}, {@code IRON_ORES}, {@code COPPER_ORES} and added an aggregate
 * {@code ORES}). Only the Java handles went away: {@code data/minecraft/tags/block/coal_ores.json} and
 * its four siblings are still in the jar with unchanged contents, so the keys are simply recreated here
 * rather than the checks that used them being dropped.
 * <p>
 * Mirrors WorldWeaver's {@code PrunedVanillaTags} (wover-sets-api, commit {@code bd3519a7}).
 */
@ApiStatus.Internal
public final class PrunedVanillaTags {
    private PrunedVanillaTags() {
    }

    private static TagKey<Block> block(String name) {
        return TagKey.create(Registries.BLOCK, Identifier.withDefaultNamespace(name));
    }

    /** {@code minecraft:coal_ores} - was {@code BlockTags.COAL_ORES}. */
    public static final TagKey<Block> COAL_ORES = block("coal_ores");
    /** {@code minecraft:diamond_ores} - was {@code BlockTags.DIAMOND_ORES}. */
    public static final TagKey<Block> DIAMOND_ORES = block("diamond_ores");
    /** {@code minecraft:emerald_ores} - was {@code BlockTags.EMERALD_ORES}. */
    public static final TagKey<Block> EMERALD_ORES = block("emerald_ores");
    /** {@code minecraft:lapis_ores} - was {@code BlockTags.LAPIS_ORES}. */
    public static final TagKey<Block> LAPIS_ORES = block("lapis_ores");
    /** {@code minecraft:redstone_ores} - was {@code BlockTags.REDSTONE_ORES}. */
    public static final TagKey<Block> REDSTONE_ORES = block("redstone_ores");
}
