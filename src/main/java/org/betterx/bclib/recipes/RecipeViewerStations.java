package org.betterx.bclib.recipes;

import org.betterx.bclib.blocks.LeveledAnvilBlock;
import org.betterx.bclib.interfaces.AlloyingRecipeWorkstation;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * Which blocks and tools a player needs for BCLib's own recipe types, as recipe viewers (JEI) show
 * them. Kept free of client and recipe-viewer classes, so the same answers can be checked on a
 * dedicated server.
 */
public final class RecipeViewerStations {
    private RecipeViewerStations() {
    }

    /**
     * Every anvil whose {@link LeveledAnvilBlock#getAnvilCraftingLevel(Block) crafting level} is high
     * enough for a {@link AnvilRecipe} of {@code anvilLevel} - the same check the anvil menu applies.
     *
     * @param anvilLevel the {@link AnvilRecipe#getAnvilLevel() level} the recipe needs
     * @return the anvils that have an item, lowest level first
     */
    public static List<Block> anvilsFor(int anvilLevel) {
        return BuiltInRegistries.BLOCK
                .stream()
                .filter(b -> b instanceof AnvilBlock)
                .filter(b -> b.asItem() != Items.AIR)
                .filter(b -> LeveledAnvilBlock.canHandle(b, anvilLevel))
                .sorted(Comparator.comparingInt(LeveledAnvilBlock::getAnvilCraftingLevel))
                .toList();
    }

    /**
     * Every hammer that can craft {@code recipe}: an item in the common hammers tag that is also in the
     * recipe's {@link AnvilRecipe#getAllowedTools() allowed tools}.
     *
     * @param recipe     the anvil recipe
     * @param registries the registries to resolve the tags against, see
     *                   {@link AnvilRecipe#getAllHammers(HolderLookup.Provider)}
     * @return the matching hammers, empty if none is known here
     */
    public static List<Item> hammersFor(AnvilRecipe recipe, @Nullable HolderLookup.Provider registries) {
        final TagKey<Item> allowed = recipe.getAllowedTools();
        final List<Item> hammers = new ArrayList<>();
        for (Holder<Item> hammer : AnvilRecipe.getAllHammers(registries)) {
            if (allowed == null || hammer.is(allowed)) hammers.add(hammer.value());
        }
        return hammers;
    }

    /**
     * Every block that runs {@link AlloyingRecipe}s (e.g. BetterEnd's End Stone Smelter).
     *
     * @return the workstations that have an item
     */
    public static List<Block> alloyingWorkstations() {
        return AlloyingRecipeWorkstation.getWorkstations()
                                        .stream()
                                        .filter(b -> b.asItem() != Items.AIR)
                                        .toList();
    }
}
