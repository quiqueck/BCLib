package org.betterx.bclib.client.gui.modmenu;

import org.betterx.bclib.integration.modmenu.ModMenuIntegration;

@Deprecated()
public class EntryPoint extends ModMenuIntegration {
    public static final Object entrypointObject = createEntrypoint(new EntryPoint());

    public EntryPoint() {
        super(MainScreen::new);
    }
}
