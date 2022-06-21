package org.betterx.worlds.together.world.event;

class WorldEventsImpl {
    public static final EventImpl<OnWorldRegistryReady> WORLD_REGISTRY_READY = new EventImpl<>();
    public static final EventImpl<BeforeWorldLoad> BEFORE_WORLD_LOAD = new EventImpl<>();
    public static final EventImpl<BeforeServerWorldLoad> BEFORE_SERVER_WORLD_LOAD = new EventImpl<>();

    public static final EventImpl<OnWorldLoad> ON_WORLD_LOAD = new EventImpl<>();

    public static final PatchWorldEvent PATCH_WORLD = new PatchWorldEvent();
    public static final AdaptWorldPresetSettingEvent ADAPT_WORLD_PRESET = new AdaptWorldPresetSettingEvent();
}
