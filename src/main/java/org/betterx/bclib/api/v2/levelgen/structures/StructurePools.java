package org.betterx.bclib.api.v2.levelgen.structures;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

import java.util.function.Function;

public class StructurePools {
    public static ResourceKey<StructureTemplatePool> createKey(ResourceLocation id) {
        return ResourceKey.create(Registries.TEMPLATE_POOL, id);
    }

    public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(
            ResourceLocation id,
            Holder<StructureProcessorList> holder
    ) {
        return (projection) -> new SinglePoolElement(Either.left(id), holder, projection);
    }

    public static Function<StructureTemplatePool.Projection, SinglePoolElement> legacy(
            ResourceLocation id,
            Holder<StructureProcessorList> holder
    ) {
        return (projection) -> new LegacySinglePoolElement(Either.left(id), holder, projection);
    }
}
