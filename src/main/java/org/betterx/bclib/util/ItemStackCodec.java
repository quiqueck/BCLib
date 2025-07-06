package org.betterx.bclib.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.function.Function;

public class ItemStackCodec {
    private static DataComponentMap getComponents(ItemStack stack) {
        final DataComponentMap components = stack
                .getComponents()
                .filter((c) -> {
                    if (c == DataComponents.ENCHANTMENTS) {
                        final ItemEnchantments data = stack.get(DataComponents.ENCHANTMENTS);
                        return data != null && !data.isEmpty();
                    } else if (c == DataComponents.ATTRIBUTE_MODIFIERS) {
                        final ItemAttributeModifiers data = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
                        return data != null && !data.modifiers().isEmpty();
                    } else if (c == DataComponents.STORED_ENCHANTMENTS) {
                        final ItemEnchantments data = stack.get(DataComponents.STORED_ENCHANTMENTS);
                        return data != null && !data.isEmpty();
                    }
                    return false;
                });
        if (components.isEmpty()) return DataComponentMap.EMPTY;
        return components;
    }

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
                DataComponentMap.CODEC.optionalFieldOf("nbt", DataComponentMap.EMPTY)
                                      .forGetter(o -> getComponents(getter.apply(o)))
        ).apply(
                instance, (item, count, nbt) -> {
                    var stack = new ItemStack(item, count);
                    if (nbt != null) stack.applyComponents(nbt);
                    return factory.apply(stack);
                }
        ));
    }


    public static MapCodec<ItemStack> CODEC_ITEM_STACK_WITH_NBT = codecItemStackWithNBT(
            Function.identity(),
            Function.identity()
    );
}
