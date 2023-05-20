package org.betterx.bclib.integration.modmenu;

import net.minecraft.client.gui.screens.Screen;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Integration, to provide a custom Screen for ModMenu.
 * <p>
 * This integration allows you to use ModMenu without adding a dependency to your project. If the
 * Mod is installed on the Client.
 * <p>
 * You can add a screen for your mod by calling {@link #addModMenuScreen(String, Function)}
 */
public class ModMenu {
    static final Map<String, Function<Screen, Screen>> screen = new HashMap<>();

    /**
     * registers a ModMenu entrypoint for another Mod. For Example {@code addModMenuScreen("myMod", (parent)->new Screen(parent));}
     *
     * @param modID The ID of your Mod
     * @param scr   a function that takes a parent {@link Screen} and provides the main Screen you want
     *              to show with ModMenu for your Mod.
     */
    public static void addModMenuScreen(String modID, Function<Screen, Screen> scr) {
        screen.put(modID, scr);
    }
}
