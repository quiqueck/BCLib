package org.betterx.bclib.integration.tips;

import net.darkhax.tipsmod.api.TipsAPI;
import net.minecraft.client.gui.screens.Screen;
import org.betterx.bclib.BCLib;
import org.betterx.bclib.integration.ModIntegration;

/**
 * Internal class to use Tips classes, you should not need to use this class. If you want to register a
 * Tips Screen for a Mod using BCLib, use {@link Tips#addTipsScreen(Class)}
 */
public class TipsIntegration extends ModIntegration {
    public TipsIntegration() {
        super("tipsmod");
    }

    @Override
    public void init() {
        try {
            TipsAPI.class.getMethod("registerTipScreen", Class.class);
            for (Class<? extends Screen> screen : Tips.screen) {
                TipsAPI.registerTipScreen(screen);
            }
        } catch (NoSuchMethodException e) {
            BCLib.LOGGER.warning("Tips Mod was detected, but doesn't have the right API. Please update Tips.");
        }
    }
}
