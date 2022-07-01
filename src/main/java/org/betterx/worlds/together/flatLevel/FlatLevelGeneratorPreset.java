package org.betterx.worlds.together.flatLevel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

public record FlatLevelGeneratorPreset(Holder<Item> displayItem, FlatLevelGeneratorSettings settings) {
    public static final Codec<FlatLevelGeneratorPreset> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    RegistryFixedCodec.create(Registry.ITEM_REGISTRY).fieldOf("display").forGetter(o -> o.displayItem),
                    FlatLevelGeneratorSettings.CODEC.fieldOf("settings").forGetter(o -> o.settings)
            ).apply(instance, FlatLevelGeneratorPreset::new)
    );
    public static final Codec<Holder<FlatLevelGeneratorPreset>> CODEC = RegistryFileCodec.create(
            FlatLevelPresets.FLAT_LEVEL_GENERATOR_PRESET_REGISTRY,
            DIRECT_CODEC
    );
}