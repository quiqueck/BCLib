package org.betterx.bclib.api.v2;

import org.betterx.bclib.util.BlocksHelper;
import org.betterx.bclib.util.WeightedList;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class BonemealAPI {
    private static final Map<ResourceLocation, Map<Block, WeightedList<BiConsumer<Level, BlockPos>>>> WATER_GRASS_BIOMES = Maps.newHashMap();
    private static final Map<ResourceLocation, Map<Block, WeightedList<BiConsumer<Level, BlockPos>>>> LAND_GRASS_BIOMES = Maps.newHashMap();
    private static final Map<Block, WeightedList<BiConsumer<Level, BlockPos>>> WATER_GRASS_TYPES = Maps.newHashMap();
    private static final Map<Block, WeightedList<BiConsumer<Level, BlockPos>>> LAND_GRASS_TYPES = Maps.newHashMap();
    private static final Map<Block, Block> SPREADABLE_BLOCKS = Maps.newHashMap();
    private static final Set<Block> TERRAIN_TO_SPREAD = Sets.newHashSet();
    private static final Set<Block> TERRAIN = Sets.newHashSet();

    @Deprecated(forRemoval = true)
    public static void addSpreadableBlock(Block spreadableBlock, Block surfaceForSpread) {
        SPREADABLE_BLOCKS.put(spreadableBlock, surfaceForSpread);
        TERRAIN_TO_SPREAD.add(surfaceForSpread);
        TERRAIN.add(surfaceForSpread);
    }

    public static boolean isTerrain(Block block) {
        return TERRAIN.contains(block);
    }

    @Deprecated(forRemoval = true)
    public static boolean isSpreadableTerrain(Block block) {
        return TERRAIN_TO_SPREAD.contains(block);
    }

    @Deprecated(forRemoval = true)
    public static Block getSpreadable(Block block) {
        return SPREADABLE_BLOCKS.get(block);
    }

    @Deprecated(forRemoval = true)
    public static void addLandGrass(Block plant, Block... terrain) {
        addLandGrass(makeConsumer(plant), terrain);
    }

    @Deprecated(forRemoval = true)
    public static void addLandGrass(BiConsumer<Level, BlockPos> plant, Block... terrain) {
        for (Block block : terrain) {
            addLandGrass(plant, block, 1F);
        }
    }

    /**
     * This API is deprecated, please use the new {@link org.betterx.bclib.api.v3.bonemeal.BonemealAPI}.
     *
     * @param biome
     * @param plant
     * @param terrain
     */
    @Deprecated(forRemoval = true)
    public static void addLandGrass(ResourceLocation biome, Block plant, Block... terrain) {
        addLandGrass(biome, makeConsumer(plant), terrain);
    }

    @Deprecated(forRemoval = true)
    public static void addLandGrass(ResourceLocation biome, BiConsumer<Level, BlockPos> plant, Block... terrain) {
        for (Block block : terrain) {
            addLandGrass(biome, plant, block, 1F);
        }
    }

    @Deprecated(forRemoval = true)
    public static void addLandGrass(Block plant, Block terrain, float chance) {
        addLandGrass(makeConsumer(plant), terrain, chance);
    }

    @Deprecated(forRemoval = true)
    public static void addLandGrass(BiConsumer<Level, BlockPos> plant, Block terrain, float chance) {
        WeightedList<BiConsumer<Level, BlockPos>> list = LAND_GRASS_TYPES.get(terrain);
        if (list == null) {
            list = new WeightedList<>();
            LAND_GRASS_TYPES.put(terrain, list);
        }
        TERRAIN.add(terrain);
        list.add(plant, chance);
    }

    @Deprecated(forRemoval = true)
    public static void addLandGrass(ResourceLocation biome, Block plant, Block terrain, float chance) {
        addLandGrass(biome, makeConsumer(plant), terrain, chance);
    }

    @Deprecated(forRemoval = true)
    public static void addLandGrass(
            ResourceLocation biome,
            BiConsumer<Level, BlockPos> plant,
            Block terrain,
            float chance
    ) {
        Map<Block, WeightedList<BiConsumer<Level, BlockPos>>> map = LAND_GRASS_BIOMES.get(biome);
        if (map == null) {
            map = Maps.newHashMap();
            LAND_GRASS_BIOMES.put(biome, map);
        }
        WeightedList<BiConsumer<Level, BlockPos>> list = map.get(terrain);
        if (list == null) {
            list = new WeightedList<>();
            map.put(terrain, list);
        }
        TERRAIN.add(terrain);
        list.add(plant, chance);
    }

    public static void addWaterGrass(Block plant, Block... terrain) {
        addWaterGrass(makeConsumer(plant), terrain);
    }

    public static void addWaterGrass(BiConsumer<Level, BlockPos> plant, Block... terrain) {
        for (Block block : terrain) {
            addWaterGrass(plant, block, 1F);
        }
    }

    public static void addWaterGrass(ResourceLocation biome, Block plant, Block... terrain) {
        addWaterGrass(biome, makeConsumer(plant), terrain);
    }

    public static void addWaterGrass(ResourceLocation biome, BiConsumer<Level, BlockPos> plant, Block... terrain) {
        for (Block block : terrain) {
            addWaterGrass(biome, plant, block, 1F);
        }
    }

    public static void addWaterGrass(Block plant, Block terrain, float chance) {
        addWaterGrass(makeConsumer(plant), terrain, chance);
    }

    public static void addWaterGrass(BiConsumer<Level, BlockPos> plant, Block terrain, float chance) {
        WeightedList<BiConsumer<Level, BlockPos>> list = WATER_GRASS_TYPES.get(terrain);
        if (list == null) {
            list = new WeightedList<>();
            WATER_GRASS_TYPES.put(terrain, list);
        }
        TERRAIN.add(terrain);
        list.add(plant, chance);
    }

    public static void addWaterGrass(ResourceLocation biome, Block plant, Block terrain, float chance) {
        addWaterGrass(biome, makeConsumer(plant), terrain, chance);
    }

    public static void addWaterGrass(
            ResourceLocation biome,
            BiConsumer<Level, BlockPos> plant,
            Block terrain,
            float chance
    ) {
        Map<Block, WeightedList<BiConsumer<Level, BlockPos>>> map = WATER_GRASS_BIOMES.get(biome);
        if (map == null) {
            map = Maps.newHashMap();
            WATER_GRASS_BIOMES.put(biome, map);
        }
        WeightedList<BiConsumer<Level, BlockPos>> list = map.get(terrain);
        if (list == null) {
            list = new WeightedList<>();
            map.put(terrain, list);
        }
        TERRAIN.add(terrain);
        list.add(plant, chance);
    }

    @Deprecated(forRemoval = true)
    public static BiConsumer<Level, BlockPos> getLandGrass(
            ResourceLocation biomeID,
            Block terrain,
            RandomSource random
    ) {
        Map<Block, WeightedList<BiConsumer<Level, BlockPos>>> map = LAND_GRASS_BIOMES.get(biomeID);
        WeightedList<BiConsumer<Level, BlockPos>> list;
        if (map != null) {
            list = map.get(terrain);
            if (list == null) {
                list = LAND_GRASS_TYPES.get(terrain);
            }
        } else {
            list = LAND_GRASS_TYPES.get(terrain);
        }
        return list == null ? null : list.get(random);
    }

    public static BiConsumer<Level, BlockPos> getWaterGrass(
            ResourceLocation biomeID,
            Block terrain,
            RandomSource random
    ) {
        Map<Block, WeightedList<BiConsumer<Level, BlockPos>>> map = WATER_GRASS_BIOMES.get(biomeID);
        WeightedList<BiConsumer<Level, BlockPos>> list;
        if (map != null) {
            list = map.get(terrain);
            if (list == null) {
                list = WATER_GRASS_TYPES.get(terrain);
            }
        } else {
            list = WATER_GRASS_TYPES.get(terrain);
        }
        return list == null ? null : list.get(random);
    }

    private static BiConsumer<Level, BlockPos> makeConsumer(Block block) {
        return (level, pos) -> BlocksHelper.setWithoutUpdate(level, pos, block);
    }
}
