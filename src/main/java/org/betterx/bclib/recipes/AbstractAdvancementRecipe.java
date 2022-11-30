package org.betterx.bclib.recipes;

import org.betterx.bclib.api.v2.advancement.AdvancementManager;

import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.core.registries.BuiltInRegistries;
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

/**
 * Simple extension class that allows Recipe-builders to automatically generated Recipe-Avancements.
 * <p>
 * Implementing classes need to call {@link #createAdvancement(ResourceLocation, boolean)} or
 * {@link #createAdvancement(ResourceLocation, ItemLike)} to enable an advncement and
 * {@link #registerAdvancement(Recipe)} to finalize and register the new Advancement.
 * <p>
 * After that the unlockedBy-Methods can bes used to add Items that will unlock the Recipe (and prompt the unlock)
 */
public class AbstractAdvancementRecipe {
    protected AdvancementManager.Builder advancement;
    boolean hasUnlockTrigger = false;
    boolean generateAdvancement = false;

    /**
     * Your implementing class should call this method to prepare a new {@link AdvancementManager.Builder}
     * <p>
     * For Example {@link FurnaceRecipe} will call this in the
     * {@link FurnaceRecipe#make(ResourceLocation, ItemLike)}-Method
     *
     * @param id     {@link ResourceLocation} for this advancement
     * @param isTool true, if this is registered for a tool
     */
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


    /**
     * Your implementing class should call this method to prepare a new {@link AdvancementManager.Builder}
     * <p>
     * For Example {@link GridRecipe} will call this in the {@link GridRecipe#make(ResourceLocation, ItemLike)}-Method.
     * <p>
     * This method will call {@link #createAdvancement(ResourceLocation, boolean)}. The output object is used to
     * determine wether or not this is a tool recipe.
     *
     * @param id     {@link ResourceLocation} for this advancement
     * @param output Used to determine wether or not this is a tool recipe.
     */
    protected void createAdvancement(ResourceLocation id, ItemLike output) {
        createAdvancement(id, (output.asItem() instanceof TieredItem || output.asItem() instanceof ArmorItem));
    }

    private int nameCounter = 0;

    /**
     * The Recipe will be unlocked by one of the passed Items. As sonn als players have one in their Inventory
     * the recipe will unlock. Those Items are mostly the input Items for the recipe.
     * <p>
     * If you need to use other unlock-Criteria, you can get the {@link AdvancementManager.Builder}-Instance
     * using {@link #getAdvancementBuilder()}
     * <p>
     * This method will automatically get the Items from the stacl and call {@link #unlockedBy(ItemLike...)}
     *
     * @param stacks {@link ItemStack}s that will unlock the recipe. The count is ignored.
     */
    public void unlockedBy(ItemStack... stacks) {
        ItemLike[] items = Arrays.stream(stacks)
                                 .filter(stack -> stack.getCount() > 0)
                                 .map(stack -> (ItemLike) stack.getItem())
                                 .toArray(ItemLike[]::new);

        unlockedBy(items);
    }

    /**
     * The Recipe will be unlocked by one of the passed Items. As sonn als players have one in their Inventory
     * the recipe will unlock. Those Items are mostly the input Items for the recipe.
     * <p>
     * If you need to use other unlock-Criteria, you can get the {@link AdvancementManager.Builder}-Instance
     * using {@link #getAdvancementBuilder()}
     * <p>
     * This method will automatically derive a unique name for the criterion and call
     * {@link #unlockedBy(String, ItemLike...)}
     *
     * @param items {@link Item}s or {@link Block}s that will unlock the recipe.
     */
    public void unlockedBy(ItemLike... items) {
        String name = "has_" + (nameCounter++) + "_" +
                Arrays.stream(items)
                      .map(block -> (block instanceof Block)
                              ? BuiltInRegistries.BLOCK.getKey((Block) block)
                              : BuiltInRegistries.ITEM.getKey((Item) block))
                      .filter(id -> id != null)
                      .map(id -> id.getPath())
                      .collect(Collectors.joining("_"));
        if (name.length() > 45) name = name.substring(0, 42);
        unlockedBy(name, items);
    }

    /**
     * The Recipe will be unlocked by one of the passed Items. As sonn als players have one in their Inventory
     * the recipe will unlock. Those Items are mostly the input Items for the recipe.
     * <p>
     * If you need to use other unlock-Criteria, you can get the {@link AdvancementManager.Builder}-Instance
     * using {@link #getAdvancementBuilder()}
     * <p>
     * This method will automatically derive a unique name for the criterion and call
     * {@link #unlockedBy(String, TagKey)}
     *
     * @param tag All items from this Tag will unlock the recipe
     */
    public void unlockedBy(TagKey<Item> tag) {
        ResourceLocation id = tag.location();
        if (id != null) {
            unlockedBy("has_tag_" + id.getPath(), tag);
        }
    }


    /**
     * The Recipe will be unlocked by one of the passed Items. As sonn als players have one in their Inventory
     * the recipe will unlock. Those Items are mostly the input Items for the recipe.
     * <p>
     * If you need to use other unlock-Criteria, you can get the {@link AdvancementManager.Builder}-Instance
     * using {@link #getAdvancementBuilder()}
     *
     * @param name  The name for this unlock-Criteria
     * @param items {@link Item}s or {@link Block}s that will unlock the recipe.
     */
    public void unlockedBy(String name, ItemLike... items) {
        if (advancement != null) {
            hasUnlockTrigger = true;
            advancement.addInventoryChangedCriterion(name, items);
        }
    }

    /**
     * The Recipe will be unlocked by one of the passed Items. As sonn als players have one in their Inventory
     * the recipe will unlock. Those Items are mostly the input Items for the recipe.
     * <p>
     * If you need to use other unlock-Criteria, you can get the {@link AdvancementManager.Builder}-Instance
     * using {@link #getAdvancementBuilder()}
     *
     * @param name The name for this unlock-Criteria
     * @param tag  All items from this Tag will unlock the recipe
     */
    public void unlockedBy(String name, TagKey<Item> tag) {
        if (advancement != null) {
            hasUnlockTrigger = true;
            advancement.addInventoryChangedCriterion(name, tag);
        }
    }

    /**
     * Disables the generation of an advancement
     */
    public void noAdvancement() {
        generateAdvancement = false;
    }

    /**
     * Resets the advancement. This is usefully when you need more controll but the Recipe Builder
     * (like {@link GridRecipe} is automatically adding Items to the criterion list.
     */
    public void resetAdvancement() {
        if (advancement != null)
            advancement = AdvancementManager.Builder.createEmptyCopy(advancement);
    }

    /**
     * Returns the underlying Builder Instance
     *
     * @return null or The underlying builder Instance
     */
    public AdvancementManager.Builder getAdvancementBuilder() {
        return advancement;
    }


    /**
     * Your implementing class should calls this method to finalize the advancement and register it.
     * <p>
     * For Example {@link GridRecipe} will call this in the {@link GridRecipe#build()}-Method
     *
     * @param recipe The generated recipe that need to be linked to the Advancement
     */
    protected void registerAdvancement(Recipe<?> recipe) {
        if (hasUnlockTrigger && generateAdvancement && advancement != null) {
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
