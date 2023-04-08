package org.betterx.bclib.recipes;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.config.PathConfig;

import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;

import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GridRecipe extends AbstractAdvancementRecipe {
    private static final GridRecipe INSTANCE = new GridRecipe();

    private ResourceLocation id;
    private ItemLike output;

    private String group;
    private RecipeType<CraftingRecipe> type;
    private boolean shaped;
    private String[] shape;
    private final Map<Character, Ingredient> materialKeys = Maps.newHashMap();
    private final Map<Character, TagKey<Item>> materialTagKeys = Maps.newHashMap();
    private int count;
    private boolean showNotification;
    private boolean exist;

    protected RecipeCategory bookCategory;

    private GridRecipe() {
    }

    /**
     * Please use {@link BCLRecipeBuilder#crafting(ResourceLocation, ItemLike)} instead
     *
     * @param id
     * @param output
     * @return
     */
    private GridRecipe(ResourceLocation id, ItemLike output) {
        this.id = id;
        this.output = output;

        this.group = "";
        this.type = RecipeType.CRAFTING;
        this.shaped = true;
        this.showNotification = true;
        this.shape = new String[]{"#"};
        this.materialKeys.clear();
        this.count = 1;
        this.bookCategory = RecipeCategory.MISC;

        this.exist = output != null && BCLRecipeManager.exists(output);
        this.createAdvancement(id, output);
    }

    static GridRecipe make(ResourceLocation id, ItemLike output) {
        return new GridRecipe(id, output);
    }

    public GridRecipe checkConfig(PathConfig config) {
        exist &= config.getBoolean("grid", id.getPath(), true);
        return this;
    }

    public GridRecipe setGroup(String group) {
        this.group = group;
        return this;
    }

    public GridRecipe setShape(String... shape) {
        this.shape = shape;
        return this;
    }

    public GridRecipe setList(String shape) {
        this.shape = new String[]{shape};
        this.shaped = false;
        return this;
    }

    public GridRecipe addMaterial(char key, TagKey<Item> value) {
        unlockedBy(value);
        materialTagKeys.put(key, value);
        return this;
    }

    public GridRecipe addMaterial(char key, ItemStack... values) {
        unlockedBy(values);
        return addMaterial(key, Ingredient.of(Arrays.stream(values)));
    }

    public GridRecipe addMaterial(char key, ItemLike... values) {
        for (ItemLike item : values) {
            exist &= BCLRecipeManager.exists(item);
        }
        unlockedBy(values);
        return addMaterial(key, Ingredient.of(values));
    }

    private GridRecipe addMaterial(char key, Ingredient value) {
        materialKeys.put(key, value);
        return this;
    }

    public GridRecipe setOutputCount(int count) {
        this.count = count;
        return this;
    }

    public GridRecipe showNotification(boolean showNotification) {
        this.showNotification = showNotification;
        return this;
    }

    private NonNullList<Ingredient> getMaterials(int width, int height) {
        NonNullList<Ingredient> materials = NonNullList.withSize(width * height, Ingredient.EMPTY);
        int pos = 0;
        boolean hasNonEmpty = false;
        for (String line : shape) {
            for (int i = 0; i < width; i++) {
                char c = line.charAt(i);
                Ingredient material = materialKeys.containsKey(c)
                        ? materialKeys.get(c)
                        : Ingredient.of(materialTagKeys.get(c));
                if (material != null && !material.isEmpty()) hasNonEmpty = true;
                materials.set(pos++, material == null ? Ingredient.EMPTY : material);
            }
        }
        if (!hasNonEmpty) return null;
        return materials;
    }

    public GridRecipe setCategory(RecipeCategory c) {
        bookCategory = c;
        return this;
    }

    private static List<GridRecipe> RECIPES;

    public GridRecipe build() {
        if (RECIPES == null) RECIPES = new ArrayList<>();
        RECIPES.add(this);
        return this;
    }

    public static void registerRecipes(Consumer<FinishedRecipe> cc) {
        if (RECIPES == null) return;

        for (var r : RECIPES) {
            r.build(cc);
        }
        RECIPES.clear();
    }

    public void build(Consumer<FinishedRecipe> cc) {
        if (!exist) {
            BCLib.LOGGER.warning("Unable to build Recipe " + id);
            return;
        }

        int height = shape.length;
        int width = shape[0].length();
        ItemStack result = new ItemStack(output, count);
        if (result.is(Items.AIR)) {
            BCLib.LOGGER.warning("Unable to build Recipe " + id + ": Result is AIR");
            return;
        }

        NonNullList<Ingredient> materials = this.getMaterials(width, height);
        if (materials == null || materials.isEmpty()) {
            BCLib.LOGGER.warning("Unable to build Recipe " + id + ": Empty Material List");
            return;
        }

        if (shaped) {
            final ShapedRecipeBuilder builder = ShapedRecipeBuilder
                    .shaped(bookCategory, output, count)
                    .group(group)
                    .showNotification(showNotification);

            for (String row : this.shape) {
                builder.pattern(row);
            }

            for (Map.Entry<Character, Ingredient> in : materialKeys.entrySet()) {
                Arrays
                        .stream(in.getValue().getItems())
                        .filter(i -> i.getCount() > 0)
                        .forEach(stack -> builder.unlockedBy(
                                "has_" + stack.getDescriptionId(),
                                RecipeProvider.has(stack.getItem())
                        ));

                builder.define(in.getKey(), in.getValue());
            }

            for (Map.Entry<Character, TagKey<Item>> in : materialTagKeys.entrySet()) {
                builder.unlockedBy(
                        "has_tag_" + in.getValue().location().getNamespace() + "_" + in.getValue().location().getPath(),
                        RecipeProvider.has(in.getValue())
                );

                builder.define(in.getKey(), in.getValue());
            }
            builder.save(cc, id);
        } else {
            final ShapelessRecipeBuilder builder = ShapelessRecipeBuilder
                    .shapeless(bookCategory, output, count)
                    .group(group);

            for (Map.Entry<Character, Ingredient> in : materialKeys.entrySet()) {
                Arrays
                        .stream(in.getValue().getItems())
                        .filter(i -> i.getCount() > 0)
                        .forEach(stack -> builder.unlockedBy(
                                "has_" + stack.getDescriptionId(),
                                RecipeProvider.has(stack.getItem())
                        ));

                builder.requires(in.getValue());
            }

            for (Map.Entry<Character, TagKey<Item>> in : materialTagKeys.entrySet()) {
                builder.unlockedBy(
                        "has_tag_" + in.getValue().location().getNamespace() + "_" + in.getValue().location().getPath(),
                        RecipeProvider.has(in.getValue())
                );

                builder.requires(in.getValue());
            }
            builder.save(cc, id);
        }
    }
}
