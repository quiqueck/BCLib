package org.betterx.bclib.api.v2.levelgen.structures;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum StructurePlacementType implements StringRepresentable {
    FLOOR, WALL, CEIL, LAVA, UNDER;

    public static final Codec<StructurePlacementType> CODEC = StringRepresentable.fromEnum(StructurePlacementType::values);

    public String getName() {
        return this.getSerializedName();
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }
}
