package org.betterx.bclib.recipes;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.config.PathConfig;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;

import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Map;

public class GridRecipe extends AbstractAdvancementRecipe {
    private static final GridRecipe INSTANCE = new GridRecipe();

    private ResourceLocation id;
    private ItemLike output;

    private String group;
    private RecipeType<CraftingRecipe> type;
    private boolean shaped;
    private String[] shape;
    private final Map<Character, Ingredient> materialKeys = Maps.newHashMap();
    private int count;
    private boolean exist;

    protected CraftingBookCategory bookCategory;

    private GridRecipe() {
    }

    /**
     * Please use {@link BCLRecipeBuilder#crafting(ResourceLocation, ItemLike)} instead
     *
     * @param id
     * @param output
     * @return
     */
    static GridRecipe make(ResourceLocation id, ItemLike output) {
        INSTANCE.id = id;
        INSTANCE.output = output;

        INSTANCE.group = "";
        INSTANCE.type = RecipeType.CRAFTING;
        INSTANCE.shaped = true;
        INSTANCE.shape = new String[]{"#"};
        INSTANCE.materialKeys.clear();
        INSTANCE.count = 1;
        INSTANCE.bookCategory = CraftingBookCategory.MISC;

        INSTANCE.exist = output != null && BCLRecipeManager.exists(output);
        INSTANCE.createAdvancement(id, output);
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
        unlockedBy(value);
        return addMaterial(key, Ingredient.of(value));
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

    private NonNullList<Ingredient> getMaterials(int width, int height) {
        NonNullList<Ingredient> materials = NonNullList.withSize(width * height, Ingredient.EMPTY);
        int pos = 0;
        boolean hasNonEmpty = false;
        for (String line : shape) {
            for (int i = 0; i < width; i++) {
                char c = line.charAt(i);
                Ingredient material = materialKeys.get(c);
                if (material != null && !material.isEmpty()) hasNonEmpty = true;
                materials.set(pos++, material == null ? Ingredient.EMPTY : material);
            }
        }
        if (!hasNonEmpty) return null;
        return materials;
    }

    public GridRecipe setCraftingBookCategory(CraftingBookCategory c) {
        bookCategory = c;
        return this;
    }


    public void build() {
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

        CraftingRecipe recipe = shaped ? new ShapedRecipe(
                id,
                group,
                bookCategory,
                width,
                height,
                materials,
                result
        ) : new ShapelessRecipe(id, group, bookCategory, result, materials);

        BCLRecipeManager.addRecipe(type, recipe);
        registerAdvancement(recipe);
    }
}
