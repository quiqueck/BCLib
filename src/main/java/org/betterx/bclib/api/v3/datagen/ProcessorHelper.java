package org.betterx.bclib.api.v3.datagen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

import java.util.List;

public class ProcessorHelper {
    public static ResourceKey<StructureProcessorList> createKey(ResourceLocation id) {
        return ResourceKey.create(Registries.PROCESSOR_LIST, id);
    }

    public static void register(
            BootstapContext<StructureProcessorList> bootstapContext,
            ResourceKey<StructureProcessorList> resourceKey,
            List<StructureProcessor> list
    ) {
        bootstapContext.register(resourceKey, new StructureProcessorList(list));
    }
}
