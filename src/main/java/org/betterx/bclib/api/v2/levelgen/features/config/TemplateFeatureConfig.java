package org.betterx.bclib.api.v2.levelgen.features.config;

import org.betterx.bclib.api.v2.levelgen.structures.StructurePlacementType;
import org.betterx.bclib.api.v2.levelgen.structures.StructureWorldNBT;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * @deprecated Please use {@link org.betterx.bclib.api.v3.levelgen.features.config.TemplateFeatureConfig} instead
 */
public class TemplateFeatureConfig extends org.betterx.bclib.api.v3.levelgen.features.config.TemplateFeatureConfig {

    public TemplateFeatureConfig(ResourceLocation location, int offsetY, StructurePlacementType type) {
        super(location, offsetY, type);
    }

    public TemplateFeatureConfig(List<StructureWorldNBT> structures) {
        super(structures);
    }
}
