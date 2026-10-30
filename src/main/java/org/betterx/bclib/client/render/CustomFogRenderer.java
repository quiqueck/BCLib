package org.betterx.bclib.client.render;

import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiome;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeRegistry;
import org.betterx.bclib.api.v2.levelgen.biomes.BiomeAPI;
import org.betterx.bclib.config.Configs;
import org.betterx.bclib.util.BackgroundInfo;
import org.betterx.bclib.util.MHelper;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.FogType;

public class CustomFogRenderer {
    private static final MutableBlockPos LAST_POS = new MutableBlockPos(0, -100, 0);
    private static final MutableBlockPos MUT_POS = new MutableBlockPos();
    private static final float[] FOG_DENSITY = new float[8];
    private static final int GRID_SIZE = 32;
    // Fog start as a fraction of the fog end
    private static final float START_FACTOR = 0.25F;
    // In the End the fog start moves from START_FACTOR (full haze) out to this fraction of the fog end (no haze),
    // so a low density leaves only a thin band at the render distance. The haze is the same value that drives the
    // fog layer of BetterEnd's sky (density - 1, clamped to 0..1), so terrain and sky fade in and out together.
    private static final float THIN_START_FACTOR = 0.7F;
    private static float fogStart = 0;
    private static float fogEnd = 192;

    public static boolean applyFogDensity(Camera camera, float viewDistance, boolean thickFog) {
        if (!Configs.CLIENT_CONFIG.renderCustomFog()) {
            return false;
        }

        FogType fogType = camera.getFluidInCamera();
        if (fogType != FogType.NONE) {
            BackgroundInfo.fogDensity = 1;
            return false;
        }
        Entity entity = camera.getEntity();

        if (!isForcedDimension(entity.level()) && shouldIgnoreArea(
                entity.level(),
                (int) entity.getX(),
                (int) entity.getEyeY(),
                (int) entity.getZ()
        )) {
            BackgroundInfo.fogDensity = 1;
            return false;
        }

        float fog = getFogDensity(
                entity.level(),
                entity.getX(),
                entity.getEyeY(),
                entity.getZ()
        ) * Configs.CLIENT_CONFIG.fogDensity();
        BackgroundInfo.fogDensity = fog;

        if (thickFog(thickFog, entity.level())) {
            fogStart = viewDistance * 0.05F / fog;
            fogEnd = Math.min(viewDistance, 192.0F) * 0.5F / fog;
        } else {
            fogEnd = viewDistance / fog;
            fogStart = fogEnd * startFactor(entity.level(), fog); // In vanilla - 0
        }

        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            MobEffectInstance effect = livingEntity.getEffect(MobEffects.BLINDNESS);
            if (effect != null) {
                int duration = effect.getDuration();
                if (duration > 20) {
                    fogStart = 0;
                    fogEnd *= 0.03F;
                    BackgroundInfo.blindness = 1;
                } else {
                    float delta = (float) duration / 20F;
                    BackgroundInfo.blindness = delta;
                    fogStart = Mth.lerp(delta, fogStart, 0);
                    fogEnd = Mth.lerp(delta, fogEnd, fogEnd * 0.03F);
                }
            } else {
                BackgroundInfo.blindness = 0;
            }
        }
        RenderSystem.setShaderFogStart(fogStart);
        RenderSystem.setShaderFogEnd(fogEnd);

        return true;
    }

    private static float startFactor(Level level, float density) {
        if (level.dimension() != Level.END) return START_FACTOR;
        return Mth.lerp(Mth.clamp(density - 1, 0, 1), THIN_START_FACTOR, START_FACTOR);
    }

    /**
     * How much of the sky's horizon colour the End's fog takes on: where the haze is thin the terrain fog fades into
     * the sky instead of the biome's fog colour, so the band at the render distance does not stand out against it.
     */
    public static float skyHorizonBlend(Level level) {
        if (level.dimension() != Level.END || !BackgroundInfo.hasSkyHorizonColor) return 0;
        return 1 - Mth.clamp(BackgroundInfo.fogDensity - 1, 0, 1);
    }

    private static boolean thickFog(boolean thickFog, Level level) {
        if (!thickFog) {
            return false;
        }
        if (level.dimension() == Level.NETHER) {
            return Configs.CLIENT_CONFIG.netherThickFog();
        }
        return true;
    }

    private static boolean isForcedDimension(Level level) {
        return level.dimension() == Level.END || level.dimension() == Level.NETHER;
    }

    private static boolean shouldIgnoreArea(Level level, int x, int y, int z) {
        for (int i = -8; i <= 8; i += 8) {
            for (int j = -8; j <= 8; j += 8) {
                if (!shouldIgnore(level, x + i, y, z + j)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean shouldIgnore(Level level, int x, int y, int z) {
        Biome biome = level.getBiome(MUT_POS.set(x, y, z)).value();
        return BCLBiomeRegistry.isEmptyBiome(BiomeAPI.getRenderBiome(biome, level.registryAccess()));
    }

    private static float getFogDensityI(Level level, int x, int y, int z) {
        Biome biome = level.getBiome(MUT_POS.set(x, y, z)).value();
        BCLBiome renderBiome = BiomeAPI.getRenderBiome(biome, level.registryAccess());
        if (renderBiome == null) {
            return BCLBiomeRegistry.EMPTY_BIOME.settings.getFogDensity();
        }
        return renderBiome.settings.getFogDensity();
    }

    private static float getFogDensity(Level level, double x, double y, double z) {
        int x1 = MHelper.floor(x / GRID_SIZE) * GRID_SIZE;
        int y1 = MHelper.floor(y / GRID_SIZE) * GRID_SIZE;
        int z1 = MHelper.floor(z / GRID_SIZE) * GRID_SIZE;
        float dx = (float) (x - x1) / GRID_SIZE;
        float dy = (float) (y - y1) / GRID_SIZE;
        float dz = (float) (z - z1) / GRID_SIZE;

        if (LAST_POS.getX() != x1 || LAST_POS.getY() != y1 || LAST_POS.getZ() != z1) {
            int x2 = x1 + GRID_SIZE;
            int y2 = y1 + GRID_SIZE;
            int z2 = z1 + GRID_SIZE;
            LAST_POS.set(x1, y1, z1);
            FOG_DENSITY[0] = getFogDensityI(level, x1, y1, z1);
            FOG_DENSITY[1] = getFogDensityI(level, x2, y1, z1);
            FOG_DENSITY[2] = getFogDensityI(level, x1, y2, z1);
            FOG_DENSITY[3] = getFogDensityI(level, x2, y2, z1);
            FOG_DENSITY[4] = getFogDensityI(level, x1, y1, z2);
            FOG_DENSITY[5] = getFogDensityI(level, x2, y1, z2);
            FOG_DENSITY[6] = getFogDensityI(level, x1, y2, z2);
            FOG_DENSITY[7] = getFogDensityI(level, x2, y2, z2);
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
