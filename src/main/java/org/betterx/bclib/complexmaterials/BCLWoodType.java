package org.betterx.bclib.complexmaterials;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

public class BCLWoodType extends WoodType {
    private String modID;

    protected BCLWoodType(String modID, String string) {
        super(string, new BlockSetType(modID + "_" + string));
        this.modID = modID;
    }

    protected BCLWoodType(String modID, String string, BlockSetType setType) {
        super(string, setType);
        this.modID = modID;
    }

    protected BCLWoodType(
            String modID,
            String string,
            BlockSetType setType,
            SoundType soundType,
            SoundType hangingSignSoundType,
            SoundEvent fenceGateClose,
            SoundEvent fenceGateOpen
    ) {
        super(string, setType, soundType, hangingSignSoundType, fenceGateClose, fenceGateOpen);
        this.modID = modID;
    }
}
