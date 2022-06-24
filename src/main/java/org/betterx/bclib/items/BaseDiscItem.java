package org.betterx.bclib.items;

import org.betterx.bclib.interfaces.ItemModelProvider;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.RecordItem;

public class BaseDiscItem extends RecordItem implements ItemModelProvider {
    @Deprecated(forRemoval = true)
    public BaseDiscItem(int comparatorOutput, SoundEvent sound, Properties settings) {
        this(comparatorOutput, sound, settings, 30);
    }

    public BaseDiscItem(int comparatorOutput, SoundEvent sound, Properties settings, int lengthInSeconds) {
        super(comparatorOutput, sound, settings, lengthInSeconds);
    }
}
