package org.betterx.bclib.api.v2.poi;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class PoiRegistry {
    @FunctionalInterface
    public interface OnBootstrap {
        void run();
    }

    private final static List<BCLPoiType> KNOWN_TYPES = new ArrayList<>(4);
    private final static List<OnBootstrap> KNOW = new ArrayList<>(4);

    public static void registerForBootstrap(OnBootstrap callback) {
        KNOW.add(callback);
    }

    public static BCLPoiType register(ResourceLocation location,
                                      Supplier<Set<BlockState>> supplier,
                                      int maxTickets,
                                      int validRange) {
        ResourceKey<PoiType> key = ResourceKey.create(Registry.POINT_OF_INTEREST_TYPE_REGISTRY, location);

        BCLPoiType type = new BCLPoiType(key, supplier, maxTickets, validRange);
        KNOWN_TYPES.add(type);
        return type;
    }

    public static List<BCLPoiType> getCustomPOIs() {
        for (OnBootstrap bootstrap : KNOW) bootstrap.run();
        return ImmutableList.copyOf(KNOWN_TYPES);
    }
}
