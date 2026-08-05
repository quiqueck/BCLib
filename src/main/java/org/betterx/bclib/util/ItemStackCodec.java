package org.betterx.bclib.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.function.BiFunction;
import java.util.function.Function;

public class ItemStackCodec {
    /**
     * Network stream codec for a bare {@link Item} reference (by registry id). Unlike
     * {@link ItemStack#STREAM_CODEC}, this never touches the item's bound DataComponents, so it's
     * safe to use anywhere - including recipe *building* (e.g. datagen), which runs before
     * DataComponents are bound onto registry Holders (that binding happens later, as part of
     * {@code ReloadableServerResources}' reload cycle).
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, Item> ITEM_STREAM_CODEC =
            ByteBufCodecs.registry(Registries.ITEM);

    /**
     * JSON codec for a bare (item, count) pair - same field names/shape as
     * {@link #codecItemStackWithNBT} (whose "nbt" field is elided whenever empty, which is the
     * common case), but without ever constructing an {@link ItemStack}. Recipes that don't need
     * per-instance NBT/component overrides on their result should use this and materialize a real
     * ItemStack lazily, at actual {@code assemble()}/craft time - not at recipe-building time, for
     * the same DataComponents-binding-order reason as {@link #ITEM_STREAM_CODEC} above.
     */
    public static <T> MapCodec<T> codecItemAndCount(
            Function<T, Item> itemGetter,
            Function<T, Integer> countGetter,
            BiFunction<Item, Integer, T> factory
    ) {
        return RecordCodecBuilder.mapCodec((instance) -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(itemGetter),
                Codec.INT.optionalFieldOf("count", 1).forGetter(countGetter)
        ).apply(instance, factory));
    }

    /** Convenience {@link #codecItemAndCount} instantiation for nesting under a single field. */
    public record ItemAndCount(Item item, int count) {
        public static final MapCodec<ItemAndCount> CODEC =
                codecItemAndCount(ItemAndCount::item, ItemAndCount::count, ItemAndCount::new);
    }

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
                                      .forGetter(o -> getter.apply(o).typeHolder()),
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
