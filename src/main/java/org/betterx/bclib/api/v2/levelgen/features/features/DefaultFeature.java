package org.betterx.bclib.api.v2.levelgen.features.features;

import org.betterx.bclib.util.BlocksHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * 26.3 collapsed {@code ConfiguredFeature<FC, F>} into {@link Feature}: the whole
 * {@code net.minecraft.world.level.levelgen.feature.configurations} package (including
 * {@code NoneFeatureConfiguration}) is gone, {@link Feature} is a non-generic <em>interface</em> that
 * carries its own configuration, and it declares {@code MapCodec<? extends Feature> codec()} plus
 * {@code place(WorldGenLevel, ChunkGenerator, RandomSource, BlockPos)}.
 * <p>
 * This class therefore implements the interface instead of extending the old abstract class, and the
 * codec is no longer passed to a constructor - each concrete feature supplies its own
 * {@code codec()}. The static surface-probing helpers below are unchanged.
 */
public abstract class DefaultFeature implements Feature {
    public static final BlockState AIR = Blocks.AIR.defaultBlockState();
    public static final BlockState WATER = Blocks.WATER.defaultBlockState();

    public static int getYOnSurface(WorldGenLevel world, int x, int z) {
        return world.getHeight(Types.WORLD_SURFACE, x, z);
    }

    public static int getYOnSurfaceWG(WorldGenLevel world, int x, int z) {
        return world.getHeight(Types.WORLD_SURFACE_WG, x, z);
    }

    public static BlockPos getPosOnSurface(WorldGenLevel world, BlockPos pos) {
        return world.getHeightmapPos(Types.WORLD_SURFACE, pos);
    }

    public static BlockPos getPosOnSurfaceWG(WorldGenLevel world, BlockPos pos) {
        return world.getHeightmapPos(Types.WORLD_SURFACE_WG, pos);
    }

    public static BlockPos getPosOnSurfaceRaycast(WorldGenLevel world, BlockPos pos) {
        return getPosOnSurfaceRaycast(world, pos, 256);
    }

    public static BlockPos getPosOnSurfaceRaycast(WorldGenLevel world, BlockPos pos, int dist) {
        int h = BlocksHelper.downRay(world, pos, dist);
        return pos.below(h);
    }
}
