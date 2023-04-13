package org.betterx.bclib.recipes;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.config.Configs;
import org.betterx.worlds.together.util.DatapackConfigs;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.Container;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Map;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class BCLRecipeManager {
    public static <C extends Container, S extends RecipeSerializer<T>, T extends Recipe<C>> S registerSerializer(
            String modID,
            String id,
            S serializer
    ) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, modID + ":" + id, serializer);
    }

    public static <C extends Container, T extends Recipe<C>> RecipeType<T> registerType(String modID, String type) {
        ResourceLocation recipeTypeId = new ResourceLocation(modID, type);
        return Registry.register(BuiltInRegistries.RECIPE_TYPE, recipeTypeId, new RecipeType<T>() {
            public String toString() {
                return type;
            }
        });
    }

    public static boolean exists(ItemLike item) {
        if (item instanceof Block) {
            return BuiltInRegistries.BLOCK.getKey((Block) item) != BuiltInRegistries.BLOCK.getDefaultKey();
        } else {
            return item != Items.AIR && BuiltInRegistries.ITEM.getKey(item.asItem()) != BuiltInRegistries.ITEM.getDefaultKey();
        }
    }

    private final static HashSet<ResourceLocation> disabledRecipes = new HashSet<>();

    private static void clearRecipeConfig() {
        disabledRecipes.clear();
    }

    private static void processRecipeConfig(@NotNull ResourceLocation sourceId, @NotNull JsonObject root) {
        if (root.has("disable")) {
            root
                    .getAsJsonArray("disable")
                    .asList()
                    .stream()
                    .map(el -> ResourceLocation.tryParse(el.getAsString()))
                    .filter(id -> id != null)
                    .forEach(disabledRecipes::add);
        }
    }

    @ApiStatus.Internal
    public static void removeDisabledRecipes(ResourceManager manager, Map<ResourceLocation, JsonElement> map) {
        clearRecipeConfig();
        DatapackConfigs
                .instance()
                .runForResources(manager, BCLib.MOD_ID, "recipes.json", BCLRecipeManager::processRecipeConfig);

        for (ResourceLocation id : disabledRecipes) {
            if (Configs.MAIN_CONFIG.verboseLogging()) {
                BCLib.LOGGER.info("Disabling Recipe: {}", id);
            }
            map.remove(id);
        }
    }
}