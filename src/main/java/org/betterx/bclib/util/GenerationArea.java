package org.betterx.bclib.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * The blocks a feature may touch while it is placed during world generation: the chunk it is placed in and the
 * eight chunks around it (the write area of vanilla's {@link WorldGenRegion}). Blocks further away belong to chunks
 * other worldgen threads may be working on at the same time (C2ME only locks this 3x3 area), so features must neither
 * read nor write them.
 * <p>
 * Outside of world generation (e.g. a sapling growing in a live level) every position is inside the area.
 * <p>
 * The helpers treat positions outside the area as air (no fluid) and clamp height lookups to the nearest column
 * inside the area, so callers get a deterministic answer instead of whatever another thread is generating there.
 */
public final class GenerationArea {
    /** Chunk radius around the center chunk a feature may access (vanilla's write radius for features). */
    public static final int CHUNK_RADIUS = 1;
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    /** {@link LevelReader#getBiome(BlockPos)} blends the biomes of the quarts up to 5 blocks around the position. */
    private static final int BIOME_BLEND_MARGIN = 6;

    private GenerationArea() {
    }

    public static boolean isRestricted(LevelReader level) {
        return level instanceof WorldGenRegion;
    }

    public static boolean contains(LevelReader level, BlockPos pos) {
        return contains(level, pos.getX(), pos.getZ());
    }

    public static boolean contains(LevelReader level, int x, int z) {
        if (level instanceof WorldGenRegion region) {
            final ChunkPos center = region.getCenter();
            return Math.abs(SectionPos.blockToSectionCoord(x) - center.x) <= CHUNK_RADIUS
                    && Math.abs(SectionPos.blockToSectionCoord(z) - center.z) <= CHUNK_RADIUS;
        }
        return true;
    }

    /**
     * Like {@link #contains(LevelReader, BlockPos)}, but the position must be at least {@code margin} blocks away
     * from the border of the area (so the neighbours of the position can be read as well).
     */
    public static boolean contains(LevelReader level, BlockPos pos, int margin) {
        return contains(level, pos.getX(), pos.getZ(), margin);
    }

    public static boolean contains(LevelReader level, int x, int z, int margin) {
        return x >= minX(level) + margin && x <= maxX(level) - margin
                && z >= minZ(level) + margin && z <= maxZ(level) - margin;
    }

    public static int minX(LevelReader level) {
        if (level instanceof WorldGenRegion region) {
            return SectionPos.sectionToBlockCoord(region.getCenter().x - CHUNK_RADIUS);
        }
        return Integer.MIN_VALUE / 2;
    }

    public static int maxX(LevelReader level) {
        if (level instanceof WorldGenRegion region) {
            return SectionPos.sectionToBlockCoord(region.getCenter().x + CHUNK_RADIUS, 15);
        }
        return Integer.MAX_VALUE / 2;
    }

    public static int minZ(LevelReader level) {
        if (level instanceof WorldGenRegion region) {
            return SectionPos.sectionToBlockCoord(region.getCenter().z - CHUNK_RADIUS);
        }
        return Integer.MIN_VALUE / 2;
    }

    public static int maxZ(LevelReader level) {
        if (level instanceof WorldGenRegion region) {
            return SectionPos.sectionToBlockCoord(region.getCenter().z + CHUNK_RADIUS, 15);
        }
        return Integer.MAX_VALUE / 2;
    }

    public static int clampX(LevelReader level, int x) {
        return Math.max(minX(level), Math.min(maxX(level), x));
    }

    public static int clampZ(LevelReader level, int z) {
        return Math.max(minZ(level), Math.min(maxZ(level), z));
    }

    /** The position moved horizontally to the nearest column inside the area (unchanged if it is inside). */
    public static BlockPos clamp(LevelReader level, BlockPos pos) {
        if (contains(level, pos)) return pos;
        return new BlockPos(clampX(level, pos.getX()), pos.getY(), clampZ(level, pos.getZ()));
    }

    /** The block at the position, or air if the position is outside the area. */
    public static BlockState getBlockState(LevelReader level, BlockPos pos) {
        return contains(level, pos) ? level.getBlockState(pos) : AIR;
    }

    /** The fluid at the position, or no fluid if the position is outside the area. */
    public static FluidState getFluidState(LevelReader level, BlockPos pos) {
        return contains(level, pos) ? level.getFluidState(pos) : Fluids.EMPTY.defaultFluidState();
    }

    /** {@code true} if the position is air, or outside the area. */
    public static boolean isEmptyBlock(LevelReader level, BlockPos pos) {
        return !contains(level, pos) || level.isEmptyBlock(pos);
    }

    /** The heightmap value of the column, or of the nearest column inside the area. */
    public static int getHeight(LevelReader level, Heightmap.Types type, int x, int z) {
        return level.getHeight(type, clampX(level, x), clampZ(level, z));
    }

    /**
     * The biome at the position, blended like {@link LevelReader#getBiome(BlockPos)}. Where the blending looks at
     * quarts outside of the area, it uses the biome the biome source generates there (what the chunk stores after its
     * biome step) instead of reading the chunk.
     */
    public static Holder<Biome> getBiome(LevelReader level, BlockPos pos) {
        if (level instanceof WorldGenRegion region && !contains(level, pos, BIOME_BLEND_MARGIN)) {
            final ServerLevel serverLevel = region.getLevel();
            final BiomeSource source = serverLevel.getChunkSource().getGenerator().getBiomeSource();
            final Climate.Sampler sampler = serverLevel.getChunkSource().randomState().sampler();
            final int minQuartY = QuartPos.fromBlock(region.getMinBuildHeight());
            final int maxQuartY = minQuartY + QuartPos.fromBlock(region.getHeight()) - 1;
            return region.getBiomeManager().withDifferentSource((qx, qy, qz) -> {
                if (contains(level, QuartPos.toBlock(qx), QuartPos.toBlock(qz))) {
                    return region.getNoiseBiome(qx, qy, qz);
                }
                // same y clamping as ChunkAccess.getNoiseBiome
                return source.getNoiseBiome(qx, Mth.clamp(qy, minQuartY, maxQuartY), qz, sampler);
            }).getBiome(pos);
        }
        return level.getBiome(pos);
    }
}
