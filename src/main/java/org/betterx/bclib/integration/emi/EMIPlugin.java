package org.betterx.bclib.integration.emi;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.blocks.LeveledAnvilBlock;
import org.betterx.bclib.interfaces.AlloyingRecipeWorkstation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;

import java.util.Comparator;

public class EMIPlugin implements EmiPlugin {
    private static boolean didInit = false;
    private static int maxAnvilLevel = 1;
    public static final ResourceLocation MY_SPRITE_SHEET = BCLib.makeID(
            "textures/gui/widgets.png"
    );

    public static EmiStack END_ALLOYING_WORKSTATION;
    public static EmiRecipeCategory END_ALLOYING_CATEGORY;

    public static EmiRecipeCategory[] ANVIL_CATEGORIES;
    public static EmiStack[] ANVIL_WORKSTATIONS;

    public static EmiTexture getSprite(int u, int v) {
        return new EmiTexture(MY_SPRITE_SHEET, u, v, 16, 16, 16, 16, 32, 32);
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
                ANVIL_WORKSTATIONS[anvilLevel] = new AnvilEmiStack(LeveledAnvilBlock
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

    static EmiRecipeCategory getAnvilCategoryForLevel(int anvilLevel) {
        anvilLevel = Math.max(0, Math.min(ANVIL_CATEGORIES.length - 1, anvilLevel));
        return ANVIL_CATEGORIES[anvilLevel];
    }
}
