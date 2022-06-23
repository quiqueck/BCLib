package org.betterx.bclib.api.v2.generator.config;

import org.betterx.bclib.api.v2.generator.BCLibEndBiomeSource;
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

public class BCLEndBiomeSourceConfig implements BiomeSourceConfig<BCLibEndBiomeSource> {
    public static final BCLEndBiomeSourceConfig MINECRAFT_17 = new BCLEndBiomeSourceConfig(
            EndBiomeMapType.SQUARE,
            EndBiomeGeneratorType.PAULEVS,
            true,
            1000000
    );
    public static final BCLEndBiomeSourceConfig MINECRAFT_18 = new BCLEndBiomeSourceConfig(
            EndBiomeMapType.HEX,
            EndBiomeGeneratorType.PAULEVS,
            false,
            MINECRAFT_17.innerVoidRadiusSquared
    );
    public static final BCLEndBiomeSourceConfig DEFAULT = MINECRAFT_18;

    public static final Codec<BCLEndBiomeSourceConfig> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    EndBiomeMapType.CODEC
                            .fieldOf("map_type")
                            .orElse(DEFAULT.mapVersion)
                            .forGetter(o -> o.mapVersion),
                    EndBiomeGeneratorType.CODEC
                            .fieldOf("generator_version")
                            .orElse(DEFAULT.generatorVersion)
                            .forGetter(o -> o.generatorVersion),
                    Codec.BOOL
                            .fieldOf("with_void_biomes")
                            .orElse(DEFAULT.withVoidBiomes)
                            .forGetter(o -> o.withVoidBiomes),
                    Codec.INT
                            .fieldOf("inner_void_radius_squared")
                            .orElse(DEFAULT.innerVoidRadiusSquared)
                            .forGetter(o -> o.innerVoidRadiusSquared)
            )
            .apply(instance, BCLEndBiomeSourceConfig::new));

    public BCLEndBiomeSourceConfig(
            @NotNull EndBiomeMapType mapVersion,
            @NotNull EndBiomeGeneratorType generatorVersion,
            boolean withVoidBiomes,
            int innerVoidRadiusSquared
    ) {
        this.mapVersion = mapVersion;
        this.generatorVersion = generatorVersion;
        this.withVoidBiomes = withVoidBiomes;
        this.innerVoidRadiusSquared = innerVoidRadiusSquared;
    }

    public enum EndBiomeMapType implements StringRepresentable {
        VANILLA("vanilla", (seed, biomeSize, picker) -> new HexBiomeMap(seed, biomeSize, picker)),
        SQUARE("square", (seed, biomeSize, picker) -> new SquareBiomeMap(seed, biomeSize, picker)),
        HEX("hex", (seed, biomeSize, picker) -> new HexBiomeMap(seed, biomeSize, picker));

        public static final Codec<EndBiomeMapType> CODEC = StringRepresentable.fromEnum(EndBiomeMapType::values);
        public final String name;
        public final @NotNull TriFunction<Long, Integer, BiomePicker, BiomeMap> mapBuilder;

        EndBiomeMapType(String name, @NotNull TriFunction<Long, Integer, BiomePicker, BiomeMap> mapBuilder) {
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

    public enum EndBiomeGeneratorType implements StringRepresentable {
        VANILLA("vanilla"),
        PAULEVS("paulevs");

        public static final Codec<EndBiomeGeneratorType> CODEC = StringRepresentable.fromEnum(EndBiomeGeneratorType::values);
        public final String name;

        EndBiomeGeneratorType(String name) {
            this.name = name;
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


    public final @NotNull EndBiomeMapType mapVersion;
    public final @NotNull EndBiomeGeneratorType generatorVersion;
    public final boolean withVoidBiomes;
    public final int innerVoidRadiusSquared;

    @Override
    public String toString() {
        return "BCLibEndBiomeSourceConfig{" +
                "mapVersion=" + mapVersion +
                ", generatorVersion=" + generatorVersion +
                ", withVoidBiomes=" + withVoidBiomes +
                ", innerVoidRadiusSquared=" + innerVoidRadiusSquared +
                '}';
    }

    @Override
    public boolean couldSetWithoutRepair(BiomeSourceConfig<?> input) {
        if (input instanceof BCLEndBiomeSourceConfig cfg) {
            return withVoidBiomes == cfg.withVoidBiomes && mapVersion == cfg.mapVersion;
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
        if (!(o instanceof BCLEndBiomeSourceConfig)) return false;
        BCLEndBiomeSourceConfig that = (BCLEndBiomeSourceConfig) o;
        return withVoidBiomes == that.withVoidBiomes && innerVoidRadiusSquared == that.innerVoidRadiusSquared && mapVersion == that.mapVersion && generatorVersion == that.generatorVersion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mapVersion, generatorVersion, withVoidBiomes, innerVoidRadiusSquared);
    }
}
