package org.betterx.datagen.bclib.worldgen;

import org.betterx.wover.core.api.ModCore;
import org.betterx.wover.datagen.api.WoverTagProvider;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;
import org.betterx.wover.tag.api.predefined.CommonBiomeTags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public class BiomeTagProvider extends WoverTagProvider.ForBiomes {
    public BiomeTagProvider(ModCore modCore) {
        super(modCore);
    }

    @Override
    protected void prepareTags(TagBootstrapContext<Biome> context) {
        context.addOptional(
                CommonBiomeTags.IS_SMALL_END_ISLAND,
                ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("nullscape", "void_barrens"))
        );
    }
}
