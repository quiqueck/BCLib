package org.betterx.bclib.integration.tips;

import net.minecraft.client.gui.screens.Screen;

import java.util.HashSet;
import java.util.Set;

/**
 * Integration, to provide a custom Screen class for Tips.
 * <p>
 * This integration allows you to use Tips without adding a dependency to your project. If the
 * Mod is installed on the Client.
 * <p>
 * You can add a custom screen from your mod by calling {@link #addTipsScreen(Class)}.
 * <p>
 * Your custom screen should be a loading-like screen with space in the bottom left for tips.
 */
public class Tips {
    static final Set<Class<? extends Screen>> screen = new HashSet<>();

    public static void addTipsScreen(Class<? extends Screen> scr) {
        screen.add(scr);
    }
}
