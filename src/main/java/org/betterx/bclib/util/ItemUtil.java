package org.betterx.bclib.util;

import org.betterx.bclib.BCLib;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemUtil {

    public static String toStackString(@NotNull ItemStack stack) {
        try {
            if (stack == null) {
                throw new IllegalStateException("Stack can't be null!");
            }
            Item item = stack.getItem();
            return BuiltInRegistries.ITEM.getKey(item) + ":" + stack.getCount();
        } catch (Exception ex) {
            BCLib.LOGGER.error("ItemStack serialization error!", ex);
        }
        return "";
    }

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
            Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElseThrow(() -> {
                return new IllegalStateException("Output item " + itemId + " does not exists!");
            });
            return new ItemStack(item, Integer.valueOf(parts[2]));
        } catch (Exception ex) {
            BCLib.LOGGER.error("ItemStack deserialization error!", ex);
        }
        return null;
    }

    public static ItemStack fromJsonRecipeWithNBT(JsonObject recipe) {
        ItemStack output = ItemUtil.fromJsonRecipe(recipe);
        if (output != null && recipe.has("nbt")) {
            try {
                String nbtData = GsonHelper.getAsString(recipe, "nbt");
                CompoundTag nbt = TagParser.parseTag(nbtData);
                output.setTag(nbt);
            } catch (CommandSyntaxException ex) {
                BCLib.LOGGER.warning("Error parse nbt data for output.", ex);
            }
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


    public static JsonObject toJsonRecipeWithNBT(ItemStack stack) {
        return toJsonRecipeWithNBT(stack.getItem(), stack.getCount(), stack.getTag());
    }

    public static JsonObject toJsonRecipeWithNBT(ItemLike item, int count, CompoundTag nbt) {
        JsonObject root = toJsonRecipe(item, count);
        if (nbt != null) {
            final String nbtData = NbtUtils.prettyPrint(nbt);
            root.addProperty("nbt", nbtData);
            //TODO: just for testing
            try {
                TagParser.parseTag(nbtData);
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        }
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
