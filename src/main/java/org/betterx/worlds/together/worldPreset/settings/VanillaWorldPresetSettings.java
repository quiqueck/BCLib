package org.betterx.worlds.together.worldPreset.settings;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.WorldGenSettings;

import java.util.Set;

public class VanillaWorldPresetSettings extends WorldPresetSettings {
    public static final VanillaWorldPresetSettings DEFAULT = new VanillaWorldPresetSettings();
    public static final Codec<VanillaWorldPresetSettings> CODEC = Codec.unit(VanillaWorldPresetSettings::new);

    @Override
    public Codec<? extends WorldPresetSettings> codec() {
        return CODEC;
    }

    @Override
    public WorldGenSettings repairSettingsOnLoad(RegistryAccess registryAccess, WorldGenSettings settings) {
        return settings;
    }

    @Override
    public BiomeSource addDatapackBiomes(BiomeSource biomeSource, Set<Holder<Biome>> datapackBiomes) {
        return biomeSource;
    }
}
