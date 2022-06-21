package org.betterx.worlds.together.client;

import org.betterx.worlds.together.worldPreset.client.WorldPresetsClient;

public class WorldsTogetherClient {
    public static void onInitializeClient() {
        WorldPresetsClient.setupClientside();
    }
}
