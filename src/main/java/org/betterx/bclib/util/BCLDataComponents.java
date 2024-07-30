package org.betterx.bclib.util;

import org.betterx.bclib.BCLib;
import org.betterx.wover.data_components.DataComponentManager;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.component.CustomData;

import org.jetbrains.annotations.ApiStatus;

public class BCLDataComponents {
    public static final DataComponentType<CustomData> ANVIL_ENTITY_DATA = DataComponentManager.registerDataComponent(
            BCLib.makeID("anvil_entity_data"),
            (DataComponentType.Builder<CustomData> builder) -> builder
                    .persistent(CustomData.CODEC)
                    .networkSynchronized(CustomData.STREAM_CODEC)
    );

    @ApiStatus.Internal
    public static void ensureStaticInitialization() {
    }
}
