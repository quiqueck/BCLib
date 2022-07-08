package org.betterx.worlds.together.world.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;

import java.util.Map;

public interface BeforeAddingTags {
    void apply(
            String directory,
            Map<ResourceLocation, Tag.Builder> tagsMap
    );
}
