package org.betterx.bclib.registry;

import org.betterx.bclib.client.gui.screens.WorldSetupScreen;
import org.betterx.worlds.together.worldPreset.client.WorldPresetsClient;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class PresetsRegistryClient {
    public static void onLoad() {
        WorldPresetsClient.registerCustomizeUI(PresetsRegistry.BCL_WORLD, (createWorldScreen, worldCreationContext) -> {
            return new WorldSetupScreen(createWorldScreen, worldCreationContext);
        });

        WorldPresetsClient.registerCustomizeUI(
                PresetsRegistry.BCL_WORLD_LARGE,
                (createWorldScreen, worldCreationContext) -> {
                    return new WorldSetupScreen(createWorldScreen, worldCreationContext);
                }
        );

        WorldPresetsClient.registerCustomizeUI(
                PresetsRegistry.BCL_WORLD_AMPLIFIED,
                (createWorldScreen, worldCreationContext) -> {
                    return new WorldSetupScreen(createWorldScreen, worldCreationContext);
                }
        );
    }
}
