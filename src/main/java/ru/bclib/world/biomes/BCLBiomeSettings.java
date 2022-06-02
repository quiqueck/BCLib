package ru.bclib.world.biomes;

import net.minecraft.world.level.biome.Biome;
import ru.bclib.config.Configs;

public class BCLBiomeSettings {
	float terrainHeight;
	float fogDensity;
	float genChance;
	int edgeSize;
	boolean vertical;
	BCLBiome edge;
	
	protected BCLBiomeSettings() {
		this.terrainHeight = 0.1F;
		this.fogDensity = 1.0F;
		this.genChance = 1.0F;
		this.edgeSize = 0;
		this.vertical = false;
		this.edge = null;
	}
	
	public static Builder createBCL() {
		return new Builder();
	}
	
	public static class Builder extends CommonBuilder<BCLBiomeSettings, Builder>{
		public Builder(){
			super(new BCLBiomeSettings());
		}
	}
	
	public static class CommonBuilder<T extends BCLBiomeSettings, R extends CommonBuilder>{
		private final T storage;
		CommonBuilder(T storage) {
			this.storage = storage;
		}
		
		public T build(){
			return storage;
		}
		
		/**
		 * Set gen chance for this biome, default value is 1.0.
		 * @param genChance chance of this biome to be generated.
		 * @return same {@link BCLBiomeSettings}.
		 */
		public R setGenChance(float genChance) {
			storage.genChance = genChance;
			return (R)this;
		}
		
		/**
		 * Setter for terrain height, can be used in custom terrain generator.
		 * @param terrainHeight a relative float terrain height value.
		 * @return same {@link Builder}.
		 */
		public R setTerrainHeight(float terrainHeight) {
			storage.terrainHeight = terrainHeight;
			return (R)this;
		}
		
		/**
		 * Set biome vertical distribution (for tall Nether only).
		 * @return same {@link Builder}.
		 */
		public R setVertical() {
			return setVertical(true);
		}
		
		/**
		 * Set biome vertical distribution (for tall Nether only).
		 * @param vertical {@code boolean} value.
		 * @return same {@link Builder}.
		 */
		public R setVertical(boolean vertical) {
			storage.vertical = vertical;
			return (R)this;
		}
		
		/**
		 * Set edges size for this biome. Size is in blocks.
		 * @param size as a float value.
		 * @return same {@link Builder}.
		 */
		public R setEdgeSize(int size) {
			storage.edgeSize = size;
			return (R)this;
		}
		
		/**
		 * Set edges:biome for this biome.
		 * @param edge The {@link Biome}.
		 * @return same {@link Builder}.
		 */
		public R setEdge(BCLBiome edge) {
			storage.edge = edge;
			return (R)this;
		}
		
		/**
		 * Sets fog density for this biome.
		 * @param fogDensity
		 * @return same {@link Builder}.
		 */
		public R setFogDensity(float fogDensity) {
			storage.fogDensity = fogDensity;
			return (R)this;
		}
	}
	
	/**
	 * Getter for biome generation chance, used in {@link ru.bclib.world.generator.BiomePicker} and in custom generators.
	 * @return biome generation chance as float.
	 */
	public float getGenChance() {
		return this.genChance;
	}
	
	/**
	 * Checks if biome is vertical, for tall Nether only (or for custom generators).
	 * @return is biome vertical or not.
	 */
	public boolean isVertical() {
		return vertical;
	}
	
	/**
	 * Getter for terrain height, can be used in custom terrain generator.
	 * @return terrain height.
	 */
	public float getTerrainHeight() {
		return terrainHeight;
	}
	
	/**
	 * Getter for fog density, used in custom for renderer.
	 * @return fog density as a float.
	 */
	public float getFogDensity() {
		return fogDensity;
	}
	
	/**
	 * Getter for biome edge size.
	 * @return edge size in blocks.
	 */
	public int getEdgeSize() {
		return edgeSize;
	}
	
	/**
	 * Getter for edge-biome.
	 * @return The assigned edge biome.
	 */
	public BCLBiome getEdge() {
		return edge;
	}
	
	/**
	 * Load values from Config and apply to the passed Biome. The Default values for the loaded settings
	 * are derifed from the values store in this object
	 * @param biome {@link BCLBiome} to assign values to
	 */
	public void applyWithDefaults(BCLBiome biome){
		final String group = biome.configGroup();
		biome.genChance = Configs.BIOMES_CONFIG.getFloat(group, "generation_chance", this.genChance);
		
		if (edge!=null){
			biome.edgeSize = Configs.BIOMES_CONFIG.getInt(group, "edge_size", this.edgeSize);
			if (edgeSize>0) {
				biome.setEdge(edge);
			}
		}
		
		if (!(this instanceof VanillaBiomeSettings)){
			biome.fogDensity = Configs.BIOMES_CONFIG.getFloat(group, "fog_density", this.fogDensity);
			biome.vertical = Configs.BIOMES_CONFIG.getBoolean(group, "vertical", this.vertical);
			biome.terrainHeight = Configs.BIOMES_CONFIG.getFloat(group, "terrain_height", this.terrainHeight);
		}
		
		Configs.BIOMES_CONFIG.saveChanges();
	}
}
