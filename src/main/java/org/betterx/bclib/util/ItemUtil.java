package org.betterx.bclib.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ItemUtil {
    public static <T> MapCodec<T> codecItemStackWithNBT(
            Function<T, ItemStack> getter,
            Function<ItemStack, T> factory
    ) {
        return RecordCodecBuilder.mapCodec((instance) -> instance.group(
                BuiltInRegistries.ITEM.holderByNameCodec()
                                      .fieldOf("item")
                                      .forGetter(o -> getter.apply(o).getItemHolder()),
                Codec.INT.optionalFieldOf("count", 1)
                         .forGetter(o -> getter.apply(o).getCount()),
                DataComponentMap.CODEC.optionalFieldOf("nbt")
                                      .forGetter(o -> Optional.ofNullable(getter.apply(o).getComponents()))
        ).apply(instance, (item, count, nbt) -> {
            var stack = new ItemStack(item, count);
            if (nbt.isPresent()) stack.applyComponents(nbt.get());
            return factory.apply(stack);
        }));
    }


    public static MapCodec<ItemStack> CODEC_ITEM_STACK_WITH_NBT = codecItemStackWithNBT(
            Function.identity(),
            Function.identity()
    );

    private static final Codec<Ingredient.ItemValue> ITEM_VALUE_CODEC = RecordCodecBuilder.create((instance) -> instance
            .group(CODEC_ITEM_STACK_WITH_NBT.fieldOf("item").forGetter(Ingredient.ItemValue::item))
            .apply(instance, Ingredient.ItemValue::new));

    public static final Codec<Ingredient.TagValue> TAG_VALUE_CODEC = RecordCodecBuilder.create((instance) -> instance
            .group(TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(Ingredient.TagValue::tag))
            .apply(instance, Ingredient.TagValue::new));

    private static final Codec<Ingredient.Value> VALUE_CODEC = Codec
            .xor(ITEM_VALUE_CODEC, TAG_VALUE_CODEC)
            .xmap(
                    (either) -> either.map((itemValue) -> itemValue, (tagValue) -> tagValue),
                    (value) -> {
                        if (value instanceof Ingredient.TagValue tagValue) {
                            return Either.right(tagValue);
                        } else if (value instanceof Ingredient.ItemValue itemValue) {
                            return Either.left(itemValue);
                        } else {
                            throw new UnsupportedOperationException("This is neither an item value nor a tag value.");
                        }
                    }
            );

    public static Codec<Ingredient> codecIngredientWithNBT(boolean lenient) {
        Codec<Ingredient.Value[]> codec = Codec
                .list(VALUE_CODEC)
                .comapFlatMap((list) -> !lenient && list.isEmpty()
                        ? DataResult.error(() -> "Item array cannot be empty, at least one item must be defined")
                        : DataResult.success(list.toArray(new Ingredient.Value[0])), List::of);
        return Codec
                .either(codec, VALUE_CODEC)
                .flatComapMap((either) -> either.map(
                        Ingredient::new,
                        (value) -> new Ingredient(new Ingredient.Value[]{value})
                ), (ingredient) -> {
                    if (ingredient.values.length == 1) {
                        return DataResult.success(Either.right(ingredient.values[0]));
                    } else {
                        return ingredient.values.length == 0 && !lenient
                                ? DataResult.error(() -> "Item array cannot be empty, at least one item must be defined")
                                : DataResult.success(Either.left(ingredient.values));
                    }
                });
    }

    public static Codec<Ingredient> CODEC_INGREDIENT_WITH_NBT = codecIngredientWithNBT(false);
    public static Codec<Ingredient> CODEC_LENIENT_INGREDIENT_WITH_NBT = codecIngredientWithNBT(true);

}
