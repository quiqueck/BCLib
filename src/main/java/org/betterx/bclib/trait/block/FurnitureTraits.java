package org.betterx.bclib.trait.block;

import org.betterx.bclib.furniture.block.BaseChair;
import org.betterx.bclib.trait.TraitLists;
import de.ambertation.wover.block.api.client.trait.BlockModelTrait;
import de.ambertation.wover.block.api.trait.BlockTrait;
import de.ambertation.wover.block.api.trait.BlockTraits;
import de.ambertation.wover.recipe.api.RecipeBuilder;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * The composite trait bundles BetterNether's {@code registerTaburet}/{@code registerChair}/{@code registerBarStool}
 * helpers used to apply to the BCLib furniture blocks ({@link org.betterx.bclib.furniture.block.BaseTaburet},
 * {@link BaseChair}, {@link org.betterx.bclib.furniture.block.BaseBarStool}): the supplied model, the self-drop
 * (or chair-specific) loot, and the furniture's crafting recipe.
 * <p>
 * Modelled on {@link TerrainTraits}: only the actual <em>traits</em> are bundled. Everything the old helper did
 * that is <b>not</b> a trait stays at the registration site, exactly as the {@code addTags(...)} do in
 * {@code TerrainTraits}:
 * <ul>
 *   <li>the block factory ({@code new BaseTaburet.Wood(source, …)} / {@code new BaseChair.Wood(source, cloth,
 *       …)} / … - {@code from()} retired WP6.14, every wood-set call site already resolved to Wood), a
 *       constructor concern (R8);</li>
 *   <li>the mineable tags ({@code MINEABLE_WITH_AXE} for taburet/chair, {@code MINEABLE_WITH_PICKAXE} plus the
 *       wooden-only {@code MINEABLE_WITH_AXE} for a bar stool), plain {@code addTags(...)} setters — and the
 *       bar-stool wooden/non-wooden split resolves to an explicit per-site branch in Phase 5 (WP5.10), so it
 *       must not be frozen into the bundle;</li>
 *   <li>the furnace-fuel side effect ({@code addFuel(source, block)}), which per user decision 6 becomes an
 *       explicit per-site {@code .addTrait(FuelBlockTrait…)} decision rather than an implicit bundle member.</li>
 * </ul>
 * The model differs per call site (a mod-local {@code NetherModels}/model factory), so it is a required
 * parameter rather than a default.
 * <p>
 * Phase 5 replaces each {@code registerX(name, source, model)} call with a definition chain of the form
 * {@code defineBlock(name, props -> BaseX.from(source, …, props)).replacePropertiesWithCopy(source)
 * .addTraits(FurnitureTraits.x(source, model)).addTags(mineableTag).buildAndRegister()}.
 */
public class FurnitureTraits {
    private FurnitureTraits() {
    }

    /**
     * The trait bundle for a taburet (stool): the supplied model, a plain self-drop loot table, and the
     * {@code "taburet"}-group crafting recipe (two source blocks over two sticks). Mirrors the traits
     * {@code registerTaburet} attached.
     *
     * @param source the base block the taburet is built from and crafted with
     * @param model  the model trait for this taburet
     * @return model + self-drop loot + taburet recipe
     */
    public static List<BlockTrait<?, ?>> taburet(Block source, BlockModelTrait model) {
        return TraitLists.of(
                model,
                BlockTraits.LOOT_TABLE.dropSelf(),
                BlockTraits.RECIPE.with((key, block, context) -> RecipeBuilder
                        .crafting(key.identifier(), block)
                        .shape("##", "II")
                        .addMaterial('#', source)
                        .addMaterial('I', Items.STICK)
                        .group("taburet")
                        .outputCount(1)
                        .category(RecipeCategory.DECORATIONS)
                        .build(context))
        );
    }

    /**
     * The trait bundle for a chair: the supplied model, the bottom-half-only self-drop loot
     * ({@link BaseChair#chairLoot(Block)}), and the {@code "chair"}-group crafting recipe. Mirrors the traits
     * {@code registerChair} attached.
     *
     * @param source the base block the chair is built from and crafted with
     * @param model  the model trait for this chair
     * @return model + chair loot + chair recipe
     */
    public static List<BlockTrait<?, ?>> chair(Block source, BlockModelTrait model) {
        return TraitLists.of(
                model,
                BlockTraits.LOOT_TABLE.with((tableKey, blockKey, chairBlock, provider) -> BaseChair.chairLoot(chairBlock)),
                BlockTraits.RECIPE.with((key, block, context) -> RecipeBuilder
                        .crafting(key.identifier(), block)
                        .shape("I ", "##", "II")
                        .addMaterial('#', source)
                        .addMaterial('I', Items.STICK)
                        .group("chair")
                        .outputCount(1)
                        .category(RecipeCategory.DECORATIONS)
                        .build(context))
        );
    }

    /**
     * The trait bundle for a bar stool: the supplied model, a plain self-drop loot table, and the
     * {@code "bar_stool"}-group crafting recipe (two source blocks over two rows of paired sticks). Mirrors the
     * traits {@code registerBarStool} attached (its pickaxe/axe mineable tags stay at the call site).
     *
     * @param source the base block the bar stool is built from and crafted with
     * @param model  the model trait for this bar stool
     * @return model + self-drop loot + bar stool recipe
     */
    public static List<BlockTrait<?, ?>> barStool(Block source, BlockModelTrait model) {
        return TraitLists.of(
                model,
                BlockTraits.LOOT_TABLE.dropSelf(),
                BlockTraits.RECIPE.with((key, block, context) -> RecipeBuilder
                        .crafting(key.identifier(), block)
                        .shape("##", "II", "II")
                        .addMaterial('#', source)
                        .addMaterial('I', Items.STICK)
                        .group("bar_stool")
                        .outputCount(1)
                        .category(RecipeCategory.DECORATIONS)
                        .build(context))
        );
    }
}
