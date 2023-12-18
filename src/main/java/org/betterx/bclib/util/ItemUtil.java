package org.betterx.bclib.util;

import org.betterx.bclib.BCLib;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public class ItemUtil {
    @Nullable
    public static ItemStack fromStackString(String stackString) {
        if (stackString == null || stackString.isEmpty()) {
            return null;
        }
        try {
            String[] parts = stackString.split(":");
            if (parts.length < 2) return null;
            if (parts.length == 2) {
                ResourceLocation itemId = new ResourceLocation(stackString);
                Item item = BuiltInRegistries
                        .ITEM
                        .getOptional(itemId)
                        .orElseThrow(() -> new IllegalStateException("Output item " + itemId + " does not exists!"));
                return new ItemStack(item);
            }
            ResourceLocation itemId = new ResourceLocation(parts[0], parts[1]);
            Item item = BuiltInRegistries
                    .ITEM
                    .getOptional(itemId)
                    .orElseThrow(() -> new IllegalStateException("Output item " + itemId + " does not exists!"));
            return new ItemStack(item, Integer.parseInt(parts[2]));
        } catch (Exception ex) {
            BCLib.LOGGER.error("ItemStack deserialization error!", ex);
        }
        return null;
    }

    public static Codec<ItemStack> CODEC_ITEM_STACK_WITH_NBT = RecordCodecBuilder.create((instance) -> instance.group(
            BuiltInRegistries.ITEM.holderByNameCodec()
                                  .fieldOf("item")
                                  .forGetter(ItemStack::getItemHolder),
            Codec.INT.optionalFieldOf("count", 1)
                     .forGetter(ItemStack::getCount),
            ExtraCodecs.strictOptionalField(TagParser.AS_CODEC, "nbt")
                       .forGetter((itemStack) -> Optional.ofNullable(itemStack.getTag()))
    ).apply(instance, ItemStack::new));

    public static Codec<Ingredient> CODEC_INGREDIENT_WITH_NBT = ingredientCodec(true);
    public static Codec<Ingredient> CODEC_INGREDIENT_WITH_NBT_NOT_EMPTY = ingredientCodec(false);


    private static Codec<Ingredient> ingredientCodec(boolean allowEmpty) {
        record NbtItemValue(ItemStack item) implements Ingredient.Value {
            static final Codec<NbtItemValue> CODEC = RecordCodecBuilder.create((instance) -> instance
                    .group(CODEC_ITEM_STACK_WITH_NBT.fieldOf("item").forGetter((itemValue) -> itemValue.item))
                    .apply(instance, NbtItemValue::new));

            public boolean equals(Object object) {
                if (object instanceof NbtItemValue itemValue) {
                    return ItemStack.isSameItemSameTags(itemValue.item, this.item)
                            && itemValue.item.getCount() == this.item.getCount();
                } else if (object instanceof Ingredient.ItemValue itemValue) {
                    return ItemStack.isSameItemSameTags(itemValue.item(), this.item)
                            && itemValue.item().getCount() == this.item.getCount();
                } else {
                    return false;
                }
            }

            public Collection<ItemStack> getItems() {
                return Collections.singleton(this.item);
            }

            public ItemStack item() {
                return this.item;
            }
        }

        Codec<Ingredient.Value> VALUE_CODEC = ExtraCodecs
                .xor(NbtItemValue.CODEC, Ingredient.TagValue.CODEC)
                .xmap(
                        (either) -> either.map((itemValue) -> itemValue, (tagValue) -> tagValue),
                        (value) -> {
                            if (value instanceof Ingredient.TagValue tagValue) {
                                return Either.right(tagValue);
                            } else if (value instanceof NbtItemValue itemValue) {
                                return Either.left(itemValue);
                            } else {
                                throw new UnsupportedOperationException(
                                        "This is neither an nbt-item value nor a tag value.");
                            }
                        }
                );


        Codec<Ingredient.Value[]> codec = Codec.list(VALUE_CODEC).comapFlatMap((list) ->
                        !allowEmpty && list.size() < 1
                                ? DataResult.error(() -> "Item array cannot be empty, at least one item must be defined")
                                : DataResult.success(list.toArray(new Ingredient.Value[0]))
                , List::of);

        return ExtraCodecs.either(codec, VALUE_CODEC).flatComapMap(
                (either) -> either.map(Ingredient::new, (value) -> new Ingredient(new Ingredient.Value[]{value})),
                (ingredient) -> {
                    if (ingredient.values.length == 1) {
                        return DataResult.success(Either.right(ingredient.values[0]));
                    } else {
                        return ingredient.values.length == 0 && !allowEmpty
                                ? DataResult.error(() -> "Item array cannot be empty, at least one item must be defined")
                                : DataResult.success(Either.left(ingredient.values));
                    }
                }
        );
    }
}
