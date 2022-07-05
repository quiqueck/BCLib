package org.betterx.bclib.interfaces;

import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public interface ChunkGeneratorAccessor {
    Registry<StructureSet> bclib_getStructureSetsRegistry();
}
