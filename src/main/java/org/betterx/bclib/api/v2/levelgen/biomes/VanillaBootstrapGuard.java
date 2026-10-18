package org.betterx.bclib.api.v2.levelgen.biomes;

import org.jetbrains.annotations.ApiStatus;

/**
 * Tracks whether the current thread is building the vanilla-only registry lookup
 * ({@link net.minecraft.data.registries.VanillaRegistries#createLookup()}).
 * <p>
 * That lookup only contains vanilla biomes, so Fabric's NetherBiomes entries (which BCLib registers for every
 * BCL Nether biome) must not be added to the vanilla Nether parameter list while it is built, otherwise the
 * build fails with "Unreferenced key" (e.g. when Colormatic or Vanadium call it).
 */
@ApiStatus.Internal
public final class VanillaBootstrapGuard {
    private static final ThreadLocal<int[]> DEPTH = ThreadLocal.withInitial(() -> new int[1]);

    private VanillaBootstrapGuard() {
    }

    public static void enter() {
        DEPTH.get()[0]++;
    }

    public static void leave() {
        final int[] depth = DEPTH.get();
        if (depth[0] > 0) depth[0]--;
    }

    public static boolean isBuildingVanillaLookup() {
        return DEPTH.get()[0] > 0;
    }
}
