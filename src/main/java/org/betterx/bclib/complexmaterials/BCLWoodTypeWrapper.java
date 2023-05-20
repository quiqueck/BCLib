package org.betterx.bclib.complexmaterials;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;

import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.type.WoodTypeRegistry;

import java.util.Objects;

public final class BCLWoodTypeWrapper {
    public final ResourceLocation id;
    public final WoodType type;
    public final MapColor color;
    public final boolean flammable;

    protected BCLWoodTypeWrapper(ResourceLocation id, WoodType type, MapColor color, boolean flammable) {
        this.id = id;
        this.type = type;
        this.color = color;
        this.flammable = flammable;
    }

    public static Builder create(String modID, String string) {
        return new Builder(new ResourceLocation(modID, string));
    }

    public static Builder create(ResourceLocation id) {
        return new Builder(id);
    }

    public BlockSetType setType() {
        return type.setType();
    }

    public ResourceLocation id() {
        return id;
    }

    public WoodType type() {
        return type;
    }

    public MapColor color() {
        return color;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BCLWoodTypeWrapper) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.type, that.type) &&
                Objects.equals(this.color, that.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, color);
    }

    @Override
    public String toString() {
        return "BCLWoodTypeWrapper[" +
                "id=" + id + ", " +
                "type=" + type + ", " +
                "color=" + color + ']';
    }


    public static class Builder {
        private final ResourceLocation id;
        private BlockSetType setType;
        private MapColor color;
        private boolean flammable;

        public Builder(ResourceLocation id) {
            this.id = id;
            this.color = MapColor.WOOD;
            this.flammable = true;
        }

        public Builder setBlockSetType(BlockSetType setType) {
            this.setType = setType;
            return this;
        }

        public Builder setColor(MapColor color) {
            this.color = color;
            return this;
        }

        public Builder setFlammable(boolean flammable) {
            this.flammable = flammable;
            return this;
        }

        public BCLWoodTypeWrapper build() {
            if (setType == null) setType = BlockSetTypeRegistry.registerWood(id);

            final WoodType type = WoodTypeRegistry.register(id, setType);
            return new BCLWoodTypeWrapper(id, type, color, flammable);
        }
    }
}
