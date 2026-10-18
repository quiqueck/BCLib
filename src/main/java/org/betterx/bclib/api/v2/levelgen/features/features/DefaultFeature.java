package org.betterx.bclib.api.v2.levelgen.features.features;

import org.betterx.bclib.util.BlocksHelper;
import org.betterx.bclib.util.GenerationArea;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public abstract class DefaultFeature extends Feature<NoneFeatureConfiguration> {
    public static final BlockState AIR = Blocks.AIR.defaultBlockState();
    public static final BlockState WATER = Blocks.WATER.defaultBlockState();

    public DefaultFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    // Height lookups outside the generation area (the 3x3 chunks around the chunk being decorated) use the nearest
    // column inside it: other worldgen threads may be generating the chunks further away.
    public static int getYOnSurface(WorldGenLevel world, int x, int z) {
        return GenerationArea.getHeight(world, Types.WORLD_SURFACE, x, z);
    }

    public static int getYOnSurfaceWG(WorldGenLevel world, int x, int z) {
        return GenerationArea.getHeight(world, Types.WORLD_SURFACE_WG, x, z);
    }

    public static BlockPos getPosOnSurface(WorldGenLevel world, BlockPos pos) {
        return new BlockPos(pos.getX(), getYOnSurface(world, pos.getX(), pos.getZ()), pos.getZ());
    }

    public static BlockPos getPosOnSurfaceWG(WorldGenLevel world, BlockPos pos) {
        return new BlockPos(pos.getX(), getYOnSurfaceWG(world, pos.getX(), pos.getZ()), pos.getZ());
    }

    public static BlockPos getPosOnSurfaceRaycast(WorldGenLevel world, BlockPos pos) {
        return getPosOnSurfaceRaycast(world, pos, 256);
    }

    public static BlockPos getPosOnSurfaceRaycast(WorldGenLevel world, BlockPos pos, int dist) {
        int h = BlocksHelper.downRay(world, pos, dist);
        return pos.below(h);
    }
}
