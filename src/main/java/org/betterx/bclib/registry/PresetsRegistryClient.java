package org.betterx.bclib.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class PresetsRegistryClient {
    public static void onLoad() {
        //TODO:1.19.3 Disabled for now
//        WorldPresetsClient.registerCustomizeUI(PresetsRegistry.BCL_WORLD, (createWorldScreen, worldCreationContext) -> {
//            return new WorldSetupScreen(createWorldScreen, worldCreationContext);
//        });
    }
}
