package org.betterx.bclib.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

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
}
