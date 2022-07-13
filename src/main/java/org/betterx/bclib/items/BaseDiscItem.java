package org.betterx.bclib.items;

import org.betterx.bclib.BCLib;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.RecordItem;

import java.lang.reflect.Constructor;

public class BaseDiscItem extends RecordItem {
    /**
     * @param comparatorOutput
     * @param sound
     * @param settings
     * @deprecated Please use {@link BaseDiscItem#create(int, SoundEvent, Properties, int)} instead
     */
    @Deprecated(forRemoval = true)
    public BaseDiscItem(int comparatorOutput, SoundEvent sound, Properties settings) {
        this(comparatorOutput, sound, settings, 30);
    }


    /**
     * @param comparatorOutput
     * @param sound
     * @param settings
     * @param lengthInSeconds
     * @deprecated Please use {@link BaseDiscItem#create(int, SoundEvent, Properties, int)} instead
     */
    @Deprecated(forRemoval = true)
    public BaseDiscItem(int comparatorOutput, SoundEvent sound, Properties settings, int lengthInSeconds) {
        super(comparatorOutput, sound, settings);
    }

    public static RecordItem create(int comparatorOutput, SoundEvent sound, Properties settings, int lengthInSeconds) {
        for (Constructor<?> c : RecordItem.class.getConstructors()) {
            if (c.getParameterCount() == 4) {
                var types = c.getParameterTypes();
                if (types.length == 4) { //1.19.1 Constructor
                    if (
                            types[0].isAssignableFrom(int.class)
                                    && types[1].isAssignableFrom(SoundEvent.class)
                                    && types[2].isAssignableFrom(Properties.class)
                                    && types[3].isAssignableFrom(int.class)
                    ) {
                        c.setAccessible(true);
                        try {
                            return (RecordItem) c.newInstance(comparatorOutput, sound, settings, lengthInSeconds);
                        } catch (Exception e) {
                            BCLib.LOGGER.error("Failed to instantiate RecordItem", e);
                        }
                    }
                }
            } else if (c.getParameterCount() == 3) {
                var types = c.getParameterTypes();
                if (types.length == 3) { //1.19 constructor
                    if (
                            types[0].isAssignableFrom(int.class)
                                    && types[1].isAssignableFrom(SoundEvent.class)
                                    && types[2].isAssignableFrom(Properties.class)
                    ) {
                        c.setAccessible(true);
                        try {
                            return (RecordItem) c.newInstance(comparatorOutput, sound, settings);
                        } catch (Exception e) {
                            BCLib.LOGGER.error("Failed to instantiate RecordItem", e);
                        }
                    }
                }
            }
        }
        BCLib.LOGGER.error("No Constructor for RecordItems found.");
        return null;
    }
}
