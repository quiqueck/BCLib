package org.betterx.bclib.recipes;

import org.betterx.bclib.config.PathConfig;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;

import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Map;

public class GridRecipe {
    private static final GridRecipe INSTANCE = new GridRecipe();

    private ResourceLocation id;
    private ItemLike output;

    private String group;
    private RecipeType<?> type;
    private boolean shaped;
    private String[] shape;
    private final Map<Character, Ingredient> materialKeys = Maps.newHashMap();
    private int count;
    private boolean exist;

    private GridRecipe() {
    }

    public static GridRecipe make(String modID, String name, ItemLike output) {
        return make(new ResourceLocation(modID, name), output);
    }

    public static GridRecipe make(ResourceLocation id, ItemLike output) {
        INSTANCE.id = id;
        INSTANCE.output = output;

        INSTANCE.group = "";
        INSTANCE.type = RecipeType.CRAFTING;
        INSTANCE.shaped = true;
        INSTANCE.shape = new String[]{"#"};
        INSTANCE.materialKeys.clear();
        INSTANCE.count = 1;

        INSTANCE.exist = output != null && BCLRecipeManager.exists(output);

        return INSTANCE;
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
        return addMaterial(key, Ingredient.of(value));
    }

    public GridRecipe addMaterial(char key, ItemStack... value) {
        return addMaterial(key, Ingredient.of(Arrays.stream(value)));
    }

    public GridRecipe addMaterial(char key, ItemLike... values) {
        for (ItemLike item : values) {
            exist &= BCLRecipeManager.exists(item);
        }
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

    private NonNullList<Ingredient> getMaterials(int width, int height) {
        NonNullList<Ingredient> materials = NonNullList.withSize(width * height, Ingredient.EMPTY);
        int pos = 0;
        for (String line : shape) {
            for (int i = 0; i < width; i++) {
                char c = line.charAt(i);
                Ingredient material = materialKeys.get(c);
                materials.set(pos++, material == null ? Ingredient.EMPTY : material);
            }
        }
        return materials;
    }

    public void build() {
        if (!exist) {
            return;
        }

        int height = shape.length;
        int width = shape[0].length();
        ItemStack result = new ItemStack(output, count);
        NonNullList<Ingredient> materials = this.getMaterials(width, height);

        CraftingRecipe recipe = shaped ? new ShapedRecipe(
                id,
                group,
                width,
                height,
                materials,
                result
        ) : new ShapelessRecipe(id, group, result, materials);
        BCLRecipeManager.addRecipe(type, recipe);
    }
}
