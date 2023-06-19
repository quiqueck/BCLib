package org.betterx.datagen.bclib.integrations;

import org.betterx.worlds.together.tag.v3.CommonBiomeTags;
import org.betterx.worlds.together.tag.v3.TagManager;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class NullscapeBiomes {
    public static void ensureStaticallyLoaded() {
        TagManager.BIOMES.addOptional(
                CommonBiomeTags.IS_SMALL_END_ISLAND,
                ResourceKey.create(Registries.BIOME, new ResourceLocation("nullscape", "void_barrens"))
        );
    }
}
