package org.betterx.bclib.api.v2.levelgen.structures;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

public enum StructurePlacementType implements StringRepresentable {
    FLOOR, WALL, CEIL, LAVA, UNDER;

    public static final Codec<StructurePlacementType> CODEC = StringRepresentable.fromEnum(StructurePlacementType::values);

    public String getName() {
        return this.getSerializedName();
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name().toLowerCase();
    }
}
