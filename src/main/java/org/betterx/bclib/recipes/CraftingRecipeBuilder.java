package org.betterx.bclib.recipes;

import org.betterx.bclib.BCLib;

import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CraftingRecipeBuilder extends AbstractBaseRecipeBuilder<CraftingRecipeBuilder> {
    private String[] shape;
    private boolean showNotification;

    protected CraftingRecipeBuilder(
            ResourceLocation id,
            ItemLike output
    ) {
        super(id, output);
        this.showNotification = true;
        this.shape = new String[]{"#"};
    }

    static CraftingRecipeBuilder make(ResourceLocation id, ItemLike output) {
        return new CraftingRecipeBuilder(id, output);
    }

    protected final Map<String, CriterionTriggerInstance> unlocks = new HashMap<>();
    protected final Map<Character, Ingredient> materials = new HashMap<>();

    @Override
    public CraftingRecipeBuilder setOutputCount(int count) {
        return super.setOutputCount(count);
    }

    public CraftingRecipeBuilder addMaterial(char key, TagKey<Item> value) {
        unlockedBy(value);
        materials.put(key, Ingredient.of(value));
        return this;
    }

    public CraftingRecipeBuilder addMaterial(char key, ItemStack... values) {
        unlockedBy(values);
        return addMaterial(key, Ingredient.of(Arrays.stream(values)));
    }

    public CraftingRecipeBuilder addMaterial(char key, ItemLike... values) {
        for (ItemLike item : values) {
            this.alright &= BCLRecipeManager.exists(item);
        }
        unlockedBy(values);
        return addMaterial(key, Ingredient.of(values));
    }

    private CraftingRecipeBuilder addMaterial(char key, Ingredient value) {
        materials.put(key, value);
        return this;
    }

    public CraftingRecipeBuilder setShape(String... shape) {
        this.shape = shape;
        return this;
    }

    public CraftingRecipeBuilder shapeless() {
        this.shape = null;
        return this;
    }

    /**
     * @param shape
     * @return
     * @deprecated Use {@link #shapeless()} instead
     */
    @Deprecated(forRemoval = true)
    public CraftingRecipeBuilder setList(String shape) {
        return shapeless();
    }

    public CraftingRecipeBuilder showNotification(boolean showNotification) {
        this.showNotification = showNotification;
        return this;
    }


    @Override
    public CraftingRecipeBuilder unlockedBy(ItemLike item) {
        return super.unlockedBy(item);
    }

    @Override
    public CraftingRecipeBuilder unlockedBy(TagKey<Item> tag) {
        return super.unlockedBy(tag);
    }

    @Override
    public CraftingRecipeBuilder unlockedBy(ItemLike... items) {
        return super.unlockedBy(items);
    }

    @Override
    public CraftingRecipeBuilder unlockedBy(ItemStack... stacks) {
        return super.unlockedBy(stacks);
    }

    @Override
    protected CraftingRecipeBuilder unlocks(String name, CriterionTriggerInstance trigger) {
        this.unlocks.put(name, trigger);
        return this;
    }

    @Override
    public CraftingRecipeBuilder setGroup(String group) {
        return super.setGroup(group);
    }


    @Override
    protected boolean checkRecipe() {
        if (shape != null) return checkShaped();
        else return checkShapeless();
    }

    @Override
    protected void buildRecipe(Consumer<FinishedRecipe> cc) {
        if (shape != null) buildShaped(cc);
        else buildShapeless(cc);
    }

    protected boolean checkShapeless() {
        if (materials.size() == 0) {
            BCLib.LOGGER.warning("Recipe {} does not contain a material!", id);
            return false;
        }
        return super.checkRecipe();
    }

    protected void buildShapeless(Consumer<FinishedRecipe> cc) {
        final ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.shapeless(
                category, output.getItem(), output.getCount()
        );
        for (Map.Entry<String, CriterionTriggerInstance> item : unlocks.entrySet()) {
            builder.unlockedBy(item.getKey(), item.getValue());
        }
        for (Map.Entry<Character, Ingredient> mat : materials.entrySet()) {
            builder.requires(mat.getValue());
        }

        builder.group(group);

        builder.save(cc, id);
    }

    protected boolean checkShaped() {
        if (shape == null || shape.length == 0) {
            BCLib.LOGGER.warning("Recipe {} does not contain a shape!", id);
            return false;
        }
        if (shape.length > 3) {
            BCLib.LOGGER.warning("Recipe {} shape contains more than three lines!", id);
            return false;
        }
        int width = shape[0].length();
        if (width > 3) {
            BCLib.LOGGER.warning("Recipe {} shape is wider than three!", id);
            return false;
        }
        String allLines = "";
        for (int i = 0; i < shape.length; i++) {
            if (shape[i].length() != width) {
                BCLib.LOGGER.warning("All lines in the shape of Recipe {} should be the same length!", id);
                return false;
            }
            allLines += shape[i];
        }
        allLines = allLines.replaceAll(" ", "");
        if (allLines.length() == 1) {
            BCLib.LOGGER.warning("Recipe {} only takes in a single item and should be shapeless", id);
            return false;
        }

        for (int i = 0; i < allLines.length(); i++) {
            char c = allLines.charAt(i);
            if (!materials.containsKey(c)) {
                BCLib.LOGGER.warning("Recipe {} is missing the material definition for '" + c + "'!", id);
                return false;
            }
        }

        return super.checkRecipe();
    }


    protected void buildShaped(Consumer<FinishedRecipe> cc) {
        final ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(
                category, output.getItem(), output.getCount()
        );
        for (Map.Entry<String, CriterionTriggerInstance> item : unlocks.entrySet()) {
            builder.unlockedBy(item.getKey(), item.getValue());
        }
        for (Map.Entry<Character, Ingredient> mat : materials.entrySet()) {
            builder.define(mat.getKey(), mat.getValue());
        }
        for (String row : shape) {
            builder.pattern(row);
        }
        builder.group(group);
        builder.showNotification(showNotification);
        builder.save(cc, id);
    }

}
