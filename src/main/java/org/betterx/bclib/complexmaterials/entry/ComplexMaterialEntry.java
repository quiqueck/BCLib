package org.betterx.bclib.complexmaterials.entry;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public abstract class ComplexMaterialEntry {
    @NotNull
    private final String suffix;

    protected ComplexMaterialEntry(String suffix) {
        this.suffix = suffix;
    }

    public String getName(String baseName) {
        return baseName + "_" + suffix;
    }

    public ResourceLocation getLocation(String modID, String baseName) {
        return new ResourceLocation(modID, getName(baseName));
    }

    public String getSuffix() {
        return suffix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplexMaterialEntry that = (ComplexMaterialEntry) o;
        return suffix.equals(that.suffix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(suffix);
    }
}
