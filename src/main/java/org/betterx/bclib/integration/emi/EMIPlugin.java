package org.betterx.bclib.integration.emi;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.blocks.LeveledAnvilBlock;
import org.betterx.bclib.interfaces.AlloyingRecipeWorkstation;
import org.betterx.worlds.together.util.Logger;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class EMIPlugin implements EmiPlugin {
    private static boolean didInit = false;
    private static int maxAnvilLevel = 1;
    public static final ResourceLocation BCL_SIMPLIFIED_SPRITES = BCLib.makeID(
            "textures/gui/widgets.png"
    );

    public static EmiStack END_ALLOYING_WORKSTATION;
    public static EmiRecipeCategory END_ALLOYING_CATEGORY;

    public static EmiRecipeCategory[] ANVIL_CATEGORIES;
    public static EmiStack[] ANVIL_WORKSTATIONS;

    public static EmiTexture getSprite(int u, int v) {
        return new EmiTexture(BCL_SIMPLIFIED_SPRITES, u, v, 16, 16, 16, 16, 32, 32);
    }

    public void lazyInit() {
        if (!didInit) {
            didInit = true;
            lazyInitAlloyingCategory();
            lazyInitAnvilCategories();
        }
    }

    private void lazyInitAlloyingCategory() {
        var workstations = AlloyingRecipeWorkstation.getWorkstationIcon();
        if (!workstations.is(Blocks.BARRIER.asItem())) {
            END_ALLOYING_WORKSTATION = EmiStack.of(workstations);

            END_ALLOYING_CATEGORY = new EmiRecipeCategory(
                    BCLib.makeID("alloying"),
                    END_ALLOYING_WORKSTATION,
                    getSprite(16, 0)
            );
        }
    }


    private void lazyInitAnvilCategories() {
        if (ANVIL_CATEGORIES == null) {
            maxAnvilLevel = Math.max(1, LeveledAnvilBlock
                    .getAnvils()
                    .stream()
                    .map(LeveledAnvilBlock::getAnvilCraftingLevel)
                    .reduce(0, Math::max)
            );

            ANVIL_CATEGORIES = new EmiRecipeCategory[maxAnvilLevel + 1];
            ANVIL_WORKSTATIONS = new EmiStack[maxAnvilLevel + 1];

            for (int anvilLevel = 0; anvilLevel <= maxAnvilLevel; anvilLevel++) {
                int finalAnvilLevel = anvilLevel;
                ANVIL_WORKSTATIONS[anvilLevel] = EmiStack.of(LeveledAnvilBlock
                        .getAnvils()
                        .stream()
                        .filter(b -> LeveledAnvilBlock.canHandle(b, finalAnvilLevel))
                        .sorted(Comparator.comparingInt(LeveledAnvilBlock::getAnvilCraftingLevel))
                        .findFirst().orElse(Blocks.BARRIER)
                );
                ANVIL_CATEGORIES[anvilLevel] = new EMIAnvilRecipeCategory(
                        BCLib.makeID("anvil_" + anvilLevel),
                        ANVIL_WORKSTATIONS[anvilLevel],
                        getSprite(0, 0),
                        anvilLevel
                );

                if (anvilLevel > 0 && ANVIL_WORKSTATIONS[anvilLevel].isEqual(ANVIL_WORKSTATIONS[anvilLevel - 1])) {
                    ANVIL_WORKSTATIONS[anvilLevel - 1] = ANVIL_WORKSTATIONS[anvilLevel];
                    ANVIL_CATEGORIES[anvilLevel - 1] = ANVIL_CATEGORIES[anvilLevel];
                }
            }
        }
    }


    @Override
    public void register(EmiRegistry emiRegistry) {
        lazyInit();
        final RecipeManager manager = emiRegistry.getRecipeManager();

        if (END_ALLOYING_CATEGORY != null && END_ALLOYING_WORKSTATION != null) {
            emiRegistry.addCategory(END_ALLOYING_CATEGORY);
            emiRegistry.addWorkstation(END_ALLOYING_CATEGORY, END_ALLOYING_WORKSTATION);

            EMIAlloyingRecipe.addAllRecipes(emiRegistry, manager);
        }

        if (ANVIL_CATEGORIES != null && ANVIL_WORKSTATIONS != null && ANVIL_CATEGORIES.length > 0) {
            for (int i = 0; i <= maxAnvilLevel; i++) {
                emiRegistry.addCategory(ANVIL_CATEGORIES[i]);
                emiRegistry.addWorkstation(ANVIL_CATEGORIES[i], ANVIL_WORKSTATIONS[i]);
            }
            EMIAnvilRecipe.addAllRecipes(emiRegistry, manager);
        }
    }

    public static <C extends Container, T extends Recipe<C>, E extends EmiRecipe> void addAllRecipes(
            EmiRegistry emiRegistry,
            RecipeManager manager,
            Logger logger,
            RecipeType<T> recipeType,
            Function<T, E> createRecipe
    ) {
        addAllRecipes(
                emiRegistry,
                manager,
                logger,
                recipeType,
                (_ignored) -> null,
                (recipe, _ignored) -> createRecipe.apply(recipe)
        );
    }

    public static <C extends Container, T extends Recipe<C>, E extends EmiRecipe, V> void addAllRecipes(
            EmiRegistry emiRegistry,
            RecipeManager manager,
            Logger logger,
            RecipeType<T> recipeType,
            Function<T, List<V>> variantSupplier,
            BiFunction<T, V, E> createRecipe
    ) {
        for (T recipe : manager.getAllRecipesFor(recipeType)) {
            List<V> variants = variantSupplier.apply(recipe);
            if (variants == null) {
                emiRegistry.addRecipe(createRecipe.apply(recipe, null));
            } else {
                for (V variantData : variants) {
                    try {
                        emiRegistry.addRecipe(createRecipe.apply(recipe, variantData));
                    } catch (Exception e) {
                        logger.error("Exception when parsing vanilla recipe " + recipe.getId(), e);
                    }
                }
            }
        }
    }


    static EmiRecipeCategory getAnvilCategoryForLevel(int anvilLevel) {
        anvilLevel = Math.max(0, Math.min(ANVIL_CATEGORIES.length - 1, anvilLevel));
        return ANVIL_CATEGORIES[anvilLevel];
    }
}
