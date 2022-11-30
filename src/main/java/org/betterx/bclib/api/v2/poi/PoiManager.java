package org.betterx.bclib.api.v2.poi;

import org.betterx.bclib.api.v2.levelgen.biomes.InternalBiomeAPI;
import org.betterx.worlds.together.tag.v3.CommonPoiTags;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import org.jetbrains.annotations.ApiStatus;

public class PoiManager {
    public static BCLPoiType register(
            ResourceLocation location,
            Set<BlockState> matchingStates,
            int maxTickets,
            int validRanges
    ) {
        ResourceKey<PoiType> key = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, location);
        PoiType type = PoiTypes.register(
                BuiltInRegistries.POINT_OF_INTEREST_TYPE,
                key,
                matchingStates,
                maxTickets,
                validRanges
        );
        return new BCLPoiType(key, type, matchingStates, maxTickets, validRanges);
    }

    public static void setTag(ResourceKey<PoiType> type, TagKey<Block> tag) {
        var oHolder = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getHolder(type);
        if (oHolder.isPresent()) {
            setTag(oHolder.get().value(), tag);
            didAddTagFor(oHolder.get(), tag);
        }
    }

    private static void setTag(PoiType type, TagKey<Block> tag) {
        if ((Object) type instanceof PoiTypeExtension ext) {
            ext.bcl_setTag(tag);
        }
    }

    @ApiStatus.Internal
    public static void registerAll() {
        PoiManager.setTag(PoiTypes.FISHERMAN, CommonPoiTags.FISHERMAN_WORKSTATION);
        PoiManager.setTag(PoiTypes.FARMER, CommonPoiTags.FARMER_WORKSTATION);
    }


    private static final List<Holder<PoiType>> TYPES_WITH_TAGS = new ArrayList<>(4);
    private static Map<BlockState, Holder<PoiType>> ORIGINAL_BLOCK_STATES = null;

    private static void didAddTagFor(Holder<PoiType> type, TagKey<Block> tag) {
        TYPES_WITH_TAGS.remove(type);
        if (tag != null) TYPES_WITH_TAGS.add(type);
    }


    @ApiStatus.Internal
    public static void updateStates() {
        if (ORIGINAL_BLOCK_STATES == null) {
            //We have not yet tainted the original states, so we will create a copy now
            ORIGINAL_BLOCK_STATES = new HashMap<>(PoiTypes.TYPE_BY_STATE);
        } else {
            //restore unaltered state
            PoiTypes.TYPE_BY_STATE.clear();
            PoiTypes.TYPE_BY_STATE.putAll(ORIGINAL_BLOCK_STATES);
        }

        for (Holder<PoiType> type : TYPES_WITH_TAGS) {
            if ((Object) type.value() instanceof PoiTypeExtension ex) {
                TagKey<Block> tag = ex.bcl_getTag();
                if (tag != null) {
                    var registry = InternalBiomeAPI.worldRegistryAccess().registryOrThrow(tag.registry());
                    for (var block : registry.getTagOrEmpty(tag)) {
                        for (var state : block.value().getStateDefinition().getPossibleStates()) {
                            PoiTypes.TYPE_BY_STATE.put(state, type);
                        }
                    }
                }
            }
        }
    }
}
