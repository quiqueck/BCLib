package org.betterx.bclib.integration.jei;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.blocks.LeveledAnvilBlock;
import org.betterx.bclib.interfaces.AlloyingRecipeWorkstation;
import org.betterx.bclib.recipes.AlloyingRecipe;
import org.betterx.bclib.recipes.AnvilRecipe;
import de.ambertation.wover.recipe.api.SyncedRecipes;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = BCLib.C.mk("jei_plugin");

    private List<AnvilCategory> anvilCategories = List.of();

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();

        List<Block> anvils = LeveledAnvilBlock.getAnvils();
        int maxLevel = anvils.stream().mapToInt(LeveledAnvilBlock::getAnvilCraftingLevel).max().orElse(0);

        List<AnvilCategory> anvilCategories = new ArrayList<>();
        for (int level = 0; level <= maxLevel; level++) {
            final int lvl = level;
            Block workstation = anvils.stream()
                                      .filter(b -> LeveledAnvilBlock.canHandle(b, lvl))
                                      .min(Comparator.comparingInt(LeveledAnvilBlock::getAnvilCraftingLevel))
                                      .orElse(Blocks.ANVIL);
            anvilCategories.add(new AnvilCategory(guiHelper, level, workstation));
        }
        this.anvilCategories = anvilCategories;

        List<IRecipeCategory<?>> categories = new ArrayList<>();
        categories.add(new AlloyingCategory(guiHelper));
        categories.addAll(anvilCategories);
        registration.addRecipeCategories(categories.toArray(IRecipeCategory[]::new));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Level clientLevel = Minecraft.getInstance().level;
        if (clientLevel == null) return;

        registration.addRecipes(
                AlloyingCategory.TYPE,
                List.copyOf(SyncedRecipes.allOfType(clientLevel, AlloyingRecipe.TYPE))
        );

        List<List<AnvilRecipeDisplay>> byLevel = new ArrayList<>();
        for (int i = 0; i < anvilCategories.size(); i++) byLevel.add(new ArrayList<>());

        // read the hammer tag off the connection's own registries: a client connected to a
        // dedicated server never gets a WorldState registry access, so the no-arg
        // AnvilRecipe.getAllHammers() would leave every anvil category empty there.
        final Iterable<Holder<Item>> hammers = AnvilRecipe.getAllHammers(clientLevel.registryAccess());

        for (RecipeHolder<AnvilRecipe> recipeHolder : SyncedRecipes.allOfType(clientLevel, AnvilRecipe.TYPE)) {
            AnvilRecipe recipe = recipeHolder.value();
            int level = Math.max(0, Math.min(anvilCategories.size() - 1, recipe.getAnvilLevel()));
            for (Holder<Item> hammer : hammers) {
                if (recipe.canUse(hammer.value())) {
                    byLevel.get(level)
                           .add(new AnvilRecipeDisplay(recipeHolder.id().location(), recipe, hammer.value()));
                }
            }
        }

        for (int level = 0; level < anvilCategories.size(); level++) {
            registration.addRecipes(anvilCategories.get(level).getType(), byLevel.get(level));
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(
                AlloyingCategory.TYPE,
                AlloyingRecipeWorkstation.getWorkstations().toArray(new Block[0])
        );

        List<Block> anvils = LeveledAnvilBlock.getAnvils();
        for (AnvilCategory category : anvilCategories) {
            Block[] catalysts = anvils.stream()
                                      .filter(b -> LeveledAnvilBlock.canHandle(b, category.level))
                                      .toArray(Block[]::new);
            registration.addCraftingStation(category.getType(), catalysts);
        }
    }
}
