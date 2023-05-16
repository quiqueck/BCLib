package org.betterx.datagen.bclib.integrations;

import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiome;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeRegistry;
import org.betterx.bclib.api.v2.levelgen.biomes.BiomeAPI;
import org.betterx.bclib.api.v3.datagen.TagDataProvider;
import org.betterx.worlds.together.tag.v3.TagManager;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NullscapeBiomes extends TagDataProvider<Biome> {

    /**
     * Constructs a new {@link FabricTagProvider} with the default computed path.
     *
     * <p>Common implementations of this class are provided.
     *
     * @param output           the {@link FabricDataOutput} instance
     * @param registriesFuture the backing registry for the tag type
     */
    public NullscapeBiomes(
            FabricDataOutput output,
            CompletableFuture<HolderLookup.Provider> registriesFuture
    ) {
        super(TagManager.BIOMES, List.of("nullscape"), output, registriesFuture);
    }


    public static void ensureStaticallyLoaded() {
        BCLBiomeRegistry.register(new BCLBiome(
                new ResourceLocation("nullscape", "void_barrens"),
                BiomeAPI.BiomeType.END_VOID
        ));
    }
}
