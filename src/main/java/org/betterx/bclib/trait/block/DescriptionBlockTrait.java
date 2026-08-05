package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import de.ambertation.wover.block.api.trait.BlockTrait;
import de.ambertation.wover.block.api.trait.BlockTraitKey;
import de.ambertation.wover.block.api.trait.GenericBlockTrait;
import de.ambertation.wover.block.impl.trait.BlockTraitImpl;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Free-form tooltip lines for a block's item: the rules a player cannot read off the block itself, such as
 * what a tool does to it or what sneaking while placing it changes.
 * <p>
 * Carries translation keys rather than text, and the item tooltip is the only thing that reads them - the
 * trait describes behaviour, it does not implement any. Whatever it says has to be kept true by hand, so
 * it is for the handful of rules a block class actually has, not for restating its material or its drops.
 * <p>
 * Rules that are already modelled as traits describe themselves and do not belong here:
 * {@link SurvivesOnBlockTrait} and {@link SurvivesOnSolidTrait} generate their own "Survives on ..." line
 * from the real placement rule, which cannot drift the way a hand-written line can.
 * <p>
 * Emitted by {@code ItemMixin} (client-side) in the order the keys were given, above the survival lines.
 */
public class DescriptionBlockTrait extends BlockTraitImpl<Block, GenericBlockTrait> implements GenericBlockTrait {
    public static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "description");

    private static final Map<List<String>, DescriptionBlockTrait> CACHE = new HashMap<>();

    /**
     * The translation keys of the lines, in the order they are shown.
     */
    public final List<String> translationKeys;

    /**
     * All lines of one block in a single trait instance, so their order is the order they were written in
     * - traits attached under the same key have no guaranteed order among themselves.
     *
     * @param translationKeys the lines, as translation keys
     * @return the (cached) trait carrying them
     */
    public static DescriptionBlockTrait of(String... translationKeys) {
        return CACHE.computeIfAbsent(List.of(translationKeys), DescriptionBlockTrait::new);
    }

    private DescriptionBlockTrait(List<String> translationKeys) {
        this.translationKeys = translationKeys;
    }

    @Override
    public BlockTraitKey key() {
        return KEY;
    }

    @Override
    public GenericBlockTrait forRuntime() {
        return this;
    }

    /**
     * Appends the description lines of every {@link DescriptionBlockTrait} attached to {@code block}.
     *
     * @param block    the (registered) block to describe
     * @param consumer receives the tooltip lines
     */
    @Environment(EnvType.CLIENT)
    public static void appendHoverText(Block block, Consumer<Component> consumer) {
        BlockTrait.runtimeTraits(block)
                  .filter(t -> t instanceof DescriptionBlockTrait)
                  .flatMap(t -> ((DescriptionBlockTrait) t).translationKeys.stream())
                  .forEach(key -> consumer.accept(Component.translatable(key).withStyle(ChatFormatting.GRAY)));
    }
}
