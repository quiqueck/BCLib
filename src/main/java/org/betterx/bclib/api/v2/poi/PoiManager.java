package org.betterx.bclib.api.v2.poi;

import org.betterx.worlds.together.tag.v3.CommonPoiTags;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import org.jetbrains.annotations.ApiStatus;

public class PoiManager {
    public static BCLPoiType register(
            ResourceLocation location,
            Set<BlockState> matchingStates,
            int maxTickets,
            int validRanges
    ) {
        ResourceKey<PoiType> key = ResourceKey.create(Registry.POINT_OF_INTEREST_TYPE_REGISTRY, location);
        PoiType type = PoiTypes.register(Registry.POINT_OF_INTEREST_TYPE, key, matchingStates, maxTickets, validRanges);
        return new BCLPoiType(key, type, matchingStates, maxTickets, validRanges);
    }

    public static void setTag(ResourceKey<PoiType> type, TagKey<Block> tag) {
        setTag(Registry.POINT_OF_INTEREST_TYPE.get(type), tag);
    }

    public static void setTag(PoiType type, TagKey<Block> tag) {
        if ((Object) type instanceof PoiTypeExtension ext) {
            ext.bcl_setTag(tag);
        }
    }

    @ApiStatus.Internal
    public static void registerAll() {
        PoiManager.setTag(PoiTypes.FISHERMAN, CommonPoiTags.FISHERMAN_WORKSTATION);
    }
}
