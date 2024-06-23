package org.betterx.bclib.interfaces;

import org.betterx.wover.biome.api.data.BiomeData;
import org.betterx.wover.biome.api.data.BiomeDataRegistry;
import org.betterx.wover.state.api.WorldState;
import org.betterx.wover.surface.api.SurfaceRuleBuilder;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public interface SurfaceMaterialProvider {
    MapCodec<SurfaceMaterialProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    BlockState.CODEC.fieldOf("top").forGetter(SurfaceMaterialProvider::getTopMaterial),
                    BlockState.CODEC.fieldOf("under").forGetter(SurfaceMaterialProvider::getUnderMaterial),
                    BlockState.CODEC.fieldOf("alt").forGetter(SurfaceMaterialProvider::getAltTopMaterial),
                    Codec.BOOL.fieldOf("floor_rule").forGetter(SurfaceMaterialProvider::generateFloorRule)
            ).apply(instance, SurfaceMaterialProvider::create));

    static SurfaceMaterialProvider create(
            BlockState top,
            BlockState under,
            BlockState alt,
            boolean genFloorRule
    ) {
        return new SurfaceMaterialProvider() {
            @Override
            public BlockState getTopMaterial() {
                return top;
            }

            @Override
            public BlockState getUnderMaterial() {
                return under;
            }

            @Override
            public BlockState getAltTopMaterial() {
                return alt;
            }

            @Override
            public boolean generateFloorRule() {
                return genFloorRule;
            }

            @Override
            public SurfaceRuleBuilder surface() {
                return null;
            }
        };
    }

    BlockState getTopMaterial();
    BlockState getUnderMaterial();
    BlockState getAltTopMaterial();

    boolean generateFloorRule();
    SurfaceRuleBuilder surface();

    default void addBiomeSurfaceToEndGroup(TagBootstrapContext<Block> context, TagKey<Block> groundTag) {
        context.add(groundTag, this.getTopMaterial().getBlock());
        context.add(groundTag, this.getAltTopMaterial().getBlock());
        context.add(groundTag, this.getUnderMaterial().getBlock());
    }

    static Optional<SurfaceMaterialProvider> findSurfaceMaterialProvider(WorldGenLevel world, BlockPos pos) {
        return findSurfaceMaterialProvider(world.getBiome(pos));
    }


    static Optional<SurfaceMaterialProvider> findSurfaceMaterialProvider(@Nullable Holder<Biome> biome) {
        if (biome != null) {
            BiomeData data = WorldState
                    .registryAccess()
                    .registry(BiomeDataRegistry.BIOME_DATA_REGISTRY)
                    .orElseThrow()
                    .get(biome.unwrapKey().orElseThrow().location());

            if (data instanceof SurfaceMaterialProvider smp) {
                return Optional.of(smp);
            }
        }

        return Optional.empty();
    }
}
