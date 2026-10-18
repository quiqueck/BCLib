package org.betterx.worlds.together.client;

import org.betterx.worlds.together.world.event.WorldBootstrap;
import org.betterx.worlds.together.worldPreset.client.WorldPresetsClient;

import net.minecraft.client.Minecraft;

public class WorldsTogetherClient {
    public static void onInitializeClient() {
        WorldPresetsClient.setupClientside();
        WorldBootstrap.setClientRegistryAccessProvider(() -> {
            final Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null) return minecraft.level.registryAccess();
            if (minecraft.getConnection() != null) return minecraft.getConnection().registryAccess();
            return null;
        });
    }
}
