package org.betterx.bclib.api.v2.generator.config;

import org.betterx.bclib.api.v2.generator.BCLibNetherBiomeSource;
import org.betterx.bclib.api.v2.generator.BiomePicker;
import org.betterx.bclib.api.v2.generator.map.hex.HexBiomeMap;
import org.betterx.bclib.api.v2.generator.map.square.SquareBiomeMap;
import org.betterx.bclib.interfaces.BiomeMap;
import org.betterx.bclib.util.TriFunction;
import org.betterx.worlds.together.biomesource.config.BiomeSourceConfig;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class BCLNetherBiomeSourceConfig implements BiomeSourceConfig<BCLibNetherBiomeSource> {
    public static final BCLNetherBiomeSourceConfig VANILLA = new BCLNetherBiomeSourceConfig(
            NetherBiomeMapType.VANILLA
    );
    public static final BCLNetherBiomeSourceConfig MINECRAFT_17 = new BCLNetherBiomeSourceConfig(
            NetherBiomeMapType.SQUARE
    );
    public static final BCLNetherBiomeSourceConfig MINECRAFT_18 = new BCLNetherBiomeSourceConfig(
            NetherBiomeMapType.HEX
    );
    public static final BCLNetherBiomeSourceConfig DEFAULT = MINECRAFT_18;

    public static final Codec<BCLNetherBiomeSourceConfig> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    BCLNetherBiomeSourceConfig.NetherBiomeMapType.CODEC
                            .fieldOf("map_type")
                            .orElse(DEFAULT.mapVersion)
                            .forGetter(o -> o.mapVersion)
            )
            .apply(instance, BCLNetherBiomeSourceConfig::new));
    public final @NotNull NetherBiomeMapType mapVersion;

    public BCLNetherBiomeSourceConfig(@NotNull NetherBiomeMapType mapVersion) {
        this.mapVersion = mapVersion;
    }

    @Override
    public String toString() {
        return "BCLibNetherBiomeSourceConfig{" +
                "mapVersion=" + mapVersion +
                '}';
    }

    @Override
    public boolean couldSetWithoutRepair(BiomeSourceConfig<?> input) {
        if (input instanceof BCLNetherBiomeSourceConfig cfg) {
            return mapVersion == cfg.mapVersion;
        }
        return false;
    }

    @Override
    public boolean sameConfig(BiomeSourceConfig<?> input) {
        return this.equals(input);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BCLNetherBiomeSourceConfig)) return false;
        BCLNetherBiomeSourceConfig that = (BCLNetherBiomeSourceConfig) o;
        return mapVersion == that.mapVersion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mapVersion);
    }

    public enum NetherBiomeMapType implements StringRepresentable {
        VANILLA("vanilla", (seed, biomeSize, picker) -> new HexBiomeMap(seed, biomeSize, picker)),
        SQUARE("square", (seed, biomeSize, picker) -> new SquareBiomeMap(seed, biomeSize, picker)),
        HEX("hex", (seed, biomeSize, picker) -> new HexBiomeMap(seed, biomeSize, picker));

        public static final Codec<NetherBiomeMapType> CODEC = StringRepresentable.fromEnum(NetherBiomeMapType::values);
        public final String name;
        public final TriFunction<Long, Integer, BiomePicker, BiomeMap> mapBuilder;

        NetherBiomeMapType(String name, TriFunction<Long, Integer, BiomePicker, BiomeMap> mapBuilder) {
            this.name = name;
            this.mapBuilder = mapBuilder;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
