package org.betterx.worlds.together.client;

import org.betterx.worlds.together.worldPreset.client.WorldPresetsClient;

import net.fabricmc.api.ClientModInitializer;

public class WorldsTogetherClient implements ClientModInitializer {
    public void onInitializeClient() {
        WorldPresetsClient.setupClientside();
    }
}
