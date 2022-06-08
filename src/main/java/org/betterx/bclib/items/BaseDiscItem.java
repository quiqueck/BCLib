package org.betterx.bclib.items;

import org.betterx.bclib.interfaces.ItemModelProvider;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.RecordItem;

public class BaseDiscItem extends RecordItem implements ItemModelProvider {
    public BaseDiscItem(int comparatorOutput, SoundEvent sound, Properties settings) {
        super(comparatorOutput, sound, settings);
    }
}
