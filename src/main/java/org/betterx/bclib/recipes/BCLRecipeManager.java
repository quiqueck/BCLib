package org.betterx.bclib.recipes;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.config.Configs;
import org.betterx.bclib.util.CollectionsUtil;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class BCLRecipeManager {
    private static final Map<?, ? extends Map<?, ?>> _RECIPES = Maps.newHashMap();
    private static final Map<?, ? extends List<?>> _SORTED = Maps.newHashMap();
    private static final String MINECRAFT = "minecraft";

    @SuppressWarnings("unchecked")
    private static <C extends Container, T extends Recipe<C>> Map<RecipeType<T>, Map<ResourceLocation, T>> RECIPES() {
        return (Map<RecipeType<T>, Map<ResourceLocation, T>>) _RECIPES;
    }

    @SuppressWarnings("unchecked")
    private static <C extends Container, T extends Recipe<C>> Map<RecipeType<T>, List<T>> SORTED() {
        return (Map<RecipeType<T>, List<T>>) _SORTED;
    }

    public static <C extends Container, T extends Recipe<C>> Optional<T> getSortedRecipe(
            RecipeType<T> type,
            C inventory,
            Level level,
            Function<RecipeType<T>, Map<ResourceLocation, T>> getter
    ) {
        List<T> recipes = BCLRecipeManager.<C, T>SORTED().computeIfAbsent(type, t -> {
            Collection<T> values = getter.apply(type).values();
            List<T> list = new ArrayList<>(values);
            list.sort((v1, v2) -> {
                boolean b1 = v1.getId().getNamespace().equals(MINECRAFT);
                boolean b2 = v2.getId().getNamespace().equals(MINECRAFT);
                return b1 ^ b2 ? (b1 ? 1 : -1) : v1.getId().compareTo(v2.getId());
            });
            return list;
        });
        return recipes.stream().filter(recipe -> recipe.matches(inventory, level)).findFirst();
    }


    public static <C extends Container, T extends Recipe<C>> Map<RecipeType<T>, Map<ResourceLocation, T>> getMap(
            Map<RecipeType<T>, Map<ResourceLocation, T>> recipes
    ) {
        Map<RecipeType<T>, Map<ResourceLocation, T>> result = Maps.newHashMap();

        for (RecipeType<T> type : recipes.keySet()) {
            Map<ResourceLocation, T> typeList = Maps.newHashMap();
            typeList.putAll(recipes.get(type));
            result.put(type, typeList);
        }

        SORTED().clear();
        BCLRecipeManager.<C, T>RECIPES().forEach((type, list) -> {
            if (list != null) {
                Map<ResourceLocation, T> typeList = result.computeIfAbsent(type, i -> Maps.newHashMap());
                for (Entry<ResourceLocation, T> entry : list.entrySet()) {
                    ResourceLocation id = entry.getKey();
                    typeList.computeIfAbsent(id, i -> entry.getValue());
                }
            }
        });

        return result;
    }

    public static <C extends Container, T extends Recipe<C>> Map<ResourceLocation, T> getMapByName(Map<ResourceLocation, T> recipes) {
        Map<ResourceLocation, T> result = CollectionsUtil.getMutable(recipes);
        BCLRecipeManager.<C, T>RECIPES().values()
                        .forEach(map -> map.forEach((location, recipe) -> result.computeIfAbsent(
                                location,
                                i -> recipe
                        )));
        return result;
    }

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