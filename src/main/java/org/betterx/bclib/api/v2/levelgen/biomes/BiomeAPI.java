package org.betterx.bclib.api.v2.levelgen.biomes;

import org.betterx.wover.biome.api.data.BiomeData;
import org.betterx.wover.biome.api.data.BiomeDataRegistry;
import org.betterx.wover.state.api.WorldState;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public class BiomeAPI {
    /**
     * Get {@link Biome} from biome on client. Used in fog rendering.
     *
     * @param biome - {@link Holder<Biome>} from client world.
     * @return {@link Holder<Biome>} or null if it was not found.
     */
    public static BiomeData getRenderBiome(Holder<Biome> biome) {
        var acc = WorldState.registryAccess();
        if (acc != null) {
            final Registry<BiomeData> reg = acc.registryOrThrow(BiomeDataRegistry.BIOME_DATA_REGISTRY);
            ResourceLocation id = biome.unwrapKey().map(ResourceKey::location).orElse(null);
            if (id != null) {
                return reg.get(id);
            }
        }

        return null;
    }
}
