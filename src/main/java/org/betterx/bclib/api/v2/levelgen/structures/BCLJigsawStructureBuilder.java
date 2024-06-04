package org.betterx.bclib.api.v2.levelgen.structures;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;

import java.util.List;
import java.util.Optional;

public class BCLJigsawStructureBuilder extends BCLBaseStructureBuilder<JigsawStructure, BCLJigsawStructureBuilder> {
    private ResourceKey<StructureTemplatePool> startPool;
    private Optional<ResourceLocation> startJigsawName;
    private int maxDepth;
    private HeightProvider startHeight;
    private boolean useExpansionHack;
    private Optional<Heightmap.Types> projectStartToHeightmap;
    private int maxDistanceFromCenter;
    private List<PoolAliasBinding> aliasBindings;

    public BCLJigsawStructureBuilder(
            ResourceLocation structureID
    ) {
        super(structureID, null);
        this.maxDepth = 6;
        this.startHeight = ConstantHeight.of(VerticalAnchor.absolute(0));
        this.maxDistanceFromCenter = 80;
        this.useExpansionHack = false;
        this.startJigsawName = Optional.empty();
        this.projectStartToHeightmap = Optional.empty();
    }

    public BCLJigsawStructureBuilder projectStartToHeightmap(Heightmap.Types value) {
        this.projectStartToHeightmap = Optional.of(value);
        return this;
    }

    public BCLJigsawStructureBuilder maxDistanceFromCenter(int value) {
        this.maxDistanceFromCenter = value;
        return this;
    }

    public BCLJigsawStructureBuilder startJigsawName(ResourceLocation value) {
        this.startJigsawName = Optional.of(value);
        return this;
    }

    public BCLJigsawStructureBuilder useExpansionHack(boolean value) {
        this.useExpansionHack = value;
        return this;
    }

    public BCLJigsawStructureBuilder maxDepth(int value) {
        this.maxDepth = value;
        return this;
    }

    public BCLJigsawStructureBuilder startHeight(HeightProvider value) {
        this.startHeight = value;
        return this;
    }

    public BCLJigsawStructureBuilder startPool(ResourceKey<StructureTemplatePool> pool) {
        this.startPool = pool;
        return this;
    }

    public BCLJigsawStructureBuilder aliasBindings(List<PoolAliasBinding> aliasBindings) {
        this.aliasBindings = aliasBindings;
        return this;
    }

    @Override
    protected MapCodec<JigsawStructure> getCodec() {
        return JigsawStructure.CODEC;
    }

    @Override
    public BCLStructure<JigsawStructure> build() {
        if (startPool == null) {
            throw new IllegalStateException("Start pool must be set for " + this.structureID);
        }

        this.structureBuilder = (BCLStructure.StructureBuilderWithContext<JigsawStructure>) (structureSettings, ctx) -> {
            HolderGetter<StructureTemplatePool> templateGetter = ctx.lookup(Registries.TEMPLATE_POOL);
            return new JigsawStructure(
                    structureSettings,
                    templateGetter.getOrThrow(startPool),
                    startJigsawName,
                    maxDepth,
                    startHeight,
                    useExpansionHack,
                    projectStartToHeightmap,
                    maxDistanceFromCenter,
                    aliasBindings == null ? List.of() : aliasBindings,
                    JigsawStructure.DEFAULT_DIMENSION_PADDING,
                    JigsawStructure.DEFAULT_LIQUID_SETTINGS
            );
        };

        return super.build();
    }
}
