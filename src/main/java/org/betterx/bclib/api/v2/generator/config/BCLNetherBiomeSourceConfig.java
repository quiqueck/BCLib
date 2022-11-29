package org.betterx.bclib.api.v2.generator.config;

import org.betterx.bclib.api.v2.generator.BCLibNetherBiomeSource;
import org.betterx.bclib.api.v2.generator.map.hex.HexBiomeMap;
import org.betterx.bclib.api.v2.generator.map.square.SquareBiomeMap;
import org.betterx.worlds.together.biomesource.config.BiomeSourceConfig;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class BCLNetherBiomeSourceConfig implements BiomeSourceConfig<BCLibNetherBiomeSource> {
    public static final BCLNetherBiomeSourceConfig VANILLA = new BCLNetherBiomeSourceConfig(
            NetherBiomeMapType.VANILLA,
            256,
            86,
            false,
            false
    );
    public static final BCLNetherBiomeSourceConfig MINECRAFT_17 = new BCLNetherBiomeSourceConfig(
            NetherBiomeMapType.SQUARE,
            256,
            86,
            true,
            false
    );
    public static final BCLNetherBiomeSourceConfig MINECRAFT_18 = new BCLNetherBiomeSourceConfig(
            NetherBiomeMapType.HEX,
            MINECRAFT_17.biomeSize,
            MINECRAFT_17.biomeSizeVertical,
            MINECRAFT_17.useVerticalBiomes,
            MINECRAFT_17.amplified
    );

    public static final BCLNetherBiomeSourceConfig MINECRAFT_18_LARGE = new BCLNetherBiomeSourceConfig(
            NetherBiomeMapType.HEX,
            MINECRAFT_18.biomeSize * 4,
            MINECRAFT_18.biomeSizeVertical * 2,
            MINECRAFT_18.useVerticalBiomes,
            MINECRAFT_17.amplified
    );

    public static final BCLNetherBiomeSourceConfig MINECRAFT_18_AMPLIFIED = new BCLNetherBiomeSourceConfig(
            NetherBiomeMapType.HEX,
            MINECRAFT_18.biomeSize,
            128,
            true,
            true
    );

    public static final BCLNetherBiomeSourceConfig DEFAULT = MINECRAFT_18;

    public static final Codec<BCLNetherBiomeSourceConfig> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    BCLNetherBiomeSourceConfig.NetherBiomeMapType.CODEC
                            .fieldOf("map_type")
                            .orElse(DEFAULT.mapVersion)
                            .forGetter(o -> o.mapVersion),
                    Codec.INT.fieldOf("biome_size").orElse(DEFAULT.biomeSize).forGetter(o -> o.biomeSize),
                    Codec.INT.fieldOf("biome_size_vertical")
                             .orElse(DEFAULT.biomeSizeVertical)
                             .forGetter(o -> o.biomeSizeVertical),
                    Codec.BOOL.fieldOf("use_vertical_biomes")
                              .orElse(DEFAULT.useVerticalBiomes)
                              .forGetter(o -> o.useVerticalBiomes),
                    Codec.BOOL.fieldOf("amplified")
                              .orElse(DEFAULT.amplified)
                              .forGetter(o -> o.amplified)
            )
            .apply(instance, BCLNetherBiomeSourceConfig::new));
    public final @NotNull NetherBiomeMapType mapVersion;
    public final int biomeSize;
    public final int biomeSizeVertical;

    public final boolean useVerticalBiomes;
    public final boolean amplified;

    public BCLNetherBiomeSourceConfig(
            @NotNull NetherBiomeMapType mapVersion,
            int biomeSize,
            int biomeSizeVertical,
            boolean useVerticalBiomes,
            boolean amplified
    ) {
        this.mapVersion = mapVersion;
        this.biomeSize = Mth.clamp(biomeSize, 1, 8192);
        this.biomeSizeVertical = Mth.clamp(biomeSizeVertical, 1, 8192);
        this.useVerticalBiomes = useVerticalBiomes;
        this.amplified = amplified;
    }

    @Override
    public String toString() {
        return "BCLibNetherBiomeSourceConfig{" +
                "mapVersion=" + mapVersion +
                ", useVerticalBiomes=" + useVerticalBiomes +
                ", amplified=" + amplified +
                ", biomeSize=" + biomeSize +
                ", biomeSizeVertical=" + biomeSizeVertical +
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
        public final MapBuilderFunction mapBuilder;

        NetherBiomeMapType(String name, MapBuilderFunction mapBuilder) {
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
