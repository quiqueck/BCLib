package org.betterx.bclib.api.v2.dataexchange.handler.autosync;

import org.betterx.bclib.client.gui.screens.ProgressScreen;

import net.minecraft.util.ProgressListener;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ChunkerProgress {
    private static ProgressScreen progressScreen;

    @Environment(EnvType.CLIENT)
    public static void setProgressScreen(ProgressScreen scr) {
        progressScreen = scr;
    }

    @Environment(EnvType.CLIENT)
    public static ProgressScreen getProgressScreen() {
        return progressScreen;
    }

    @Environment(EnvType.CLIENT)
    public static ProgressListener getProgressListener() {
        return progressScreen;
    }
}
