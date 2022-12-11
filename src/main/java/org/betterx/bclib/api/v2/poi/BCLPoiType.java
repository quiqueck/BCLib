package org.betterx.bclib.api.v2.poi;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
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

    public BCLPoiType(
            ResourceKey<PoiType> key,
            PoiType type,
            Set<BlockState> matchingStates,
            int maxTickets,
            int validRange
    ) {
        this.key = key;
        this.type = type;
        this.matchingStates = matchingStates;
        this.maxTickets = maxTickets;
        this.validRange = validRange;
    }

    public static Set<BlockState> getBlockStates(Block block) {
        return ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates());
    }

    public void setTag(TagKey<Block> tag) {
        org.betterx.bclib.api.v2.poi.PoiManager.setTag(key, tag);
    }

    public Optional<BlockPos> findPoiAround(
            ServerLevel level,
            BlockPos center,
            boolean wideSearch,
            WorldBorder worldBorder
    ) {
        return findPoiAround(key, level, center, wideSearch, worldBorder);
    }

    public Optional<BlockPos> findClosest(
            ServerLevel level,
            BlockPos center,
            int radius
    ) {
        return level.getPoiManager().findClosest(
                holder -> holder.is(this.key),
                (pos) -> true,
                center,
                radius,
                PoiManager.Occupancy.ANY
        );
    }

    public Optional<BlockPos> findPoiAround(
            ServerLevel level,
            BlockPos center,
            int radius,
            WorldBorder worldBorder
    ) {
        return findPoiAround(key, level, center, radius, worldBorder);
    }

    public static Optional<BlockPos> findPoiAround(
            ResourceKey<PoiType> key,
            ServerLevel level,
            BlockPos center,
            boolean wideSearch,
            WorldBorder worldBorder
    ) {
        return findPoiAround(key, level, center, wideSearch ? 16 : 128, worldBorder);
    }

    public static Optional<BlockPos> findPoiAround(
            ResourceKey<PoiType> key,
            ServerLevel level,
            BlockPos center,
            int radius,
            WorldBorder worldBorder
    ) {
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
