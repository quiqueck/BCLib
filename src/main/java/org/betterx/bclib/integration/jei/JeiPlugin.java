package org.betterx.bclib.integration.jei;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.recipes.AlloyingRecipe;
import org.betterx.bclib.recipes.AnvilRecipe;
import org.betterx.bclib.recipes.RecipeViewerStations;
import de.ambertation.wover.recipe.api.SyncedRecipes;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;

import java.util.Comparator;
import java.util.List;

/**
 * Registered through the {@code jei_mod_plugin} entrypoint, which only JEI (a client mod) reads - so
 * none of the JEI classes here are ever loaded on a dedicated server.
 * <p>
 * The categories belong to BCLib because BCLib owns the recipe types. Workstations and anvils are
 * found by class ({@code AlloyingRecipeWorkstation}, {@code AnvilBlock} + crafting level), so the
 * End Stone Smelter and every BetterEnd anvil show up without the content mods registering anything.
 */
@Environment(EnvType.CLIENT)
public class JeiPlugin implements IModPlugin {
    private static final Identifier UID = BCLib.makeID("jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        final IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

        final List<Block> workstations = RecipeViewerStations.alloyingWorkstations();
        if (!workstations.isEmpty()) {
            registration.addRecipeCategories(new AlloyingCategory(guiHelper, workstations.getFirst()));
        }

        final List<Block> anvils = RecipeViewerStations.anvilsFor(0);
        registration.addRecipeCategories(new AnvilCategory(
                guiHelper,
                anvils.isEmpty() ? Blocks.ANVIL : anvils.getLast()
        ));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        final Level level = Minecraft.getInstance().level;
        if (level == null) return;

        if (!RecipeViewerStations.alloyingWorkstations().isEmpty()) {
            registration.addRecipes(AlloyingCategory.TYPE, List.copyOf(SyncedRecipes.allOfType(level, AlloyingRecipe.TYPE)));
        }

        registration.addRecipes(
                AnvilCategory.TYPE,
                SyncedRecipes.allOfType(level, AnvilRecipe.TYPE)
                             .stream()
                             .sorted(Comparator.<RecipeHolder<AnvilRecipe>>comparingInt(r -> r.value().getAnvilLevel())
                                               .thenComparing(r -> r.id().identifier()))
                             .toList()
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (Block workstation : RecipeViewerStations.alloyingWorkstations()) {
            registration.addCraftingStation(AlloyingCategory.TYPE, workstation);
        }

        // only anvils that can craft at least one of the recipes - a vanilla anvil (level I) cannot
        // do any of BetterEnd's (level II and up)
        final Level level = Minecraft.getInstance().level;
        final int minLevel = level == null ? 1 : SyncedRecipes.allOfType(level, AnvilRecipe.TYPE)
                                                              .stream()
                                                              .mapToInt(r -> r.value().getAnvilLevel())
                                                              .min()
                                                              .orElse(1);
        for (Block anvil : RecipeViewerStations.anvilsFor(minLevel)) {
            registration.addCraftingStation(AnvilCategory.TYPE, anvil);
        }
    }
}
