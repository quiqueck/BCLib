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
            fogStart = viewDistance * 0.25F / fog; // In vanilla - 0
            fogEnd = viewDistance / fog;
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
        return BCLBiomeRegistry.isEmptyBiome(BiomeAPI.getRenderBiome(biome));
    }

    private static float getFogDensityI(Level level, int x, int y, int z) {
        Biome biome = level.getBiome(MUT_POS.set(x, y, z)).value();
        BCLBiome renderBiome = BiomeAPI.getRenderBiome(biome);
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
