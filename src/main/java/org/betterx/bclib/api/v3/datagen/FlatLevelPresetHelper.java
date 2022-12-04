package org.betterx.bclib.api.v3.datagen;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class FlatLevelPresetHelper {
    public static void register(
            BootstapContext<FlatLevelGeneratorPreset> ctx,
            ResourceKey<FlatLevelGeneratorPreset> presetKey,
            ItemLike icon,
            ResourceKey<Biome> biomeKey,
            Set<ResourceKey<StructureSet>> allowedStructureSets,
            boolean addDecorations,
            boolean addLakes,
            FlatLayerInfo... flatLayerInfos
    ) {
        final HolderGetter<StructureSet> structureSets = ctx.lookup(Registries.STRUCTURE_SET);
        Objects.requireNonNull(structureSets);

        final HolderGetter<PlacedFeature> placedFeatures = ctx.lookup(Registries.PLACED_FEATURE);
        final HolderGetter<Biome> biomes = ctx.lookup(Registries.BIOME);

        HolderSet.Direct<StructureSet> structures = HolderSet.direct(
                allowedStructureSets.stream()
                                    .map(structureSets::getOrThrow)
                                    .toList());

        FlatLevelGeneratorSettings flatLevelGeneratorSettings = new FlatLevelGeneratorSettings(
                Optional.of(structures),
                biomes.getOrThrow(biomeKey),
                FlatLevelGeneratorSettings.createLakesList(placedFeatures)
        );
        if (addDecorations) {
            flatLevelGeneratorSettings.setDecoration();
        }

        if (addLakes) {
            flatLevelGeneratorSettings.setAddLakes();
        }

        for (int i = flatLayerInfos.length - 1; i >= 0; --i) {
            flatLevelGeneratorSettings.getLayersInfo().add(flatLayerInfos[i]);
        }

        ctx.register(
                presetKey,
                new FlatLevelGeneratorPreset(icon.asItem().builtInRegistryHolder(), flatLevelGeneratorSettings)
        );
    }
}
