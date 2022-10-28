package org.betterx.worlds.together.world.event;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class WorldEventsImpl {
    public static final EventImpl<OnWorldRegistryReady> WORLD_REGISTRY_READY = new EventImpl<>();
    public static final EventImpl<BeforeWorldLoad> BEFORE_WORLD_LOAD = new EventImpl<>();
    
    public static final EventImpl<OnWorldLoad> ON_WORLD_LOAD = new EventImpl<>();
    public static final EventImpl<OnFinalizeLevelStem> ON_FINALIZE_LEVEL_STEM = new EventImpl<>();
    public static final EventImpl<OnFinalizeWorldLoad> ON_FINALIZED_WORLD_LOAD = new EventImpl<>();

    public static final PatchWorldEvent PATCH_WORLD = new PatchWorldEvent();
    public static final AdaptWorldPresetSettingEvent ADAPT_WORLD_PRESET = new AdaptWorldPresetSettingEvent();

    public static final EventImpl<BeforeAddingTags> BEFORE_ADDING_TAGS = new EventImpl<>();
}
