package org.betterx.bclib.api.v3.levelgen.features.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.List;
import java.util.Optional;

public class InBiome extends PlacementFilter {
    public static final Codec<InBiome> CODEC = RecordCodecBuilder.create((instance) -> instance
            .group(
                    Codec.BOOL
                            .fieldOf("negate")
                            .orElse(false)
                            .forGetter(cfg -> cfg.negate),
                    Codec.list(ResourceLocation.CODEC)
                         .fieldOf("biomes")
                         .forGetter(cfg -> cfg.biomeIDs)
            )
            .apply(instance, InBiome::new));

    public final List<ResourceLocation> biomeIDs;
    public final boolean negate;

    protected InBiome(boolean negate, List<ResourceLocation> biomeIDs) {
        this.biomeIDs = biomeIDs;
        this.negate = negate;
    }

    public static InBiome matchingID(ResourceLocation... id) {
        return new InBiome(false, List.of(id));
    }

    public static InBiome matchingID(List<ResourceLocation> ids) {
        return new InBiome(false, ids);
    }

    public static InBiome notMatchingID(ResourceLocation... id) {
        return new InBiome(true, List.of(id));
    }

    public static InBiome notMatchingID(List<ResourceLocation> ids) {
        return new InBiome(true, ids);
    }

    @Override
    protected boolean shouldPlace(PlacementContext ctx, RandomSource random, BlockPos pos) {
        Holder<Biome> holder = ctx.getLevel().getBiome(pos);
        Optional<ResourceLocation> biomeLocation = holder.unwrapKey().map(key -> key.location());
        if (biomeLocation.isPresent()) {
            boolean contains = biomeIDs.contains(biomeLocation.get());
            return negate != contains;
        }
        return false;
    }

    @Override
    public PlacementModifierType<InBiome> type() {
        return PlacementModifiers.IN_BIOME;
    }
}
