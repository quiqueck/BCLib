package org.betterx.bclib.trait.block;

import de.ambertation.wover.block.api.trait.BlockRecipeTrait;
import de.ambertation.wover.block.api.trait.BlockTraits;
import de.ambertation.wover.core.api.ModCore;
import de.ambertation.wover.recipe.api.RecipeBuilder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.Nullable;

/**
 * {@link BlockTraits#RECIPE} factories for the "variant of a base block" crafting recipes (slab, stairs, wall,
 * button, pressure plate, roof tile) that BetterNether's {@code registerSlab}/{@code registerStairs}/… helpers
 * used to queue through {@code RecipesHelper}.
 * <p>
 * Each factory is a <b>thin adapter</b> over wover's existing {@link RecipeBuilder.Templates} recipe patterns: it
 * simply calls the matching {@code Templates.make…Recipe(source, block)}, which is exactly what the old helper did
 * (via {@code RecipesHelper} → {@code RecipeBuilder.Templates}). Because the recipe is generated for the block the
 * trait is attached to, {@code block} is the produced variant and only the {@code source} block has to be passed in.
 * The recipe ids are namespaced under the produced block's own mod (resolved from its registry key), which for a
 * same-mod source/variant pair — the only case these helpers ever handled — matches the old
 * {@code new Templates(context, MOD.C)} namespacing byte-for-byte.
 * <p>
 * Every factory returns {@code null} outside of a datagen environment (recipes are datagen-only), so the result
 * must be handed to a definition through {@code addTrait}, which drops nulls.
 */
public class RecipeTraits {
    private static RecipeBuilder.Templates templatesFor(ResourceKey<Block> key, RecipeBuilder.Context context) {
        // ModCore.create(id) is cached and returns the mod's canonical instance, so this is the same
        // ModCore the mod's own recipe provider builds its Templates with (namespace == modId for both mods).
        return new RecipeBuilder.Templates(context, ModCore.create(key.identifier().getNamespace()));
    }

    /**
     * A 6-output crafting recipe plus a matching stonecutting recipe turning {@code source} into this slab, in the
     * {@code slabs} group. Mirrors {@code RecipesHelper.makeSlabRecipe(source, slab)}.
     *
     * @param source the base block the slab is crafted from
     * @return the recipe trait, or {@code null} outside of a datagen environment
     */
    public static @Nullable BlockRecipeTrait slabFrom(Block source) {
        return BlockTraits.RECIPE.with((key, block, context) -> templatesFor(key, context).makeSlabRecipe(source, block));
    }

    /**
     * A 4-output crafting recipe plus a matching stonecutting recipe turning {@code source} into these stairs, in
     * the {@code stairs} group. Mirrors {@code RecipesHelper.makeStairsRecipe(source, stairs)}.
     *
     * @param source the base block the stairs are crafted from
     * @return the recipe trait, or {@code null} outside of a datagen environment
     */
    public static @Nullable BlockRecipeTrait stairsFrom(Block source) {
        return BlockTraits.RECIPE.with((key, block, context) -> templatesFor(key, context).makeStairsRecipe(source, block));
    }

    /**
     * A 6-output crafting recipe plus a matching stonecutting recipe turning {@code source} into this wall, in the
     * {@code walls} group. Mirrors {@code RecipesHelper.makeWallRecipe(source, wall)}.
     *
     * @param source the base block the wall is crafted from
     * @return the recipe trait, or {@code null} outside of a datagen environment
     */
    public static @Nullable BlockRecipeTrait wallFrom(Block source) {
        return BlockTraits.RECIPE.with((key, block, context) -> templatesFor(key, context).makeWallRecipe(source, block));
    }

    /**
     * A 1-output crafting recipe turning {@code source} into this button, in the {@code buttons} group. Mirrors
     * {@code RecipesHelper.makeButtonRecipe(source, button)}.
     *
     * @param source the base block the button is crafted from
     * @return the recipe trait, or {@code null} outside of a datagen environment
     */
    public static @Nullable BlockRecipeTrait buttonFrom(Block source) {
        return BlockTraits.RECIPE.with((key, block, context) -> templatesFor(key, context).makeButtonRecipe(source, block));
    }

    /**
     * A 1-output crafting recipe turning {@code source} into this pressure plate, in the {@code plates} group.
     * Mirrors {@code RecipesHelper.makePlateRecipe(source, plate)}.
     *
     * @param source the base block the pressure plate is crafted from
     * @return the recipe trait, or {@code null} outside of a datagen environment
     */
    public static @Nullable BlockRecipeTrait plateFrom(Block source) {
        return BlockTraits.RECIPE.with((key, block, context) -> templatesFor(key, context).makePlateRecipe(source, block));
    }

    /**
     * A 6-output crafting recipe turning {@code source} into this roof tile, in the {@code roof_tile} group.
     * Mirrors {@code RecipesHelper.makeRoofRecipe(source, roof)}.
     *
     * @param source the base block the roof tile is crafted from
     * @return the recipe trait, or {@code null} outside of a datagen environment
     */
    public static @Nullable BlockRecipeTrait roofFrom(Block source) {
        return BlockTraits.RECIPE.with((key, block, context) -> templatesFor(key, context).makeRoofRecipe(source, block));
    }
}
