package org.betterx.bclib.recipes;

import org.betterx.bclib.api.v2.advancement.AdvancementManager;

import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.stream.Collectors;

public class AbstractAdvancementRecipe {
    protected AdvancementManager.Builder advancement;
    boolean hasUnlockTrigger = false;
    boolean generateAdvancement = false;

    protected void createAdvancement(ResourceLocation id, boolean isTool) {
        hasUnlockTrigger = false;
        generateAdvancement = true;
        advancement = AdvancementManager.Builder.create(
                id,
                isTool
                        ? AdvancementManager.AdvancementType.RECIPE_TOOL
                        : AdvancementManager.AdvancementType.RECIPE_DECORATIONS
        );
    }

    protected void createAdvancement(ResourceLocation id, ItemLike output) {
        createAdvancement(id, (output.asItem() instanceof TieredItem || output.asItem() instanceof ArmorItem));
    }

    private int nameCounter = 0;

    public void unlockedBy(ItemStack... stacks) {
        ItemLike[] items = Arrays.stream(stacks)
                                 .filter(stack -> stack.getCount() > 0)
                                 .map(stack -> (ItemLike) stack.getItem())
                                 .toArray(ItemLike[]::new);

        unlockedBy(items);
    }

    public void unlockedBy(ItemLike... items) {
        String name = "has_" + (nameCounter++) + "_" +
                Arrays.stream(items)
                      .map(block -> (block instanceof Block)
                              ? Registry.BLOCK.getKey((Block) block)
                              : Registry.ITEM.getKey((Item) block))
                      .filter(id -> id != null)
                      .map(id -> id.getPath())
                      .collect(Collectors.joining("_"));
        if (name.length() > 45) name = name.substring(0, 42);
        unlockedBy(name, items);
    }

    public void unlockedBy(TagKey<Item> tag) {
        ResourceLocation id = tag.location();
        if (id != null) {
            unlockedBy("has_tag_" + id.getPath(), tag);
        }
    }

    public void unlockedBy(String name, ItemLike... items) {
        hasUnlockTrigger = true;
        advancement.addInventoryChangedCriterion(name, items);
    }

    public void unlockedBy(String name, TagKey<Item> tag) {
        hasUnlockTrigger = true;
        advancement.addInventoryChangedCriterion(name, tag);
    }

    public void noAdvancement() {
        generateAdvancement = false;
    }

    public void clearAdvancementCriteria() {
        if (advancement != null)
            advancement = AdvancementManager.Builder.createEmptyCopy(advancement);
    }

    protected void registerAdvancement(Recipe<?> recipe) {
        if (hasUnlockTrigger && generateAdvancement) {
            advancement
                    .startDisplay(recipe.getResultItem().getItem())
                    .hideFromChat()
                    .hideToast()
                    .endDisplay()
                    .addRecipeUnlockCriterion("has_the_recipe", recipe)
                    .startReward()
                    .addRecipe(recipe.getId())
                    .endReward()
                    .requirements(RequirementsStrategy.OR);

            advancement.buildAndRegister();
        }
    }
}
