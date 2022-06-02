package ru.bclib.recipes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import ru.bclib.util.CollectionsUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;

public class BCLRecipeManager {
	private static final Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> RECIPES = Maps.newHashMap();
	private static final Map<RecipeType<?>, Object> SORTED = Maps.newHashMap();
	private static final String MINECRAFT = "minecraft";
	
	public static <C extends Container, T extends Recipe<C>> Optional<T> getSortedRecipe(RecipeType<T> type, C inventory, Level level, Function<RecipeType<T>, Map<ResourceLocation, Recipe<C>>> getter) {
		List<Recipe<C>> recipes = (List<Recipe<C>>) SORTED.computeIfAbsent(type, t -> {
			Collection<Recipe<C>> values = getter.apply(type).values();
			List<Recipe<C>> list = new ArrayList<>(values);
			list.sort((v1, v2) -> {
				boolean b1 = v1.getId().getNamespace().equals(MINECRAFT);
				boolean b2 = v2.getId().getNamespace().equals(MINECRAFT);
				return b1 ^ b2 ? (b1 ? 1 : -1) : v1.getId().compareTo(v2.getId());
			});
			return ImmutableList.copyOf(list);
		});
		return recipes.stream().flatMap(recipe -> type.tryMatch(recipe, level, inventory).stream()).findFirst();
	}
	
	public static void addRecipe(RecipeType<?> type, Recipe<?> recipe) {
		Map<ResourceLocation, Recipe<?>> list = RECIPES.computeIfAbsent(type, i -> Maps.newHashMap());
		list.put(recipe.getId(), recipe);
	}
	
	public static <T extends Recipe<?>> T getRecipe(RecipeType<T> type, ResourceLocation id) {
		Map<ResourceLocation, Recipe<?>> map = RECIPES.get(type);
		return map != null ? (T) map.get(id) : null;
	}
	
	public static Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> getMap(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes) {
		Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> result = Maps.newHashMap();
		
		for (RecipeType<?> type : recipes.keySet()) {
			Map<ResourceLocation, Recipe<?>> typeList = Maps.newHashMap();
			typeList.putAll(recipes.get(type));
			result.put(type, typeList);
		}
		
		SORTED.clear();
		RECIPES.forEach((type, list) -> {
			if (list != null) {
				Map<ResourceLocation, Recipe<?>> typeList = result.computeIfAbsent(type, i -> Maps.newHashMap());
				for (Entry<ResourceLocation, Recipe<?>> entry : list.entrySet()) {
					ResourceLocation id = entry.getKey();
					typeList.computeIfAbsent(id, i -> entry.getValue());
				}
			}
		});
		
		return result;
	}
	
	public static Map<ResourceLocation, Recipe<?>> getMapByName(Map<ResourceLocation, Recipe<?>> recipes) {
		Map<ResourceLocation, Recipe<?>> result = CollectionsUtil.getMutable(recipes);
		RECIPES.values().forEach(map -> map.forEach((location, recipe) -> result.computeIfAbsent(location, i -> recipe)));
		return result;
	}
	
	public static <S extends RecipeSerializer<T>, T extends Recipe<?>> S registerSerializer(String modID, String id, S serializer) {
		return Registry.register(Registry.RECIPE_SERIALIZER, modID + ":" + id, serializer);
	}
	
	public static <T extends Recipe<?>> RecipeType<T> registerType(String modID, String type) {
		ResourceLocation recipeTypeId = new ResourceLocation(modID, type);
		return Registry.register(Registry.RECIPE_TYPE, recipeTypeId, new RecipeType<T>() {
			public String toString() {
				return type;
			}
		});
	}
	
	public static boolean exists(ItemLike item) {
		if (item instanceof Block) {
			return Registry.BLOCK.getKey((Block) item) != Registry.BLOCK.getDefaultKey();
		}
		else {
			return Registry.ITEM.getKey(item.asItem()) != Registry.ITEM.getDefaultKey();
		}
	}
	
	public static boolean exists(ItemLike... items) {
		for (ItemLike item : items) {
			if (!exists(item)) {
				return false;
			}
		}
		return true;
	}
}