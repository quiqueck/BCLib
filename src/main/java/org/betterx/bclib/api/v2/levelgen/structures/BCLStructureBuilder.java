package org.betterx.bclib.api.v2.levelgen.structures;

import com.mojang.serialization.Codec;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public class BCLStructureBuilder<S extends Structure> extends BCLBaseStructureBuilder<S, BCLStructureBuilder<S>> {
    private Codec<S> codec;

    private BCLStructureBuilder(
            ResourceLocation structureID,
            BCLStructure.StructureBuilder<S> structureBuilder
    ) {
        super(structureID, structureBuilder);

        if (structureBuilder instanceof BCLStructure.StructureCodecProvider sctx) {
            this.codec = sctx.getCodec();
        } else {
            this.codec = Structure.simpleCodec((settings) -> structureBuilder.apply(settings));
        }
    }

    public static BCLJigsawStructureBuilder jigsaw(
            ResourceLocation structureID
    ) {
        return new BCLJigsawStructureBuilder(structureID);
    }

    public static <S extends Structure> BCLStructureBuilder<S> start(
            ResourceLocation structureID,
            BCLStructure.StructureBuilderWithContext<S> structureBuilder
    ) {
        return new BCLStructureBuilder<>(structureID, structureBuilder);
    }

    public static <S extends Structure> BCLStructureBuilder<S> start(
            ResourceLocation structureID,
            BCLStructure.StructureBuilder<S> structureBuilder
    ) {
        return new BCLStructureBuilder<>(structureID, structureBuilder);
    }

    public BCLStructureBuilder<S> codec(Codec<S> value) {
        this.codec = value;
        return this;
    }

    @Override
    protected Codec<S> getCodec() {
        return codec;
    }


    public static void registerUnbound(BootstapContext<Structure> context) {
        BCLBaseStructureBuilder.registerUnbound(context);
    }

    public static void registerUnboundSets(BootstapContext<StructureSet> context) {
        BCLBaseStructureBuilder.registerUnboundSets(context);
    }
}
