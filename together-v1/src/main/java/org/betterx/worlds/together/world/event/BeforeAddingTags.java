package org.betterx.worlds.together.world.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagLoader;

import java.util.List;
import java.util.Map;

public interface BeforeAddingTags {
    void apply(
            String directory,
            Map<ResourceLocation, List<TagLoader.EntryWithSource>> tagsMap
    );
}
