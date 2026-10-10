package org.betterx.bclib.client;

import org.betterx.bclib.api.v2.datafixer.DataFixerAPI;
import de.ambertation.wover.core.api.ModCore;
import de.ambertation.wover.events.api.client.ClientWorldLifecycle;
import de.ambertation.wover.events.api.types.client.BeforeClientLoadScreen;

import net.minecraft.world.level.storage.LevelStorageSource;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Client half of {@link org.betterx.bclib.api.v2.levelgen.LevelGenEvents}. {@code BEFORE_CLIENT_LOAD_SCREEN}
 * only fires when the client opens a singleplayer world; a dedicated server patches its world through
 * {@code LevelGenEvents}' {@code WORLD_FOLDER_READY} handler instead.
 */
@Environment(EnvType.CLIENT)
public class ClientLevelGenEvents {
    public static void register() {
        ClientWorldLifecycle.BEFORE_CLIENT_LOAD_SCREEN.subscribe(ClientLevelGenEvents::patchExistingWorld, 10000);
    }

    private static void patchExistingWorld(
            LevelStorageSource.LevelStorageAccess storageAccess,
            BeforeClientLoadScreen.ContinueWith continueWith
    ) {
        //With -Dwover.suppressScreens the fixes are applied without the backup/confirm and progress screens,
        //the same way a dedicated server patches its world.
        final boolean showUI = !ModCore.suppressScreens();
        if (!DataFixerAPI.fixData(storageAccess, showUI, (b) -> continueWith.loadingScreen())) {
            continueWith.loadingScreen();
        }
    }
}
