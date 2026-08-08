package org.betterx.bclib.recipes;

import org.betterx.bclib.BCLib;
import de.ambertation.wover.config.api.DatapackConfigs;
import de.ambertation.wover.recipe.api.SyncedRecipes;
import de.ambertation.wover.recipe.impl.RecipeLookups;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceKey;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class BCLRecipeManager {
    public static final Identifier RECIPES_CONFIG_FILE = BCLib.C.id("recipes.json");

    /**
     * Registers a serializer for a custom recipe type, and makes its recipes readable on the client.
     * <p>
     * The sync registration is not optional here on purpose: every recipe type that goes through this
     * method is a modded one that some GUI - the JEI/REI plugins, an in-world recipe book - has to be
     * able to list, and without it those all come up empty against a dedicated server. See
     * {@link SyncedRecipes} for why the client cannot read them otherwise.
     */
    public static <C extends RecipeInput, S extends RecipeSerializer<T>, T extends Recipe<C>> S registerSerializer(
            String modID,
            String id,
            S serializer
    ) {
        SyncedRecipes.register(serializer);
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, modID + ":" + id, serializer);
    }

    public static <C extends RecipeInput, T extends Recipe<C>> RecipeType<T> registerType(String modID, String type) {
        Identifier recipeTypeId = Identifier.fromNamespaceAndPath(modID, type);
        return Registry.register(
                BuiltInRegistries.RECIPE_TYPE, recipeTypeId, new RecipeType<T>() {
                    public String toString() {
                        return type;
                    }
                }
        );
    }

    public static RecipeBookCategory registerCategory(Identifier location) {
        return Registry.register(
                BuiltInRegistries.RECIPE_BOOK_CATEGORY,
                location,
                new RecipeBookCategory()
        );
    }

    public static boolean exists(ItemLike item) {
        if (item instanceof Block) {
            return BuiltInRegistries.BLOCK.getKey((Block) item) != BuiltInRegistries.BLOCK.getDefaultKey();
        } else {
            return item != Items.AIR && BuiltInRegistries.ITEM.getKey(item.asItem()) != BuiltInRegistries.ITEM.getDefaultKey();
        }
    }

    private final static HashSet<Identifier> disabledRecipes = new HashSet<>();

    /**
     * The {@link ResourceManager} of the datapack load that is currently in progress, published by
     * {@code ReloadableServerResourcesMixin}.
     * <p>
     * 26.3 removed {@code RecipeManager}'s reload-listener phase, which used to hand the manager in as a
     * parameter, so it has to be carried across from the one method that starts the load. Volatile because
     * the {@code RecipeManager} is constructed on a worker thread inside the future
     * {@code ReloadableServerResources#loadResources} returns.
     */
    private static volatile ResourceManager loadingResourceManager = null;

    @ApiStatus.Internal
    public static void setLoadingResourceManager(ResourceManager manager) {
        loadingResourceManager = manager;
    }

    @ApiStatus.Internal
    public static ResourceManager loadingResourceManager() {
        return loadingResourceManager;
    }

    private static void clearRecipeConfig() {
        disabledRecipes.clear();
    }

    private static void processRecipeConfig(@NotNull Identifier sourceId, @NotNull JsonObject root) {
        if (root.has("disable")) {
            root
                    .getAsJsonArray("disable")
                    .asList()
                    .stream()
                    .map(el -> Identifier.tryParse(el.getAsString()))
                    .filter(id -> id != null)
                    .forEach(disabledRecipes::add);
        }
    }

    /**
     * Hides every recipe the {@code recipes.json} datapack config disables from {@code loadedRecipes}.
     * <p>
     * 26.3 made recipes a datapack registry, so this filters the {@code HolderLookup<Recipe<?>>} on its way
     * into {@code RecipeMap.create} rather than subtracting from the finished map. Rebuilding the map and
     * assigning it over {@code RecipeManager.recipes} would strip Fabric's recipe-sync decoration and break
     * player connect - see {@code de.ambertation.wover.recipe.impl.RecipeLookups}.
     */
    @ApiStatus.Internal
    public static HolderLookup<Recipe<?>> removeDisabledRecipes(
            ResourceManager manager,
            HolderLookup<Recipe<?>> loadedRecipes
    ) {
        clearRecipeConfig();
        DatapackConfigs
                .instance()
                .runForResource(manager, RECIPES_CONFIG_FILE, BCLRecipeManager::processRecipeConfig);

        if (disabledRecipes.isEmpty()) return loadedRecipes;

        final Set<Identifier> disabled = Set.copyOf(disabledRecipes);
        for (Identifier id : disabled) BCLib.LOGGER.verbose("Disabling Recipe: {}", id);

        return RecipeLookups.filtered(loadedRecipes, key -> !disabled.contains(key.identifier()));
    }
}