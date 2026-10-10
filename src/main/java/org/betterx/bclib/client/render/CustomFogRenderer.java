package org.betterx.bclib.client.render;

import org.betterx.bclib.config.Configs;
import org.betterx.bclib.util.BackgroundInfo;
import org.betterx.bclib.util.MHelper;

import de.ambertation.wover.biome.api.data.BiomeData;
import de.ambertation.wover.biome.api.data.BiomeDataRegistry;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.Nullable;

/**
 * Biome dependent fog on top of vanilla's {@link net.minecraft.client.renderer.fog.FogRenderer}.
 * <p>
 * The effective density is the biome's {@link BiomeData#fogDensity} (interpolated around the camera) times the
 * {@code fog_density} client setting. It applies in the End, the Nether and wherever a biome near the camera has
 * {@link BiomeData}. There the environmental fog ends at the render distance (or at vanilla's thick fog, if that is
 * closer), and densities above 1 pull it in further, but never closer than {@link #MIN_END_FACTOR} of that distance,
 * so the fog keeps scaling with the render distance (#244).
 */
@Environment(EnvType.CLIENT)
public class CustomFogRenderer {
    // Fog ends at base * (MIN_END_FACTOR + (1 - MIN_END_FACTOR) / density): base at density 1, halfway between base
    // and the pre-26.x "base / density" at density 2, and never closer than base * MIN_END_FACTOR.
    private static final float MIN_END_FACTOR = 0.5F;
    // Fog start as a fraction of the fog end, as in the pre-26.x renderer
    private static final float START_FACTOR = 0.25F;
    // Vanilla's world-fog clamp while a boss bar requests it (see AtmosphericFogEnvironment)
    private static final float BOSS_FOG_START = 10.0F;
    private static final float BOSS_FOG_END = 96.0F;

    private static final MutableBlockPos LAST_POS = new MutableBlockPos(0, -100, 0);
    private static final MutableBlockPos MUT_POS = new MutableBlockPos();
    private static final float[] FOG_DENSITY = new float[8];
    private static final int GRID_SIZE = 32;
    private static @Nullable Level lastLevel;

    private static boolean hasBiomeData;
    private static boolean atmosphericApplied;
    private static float atmosphericDensity = 1;

    /**
     * Called after vanilla filled the atmospheric fog (so never in water, lava, powder snow, blindness or darkness).
     */
    public static void applyAtmosphericFog(FogData fog, Camera camera, ClientLevel level, float renderDistance) {
        if (!Configs.CLIENT_CONFIG.renderCustomFog()) return;
        final float configDensity = Configs.CLIENT_CONFIG.fogDensity();
        if (!(configDensity > 0)) return;

        final Vec3 pos = camera.position();
        final float density = getFogDensity(level, pos.x, pos.y, pos.z) * configDensity;
        if (!hasBiomeData && !isForcedDimension(level)) return;
        atmosphericApplied = true;
        atmosphericDensity = density;

        float vanillaStart = fog.environmentalStart;
        float vanillaEnd = fog.environmentalEnd;
        if (level.dimension() == Level.NETHER && !Configs.CLIENT_CONFIG.netherThickFog()) {
            // Without thick fog the Nether gets the same render-distance based fog as the End
            vanillaStart = 0;
            vanillaEnd = Float.MAX_VALUE;
        }

        // Thick fog (Nether, boss fog) is scaled from where it already ends, everything else from the render distance
        float end = Math.min(vanillaEnd, renderDistance) * endFactor(density);
        if (end >= vanillaEnd) return;

        float start = end * START_FACTOR;
        // keep vanilla's rain thickening (negative start), but never start later than vanilla does
        start = vanillaStart > 0 ? Math.min(start, vanillaStart) : start + vanillaStart;
        if (Minecraft.getInstance().gui.hud.getBossOverlay().shouldCreateWorldFog()) {
            start = Math.min(start, BOSS_FOG_START);
            end = Math.min(end, BOSS_FOG_END);
        }

        fog.environmentalStart = start;
        fog.environmentalEnd = end;
        fog.skyEnd = Math.min(fog.skyEnd, end);
        fog.cloudEnd = Math.min(fog.cloudEnd, end);
    }

    private static float endFactor(float density) {
        if (density <= 1) return 1;
        return MIN_END_FACTOR + (1 - MIN_END_FACTOR) / density;
    }

    private static boolean isForcedDimension(Level level) {
        return level.dimension() == Level.END || level.dimension() == Level.NETHER;
    }

