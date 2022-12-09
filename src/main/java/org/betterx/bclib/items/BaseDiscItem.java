package org.betterx.bclib.items;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.RecordItem;

public class BaseDiscItem {
    public static RecordItem create(
            int comparatorOutput,
            SoundEvent sound,
            Item.Properties settings,
            int lengthInSeconds
    ) {
        return new RecordItem(comparatorOutput, sound, settings, lengthInSeconds);
    }
}
