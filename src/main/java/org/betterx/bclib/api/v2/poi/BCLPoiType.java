package org.betterx.bclib.api.v2.poi;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;

import com.google.common.collect.ImmutableSet;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

public class BCLPoiType {
    public final ResourceKey<PoiType> key;
    public final PoiType type;
    public final Set<BlockState> matchingStates;
    public final int maxTickets;
    public final int validRange;

    public BCLPoiType(ResourceKey<PoiType> key,
                      PoiType type,
                      Set<BlockState> matchingStates,
                      int maxTickets,
                      int validRange) {
        this.key = key;
        this.type = type;
        this.matchingStates = matchingStates;
        this.maxTickets = maxTickets;
        this.validRange = validRange;
    }

    public static BCLPoiType register(ResourceLocation location,
                                      Set<BlockState> matchingStates,
                                      int maxTickets,
                                      int validRanges) {
        ResourceKey<PoiType> key = ResourceKey.create(Registry.POINT_OF_INTEREST_TYPE_REGISTRY, location);
        PoiType type = PoiTypes.register(Registry.POINT_OF_INTEREST_TYPE, key, matchingStates, maxTickets, validRanges);
        return new BCLPoiType(key, type, matchingStates, maxTickets, validRanges);
    }

    public static Set<BlockState> getBlockStates(Block block) {
        return ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates());
    }

    public Optional<BlockPos> findPoiAround(ServerLevel level,
                                            BlockPos center,
                                            boolean wideSearch,
                                            WorldBorder worldBorder) {
        return findPoiAround(key, level, center, wideSearch, worldBorder);
    }

    public Optional<BlockPos> findPoiAround(ServerLevel level,
                                            BlockPos center,
                                            int radius,
                                            WorldBorder worldBorder) {
        return findPoiAround(key, level, center, radius, worldBorder);
    }

    public static Optional<BlockPos> findPoiAround(
            ResourceKey<PoiType> key,
            ServerLevel level,
            BlockPos center,
            boolean wideSearch,
            WorldBorder worldBorder) {
        return findPoiAround(key, level, center, wideSearch ? 16 : 128, worldBorder);
    }

    public static Optional<BlockPos> findPoiAround(
            ResourceKey<PoiType> key,
            ServerLevel level,
            BlockPos center,
            int radius,
            WorldBorder worldBorder) {
        PoiManager poiManager = level.getPoiManager();

        poiManager.ensureLoadedAndValid(level, center, radius);
        Optional<PoiRecord> record = poiManager
                .getInSquare(holder -> holder.is(key), center, radius, PoiManager.Occupancy.ANY)
                .filter(poiRecord -> worldBorder.isWithinBounds(poiRecord.getPos()))
                .sorted(Comparator.<PoiRecord>comparingDouble(poiRecord -> poiRecord.getPos().distSqr(center))
                                  .thenComparingInt(poiRecord -> poiRecord.getPos().getY()))
                .filter(poiRecord -> level.getBlockState(poiRecord.getPos())
                                          .hasProperty(BlockStateProperties.HORIZONTAL_AXIS))
                .findFirst();

        return record.map(poiRecord -> poiRecord.getPos());
    }
}
