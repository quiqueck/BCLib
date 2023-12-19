package org.betterx.bclib.networking;

import org.betterx.bclib.client.gui.screens.UpdatesScreen;
import org.betterx.bclib.client.gui.screens.WelcomeScreen;
import org.betterx.bclib.config.Configs;

import net.minecraft.client.gui.screens.Screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class VersionCheckerClient extends VersionChecker {

    public static void presentUpdateScreen(List<Function<Runnable, Screen>> screens) {
        if (!Configs.CLIENT_CONFIG.didShowWelcomeScreen()) {
            screens.add(WelcomeScreen::new);
        } else if (Configs.CLIENT_CONFIG.showUpdateInfo() && !VersionChecker.isEmpty()) {
            screens.add(UpdatesScreen::new);
        }
    }
}
