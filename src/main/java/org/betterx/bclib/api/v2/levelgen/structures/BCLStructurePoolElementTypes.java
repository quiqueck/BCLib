package org.betterx.bclib.api.v2.levelgen.structures;

import org.betterx.bclib.BCLib;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;

public class BCLStructurePoolElementTypes {
    public static final StructurePoolElementType<SingleEndPoolElement> END = register(
            BCLib.makeID("single_end_pool_element"), SingleEndPoolElement.CODEC);


    public static <P extends StructurePoolElement> StructurePoolElementType<P> register(
            ResourceLocation id,
            Codec<P> codec
    ) {
        return Registry.register(BuiltInRegistries.STRUCTURE_POOL_ELEMENT, id, () -> codec);
    }

    public static void ensureStaticallyLoaded() {
        // NO-OP
    }
}
