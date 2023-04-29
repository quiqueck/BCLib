package org.betterx.bclib.util;

import org.betterx.bclib.BCLib;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

public class ItemUtil {
    @Nullable
    public static ItemStack fromStackString(String stackString) {
        if (stackString == null || stackString.equals("")) {
            return null;
        }
        try {
            String[] parts = stackString.split(":");
            if (parts.length < 2) return null;
            if (parts.length == 2) {
                ResourceLocation itemId = new ResourceLocation(stackString);
                Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElseThrow(() -> {
                    return new IllegalStateException("Output item " + itemId + " does not exists!");
                });
                return new ItemStack(item);
            }
            ResourceLocation itemId = new ResourceLocation(parts[0], parts[1]);
            Item item = BuiltInRegistries.ITEM.getOptional(itemId)
                                              .orElseThrow(() -> new IllegalStateException("Output item " + itemId + " does not exists!"));
            return new ItemStack(item, Integer.valueOf(parts[2]));
        } catch (Exception ex) {
            BCLib.LOGGER.error("ItemStack deserialization error!", ex);
        }
        return null;
    }

    public static CompoundTag readNBT(JsonObject recipe) {
        if (recipe.has("nbt")) {
            try {
                String nbtData = GsonHelper.getAsString(recipe, "nbt");
                CompoundTag nbt = TagParser.parseTag(nbtData);
                return nbt;
            } catch (CommandSyntaxException ex) {
                BCLib.LOGGER.warning("Error parsing nbt data for output.", ex);
            }
        }
        return null;
    }

    public static void writeNBT(JsonObject root, CompoundTag nbt) {
        if (nbt != null) {
            final String nbtData = nbt.toString();
            root.addProperty("nbt", nbtData);
        }
    }

    public static Ingredient fromJsonIngredientWithNBT(JsonObject ingredient) {
        Ingredient ing = Ingredient.fromJson(ingredient);
        CompoundTag nbt = readNBT(ingredient);
        if (nbt != null && !ing.isEmpty()) {
            ing.getItems()[0].setTag(nbt);
        }
        return ing;
    }

    public static ItemStack fromJsonRecipeWithNBT(JsonObject recipe) {
        ItemStack output = ItemUtil.fromJsonRecipe(recipe);
        CompoundTag nbt = ItemUtil.readNBT(recipe);
        if (output != null && nbt != null) {
            output.setTag(nbt);
        }
        return output;
    }

    @Nullable
    public static ItemStack fromJsonRecipe(JsonObject recipe) {
        try {
            if (!recipe.has("item")) {
                throw new IllegalStateException("Invalid JsonObject. Entry 'item' does not exists!");
            }
            ResourceLocation itemId = new ResourceLocation(GsonHelper.getAsString(recipe, "item"));
            Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElseThrow(() -> {
                return new IllegalStateException("Output item " + itemId + " does not exists!");
            });
            int count = GsonHelper.getAsInt(recipe, "count", 1);
            return new ItemStack(item, count);
        } catch (Exception ex) {
            BCLib.LOGGER.error("ItemStack deserialization error!", ex);
        }
        return null;
    }

    public static JsonElement toJsonIngredientWithNBT(Ingredient ing) {
        JsonElement el = ing.toJson();
        if (el.isJsonObject() && !ing.isEmpty() && ing.getItems()[0].hasTag()) {
            JsonObject obj = el.getAsJsonObject();
            writeNBT(obj, ing.getItems()[0].getTag());
        }
        return el;
    }


    public static JsonObject toJsonRecipeWithNBT(ItemStack stack) {
        return toJsonRecipeWithNBT(stack.getItem(), stack.getCount(), stack.getTag());
    }

    public static JsonObject toJsonRecipeWithNBT(ItemLike item, int count, CompoundTag nbt) {
        JsonObject root = toJsonRecipe(item, count);
        writeNBT(root, nbt);
        return root;
    }

    public static JsonObject toJsonRecipe(ItemStack stack) {
        return toJsonRecipe(stack.getItem(), stack.getCount());
    }

    public static JsonObject toJsonRecipe(ItemLike item, int count) {
        final ResourceLocation id = BuiltInRegistries.ITEM.getKey(item.asItem());
        if (id == null) {
            throw new IllegalStateException("Unknown Item " + item);
        }

        final JsonObject root = new JsonObject();
        root.addProperty("item", BuiltInRegistries.ITEM.getKey(item.asItem()).toString());
        if (count > 1) {
            root.addProperty("count", count);
        }
        return root;
    }
}
