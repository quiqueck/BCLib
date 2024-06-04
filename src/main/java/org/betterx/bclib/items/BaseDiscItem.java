package org.betterx.bclib.items;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSong;

public class BaseDiscItem {
    public static Item create(
            int comparatorOutput,
            ResourceKey<JukeboxSong> sound,
            Item.Properties settings,
            int lengthInSeconds
    ) {
        return new Item(settings.jukeboxPlayable(sound));
        //return new RecordItem(comparatorOutput, sound, settings, lengthInSeconds);
    }
}
