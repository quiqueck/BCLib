package org.betterx.bclib.api.v2.generator;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.interfaces.TheEndBiomeDataAccessor;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import net.fabricmc.fabric.impl.biome.TheEndBiomeData;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;


/**
 * Helper class until FAPI integrates https://github.com/FabricMC/fabric/pull/2369
 */
public class TheEndBiomesHelper {
    public static TheEndBiomeDataAccessor INSTANCE;

    private static TheEndBiomeDataAccessor get() {
        if (INSTANCE == null) {
            try {
                Class<TheEndBiomeData> cl = TheEndBiomeData.class;
                Constructor constr = Arrays.stream(cl.getDeclaredConstructors())
                                           .filter(c -> c.getParameterCount() == 0)
                                           .findFirst()
                                           .orElseThrow();
                constr.setAccessible(true);
                INSTANCE = (TheEndBiomeDataAccessor) constr.newInstance();
            } catch (NoClassDefFoundError cnf) {

            } catch (InstantiationException e) {

            } catch (IllegalAccessException e) {

            } catch (InvocationTargetException e) {

            }
            if (INSTANCE == null) {
                BCLib.LOGGER.warning("Unable to access internal End-Biome API from Fabric. Using Fallback behaviour.");
                INSTANCE = new TheEndBiomeDataAccessor() {
                    @Override
                    public boolean bcl_canGenerateAsEndBiome(ResourceKey<Biome> key) {
                        return true;
                    }

                    @Override
                    public boolean bcl_canGenerateAsEndMidlandBiome(ResourceKey<Biome> key) {
                        return false;
                    }

                    @Override
                    public boolean bcl_canGenerateAsEndBarrensBiome(ResourceKey<Biome> key) {
                        return false;
                    }
                };
            }
        }
        return INSTANCE;
    }


    /**
     * Returns true if the given biome was added in the end, considering the Vanilla end biomes,
     * and any biomes added to the End by mods.
     */
    public static boolean isIntendedForEndBiome(ResourceKey<Biome> biome) {
        return get().bcl_canGenerateAsEndBiome(biome);
    }

    /**
     * Returns true if the given biome was added as midland biome in the end, considering the Vanilla end biomes,
     * and any biomes added to the End as midland biome by mods.
     */
    public static boolean isIntendedForEndMidlands(ResourceKey<Biome> biome) {
        return get().bcl_canGenerateAsEndMidlandBiome(biome) && !get().bcl_canGenerateAsEndBiome(biome);
    }

    /**
     * Returns true if the given biome was added as barrens biome in the end, considering the Vanilla end biomes,
     * and any biomes added to the End as barrens biome by mods.
     */
    public static boolean isIntendedForEndBarrens(ResourceKey<Biome> biome) {
        return get().bcl_canGenerateAsEndBarrensBiome(biome) && !get().bcl_canGenerateAsEndBiome(biome) && !get().bcl_canGenerateAsEndMidlandBiome(
                biome);
    }

    public static boolean isIntendedForEndLand(ResourceKey<Biome> biome) {
        return isIntendedForEndBiome(biome) || isIntendedForEndMidlands(biome);
    }

    public static boolean isIntendedForAny(ResourceKey<Biome> biome) {
        return isIntendedForEndBiome(biome) || isIntendedForEndMidlands(biome) || isIntendedForEndBarrens(biome);
    }
}
