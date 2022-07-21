package org.betterx.bclib.networking;

import org.betterx.bclib.client.gui.screens.UpdatesScreen;
import org.betterx.bclib.client.gui.screens.WelcomeScreen;
import org.betterx.bclib.config.Configs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class VersionCheckerClient extends VersionChecker {

    public static void presentUpdateScreen(Screen parent) {
        if (!Configs.CLIENT_CONFIG.didShowWelcomeScreen()) {
            Minecraft.getInstance().setScreen(new WelcomeScreen(parent));

        } else if (Configs.CLIENT_CONFIG.showUpdateInfo() && !VersionChecker.isEmpty()) {
            Minecraft.getInstance().setScreen(new UpdatesScreen(parent));
        }
    }
}
