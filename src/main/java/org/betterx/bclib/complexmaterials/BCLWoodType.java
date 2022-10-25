package org.betterx.bclib.complexmaterials;

import net.minecraft.world.level.block.state.properties.WoodType;

public class BCLWoodType extends WoodType {
    private String modID;

    protected BCLWoodType(String modID, String string) {
        super(string);
        this.modID = modID;
    }
}
