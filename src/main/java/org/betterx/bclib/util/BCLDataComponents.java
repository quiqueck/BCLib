package org.betterx.bclib.util;

import org.betterx.bclib.BCLib;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;

import java.util.function.UnaryOperator;

public class BCLDataComponents {
    public static final DataComponentType<CustomData> ANVIL_ENTITY_DATA = register(
            BCLib.makeID("anvil_entity_data"),
            (DataComponentType.Builder<CustomData> builder) -> builder
                    .persistent(CustomData.CODEC)
                    .networkSynchronized(CustomData.STREAM_CODEC)
    );

    public static <T> DataComponentType register(
            ResourceLocation id,
            UnaryOperator<DataComponentType.Builder<T>> builder
    ) {
        return Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                id,
                builder.apply(DataComponentType.builder()).build()
        );
    }
}
