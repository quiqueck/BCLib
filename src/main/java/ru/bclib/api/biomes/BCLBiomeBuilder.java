package ru.bclib.api.biomes;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome.BiomeBuilder;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import ru.bclib.util.ColorUtil;
import ru.bclib.world.biomes.BCLBiome;

public class BCLBiomeBuilder {
	private static final BCLBiomeBuilder INSTANCE = new BCLBiomeBuilder();
	
	private BiomeSpecialEffects.Builder effectsBuilder;
	private MobSpawnSettings.Builder spawnSettings;
	private Precipitation precipitation;
	private ResourceLocation biomeID;
	private BiomeCategory category;
	private float temperature;
	private float fogDensity;
	private float downfall;
	
	/**
	 * Starts new biome building process.
	 * @param biomeID {@link ResourceLocation} biome identifier.
	 * @return prepared {@link BCLBiomeBuilder} instance.
	 */
	public static BCLBiomeBuilder start(ResourceLocation biomeID) {
		INSTANCE.biomeID = biomeID;
		INSTANCE.precipitation = Precipitation.NONE;
		INSTANCE.category = BiomeCategory.NONE;
		INSTANCE.effectsBuilder = null;
		INSTANCE.spawnSettings = null;
		INSTANCE.temperature = 1.0F;
		INSTANCE.fogDensity = 1.0F;
		return INSTANCE;
	}
	
	/**
	 * Set biome {@link Precipitation}. Affect biome visual effects (rain, snow, none).
	 * @param precipitation {@link Precipitation}
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder precipitation(Precipitation precipitation) {
		this.precipitation = precipitation;
		return this;
	}
	
	/**
	 * Set biome category. Doesn't affect biome worldgen, but Fabric biome modifications can target biome by it.
	 * @param category {@link BiomeCategory}
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder category(BiomeCategory category) {
		this.category = category;
		return this;
	}
	
	/**
	 * Set biome temperature, affect plant color, biome generation and ice formation.
	 * @param temperature biome temperature.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder temperature(float temperature) {
		this.temperature = temperature;
		return this;
	}
	
	/**
	 * Set biome wetness (same as downfall). Affect plant color and biome generation.
	 * @param wetness biome wetness (downfall).
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder wetness(float wetness) {
		this.downfall = wetness;
		return this;
	}
	
	/**
	 * Adds mob spawning to biome.
	 * @param entityType {@link EntityType} mob type.
	 * @param weight spawn weight.
	 * @param minGroupCount minimum mobs in group.
	 * @param maxGroupCount maximum mobs in group.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public <M extends Mob> BCLBiomeBuilder spawn(EntityType<M> entityType, int weight, int minGroupCount, int maxGroupCount) {
		getSpawns().addSpawn(entityType.getCategory(), new SpawnerData(entityType, weight, minGroupCount, maxGroupCount));
		return this;
	}
	
	/**
	 * Adds ambient particles to thr biome.
	 * @param particle {@link ParticleOptions} particles (or {@link net.minecraft.core.particles.ParticleType}).
	 * @param probability particle spawn probability, should have low value (example: 0.01F).
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder particles(ParticleOptions particle, float probability) {
		getEffects().ambientParticle(new AmbientParticleSettings(particle, probability));
		return this;
	}
	
	/**
	 * Sets sky color for the biome. Color is in ARGB int format.
	 * @param color ARGB color as integer.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder skyColor(int color) {
		getEffects().skyColor(color);
		return this;
	}
	
	/**
	 * Sets sky color for the biome. Color represented as red, green and blue channel values.
	 * @param red red color component [0-255]
	 * @param green green color component [0-255]
	 * @param blue blue color component [0-255]
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder skyColor(int red, int green, int blue) {
		red = Mth.clamp(red, 0, 255);
		green = Mth.clamp(green, 0, 255);
		blue = Mth.clamp(blue, 0, 255);
		return skyColor(ColorUtil.color(red, green, blue));
	}
	
	/**
	 * Sets fog color for the biome. Color is in ARGB int format.
	 * @param color ARGB color as integer.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder fogColor(int color) {
		getEffects().fogColor(color);
		return this;
	}
	
	/**
	 * Sets fog color for the biome. Color represented as red, green and blue channel values.
	 * @param red red color component [0-255]
	 * @param green green color component [0-255]
	 * @param blue blue color component [0-255]
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder fogColor(int red, int green, int blue) {
		red = Mth.clamp(red, 0, 255);
		green = Mth.clamp(green, 0, 255);
		blue = Mth.clamp(blue, 0, 255);
		return fogColor(ColorUtil.color(red, green, blue));
	}
	
	/**
	 * Sets fog density for the biome.
	 * @param density fog density as a float, default value is 1.0F.
	 * @return same {@link BCLBiomeBuilder} instance.
	 */
	public BCLBiomeBuilder fogDensity(float density) {
		this.fogDensity = density;
		return this;
	}
	
	/**
	 * Finalize biome creation.
	 * @return created {@link BCLBiome} instance.
	 */
	public BCLBiome build() {
		BiomeBuilder builder = new BiomeBuilder()
			.precipitation(precipitation)
			.biomeCategory(category)
			.temperature(temperature)
			.downfall(downfall);
		
		if (spawnSettings != null) {
			builder.mobSpawnSettings(spawnSettings.build());
		}
		
		if (effectsBuilder != null) {
			builder.specialEffects(effectsBuilder.build());
		}
		
		return new BCLBiome(biomeID, builder.build()).setFogDensity(fogDensity);
	}
	
	private BiomeSpecialEffects.Builder getEffects() {
		if (effectsBuilder == null) {
			effectsBuilder = new BiomeSpecialEffects.Builder();
		}
		return effectsBuilder;
	}
	
	private MobSpawnSettings.Builder getSpawns() {
		if (spawnSettings == null) {
			spawnSettings = new MobSpawnSettings.Builder();
		}
		return spawnSettings;
	}
}