    /**
     * Called once the final {@link FogData} for the frame is known. Publishes it for custom sky renderers.
     */
    public static void updateBackgroundInfo(FogData fog, Camera camera) {
        BackgroundInfo.fogColorRed = fog.color.x();
        BackgroundInfo.fogColorGreen = fog.color.y();
        BackgroundInfo.fogColorBlue = fog.color.z();
        BackgroundInfo.fogDensity = atmosphericApplied ? atmosphericDensity : 1;
        atmosphericApplied = false;

        float blindness = 0;
        if (camera.entity() instanceof LivingEntity living) {
            final MobEffectInstance effect = living.getEffect(MobEffects.BLINDNESS);
            if (effect != null) {
                blindness = effect.isInfiniteDuration() || effect.getDuration() > 20
                        ? 1
                        : effect.getDuration() / 20F;
            }
        }
        BackgroundInfo.blindness = blindness;
    }

    private static @Nullable HolderLookup.RegistryLookup<BiomeData> biomeDataLookup(ClientLevel level) {
        // Present on the client only if the registry is synced; otherwise use the integrated server's
        var lookup = level.registryAccess().lookup(BiomeDataRegistry.BIOME_DATA_REGISTRY);
        if (lookup.isPresent()) return lookup.get();

        final IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) return null;
        return server.registryAccess().lookup(BiomeDataRegistry.BIOME_DATA_REGISTRY).orElse(null);
    }

    private static boolean hasBiomeData(
            @Nullable HolderLookup.RegistryLookup<BiomeData> lookup,
            Level level,
            int x,
            int y,
            int z
    ) {
        if (lookup == null) return false;
        return level.getBiome(MUT_POS.set(x, y, z))
                    .unwrapKey()
                    .flatMap(key -> lookup.get(BiomeDataRegistry.createKey(key)))
                    .isPresent();
    }

    private static float getFogDensityI(
            @Nullable HolderLookup.RegistryLookup<BiomeData> lookup,
            Level level,
            int x,
            int y,
            int z
    ) {
        if (lookup == null) return 1.0f;
        return level.getBiome(MUT_POS.set(x, y, z))
                    .unwrapKey()
                    .flatMap(key -> lookup.get(BiomeDataRegistry.createKey(key)))
                    .map(h -> h.value().fogDensity)
                    .orElse(1.0f);
    }

    private static float getFogDensity(ClientLevel level, double x, double y, double z) {
        int x1 = MHelper.floor(x / GRID_SIZE) * GRID_SIZE;
        int y1 = MHelper.floor(y / GRID_SIZE) * GRID_SIZE;
        int z1 = MHelper.floor(z / GRID_SIZE) * GRID_SIZE;
        float dx = (float) (x - x1) / GRID_SIZE;
        float dy = (float) (y - y1) / GRID_SIZE;
        float dz = (float) (z - z1) / GRID_SIZE;

        if (lastLevel != level || LAST_POS.getX() != x1 || LAST_POS.getY() != y1 || LAST_POS.getZ() != z1) {
            final var lookup = biomeDataLookup(level);
            int x2 = x1 + GRID_SIZE;
            int y2 = y1 + GRID_SIZE;
            int z2 = z1 + GRID_SIZE;
            lastLevel = level;
            LAST_POS.set(x1, y1, z1);
            FOG_DENSITY[0] = getFogDensityI(lookup, level, x1, y1, z1);
            FOG_DENSITY[1] = getFogDensityI(lookup, level, x2, y1, z1);
            FOG_DENSITY[2] = getFogDensityI(lookup, level, x1, y2, z1);
            FOG_DENSITY[3] = getFogDensityI(lookup, level, x2, y2, z1);
            FOG_DENSITY[4] = getFogDensityI(lookup, level, x1, y1, z2);
            FOG_DENSITY[5] = getFogDensityI(lookup, level, x2, y1, z2);
            FOG_DENSITY[6] = getFogDensityI(lookup, level, x1, y2, z2);
            FOG_DENSITY[7] = getFogDensityI(lookup, level, x2, y2, z2);
            hasBiomeData = hasBiomeData(lookup, level, x1, y1, z1) || hasBiomeData(lookup, level, x2, y1, z1)
                    || hasBiomeData(lookup, level, x1, y1, z2) || hasBiomeData(lookup, level, x2, y1, z2);
        }

        float a = Mth.lerp(dx, FOG_DENSITY[0], FOG_DENSITY[1]);
        float b = Mth.lerp(dx, FOG_DENSITY[2], FOG_DENSITY[3]);
        float c = Mth.lerp(dx, FOG_DENSITY[4], FOG_DENSITY[5]);
        float d = Mth.lerp(dx, FOG_DENSITY[6], FOG_DENSITY[7]);

        a = Mth.lerp(dy, a, b);
        b = Mth.lerp(dy, c, d);

        return Mth.lerp(dz, a, b);
    }
}
