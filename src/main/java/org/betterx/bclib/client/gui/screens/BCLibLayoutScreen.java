package org.betterx.bclib.client.gui.screens;

import de.ambertation.wunderlib.ui.vanilla.LayoutScreenWithIcon;
import org.betterx.bclib.BCLib;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

public abstract class BCLibLayoutScreen extends LayoutScreenWithIcon {
    static final ResourceLocation BCLIB_LOGO_LOCATION = new ResourceLocation(BCLib.MOD_ID, "icon.png");
    static final ResourceLocation BCLIB_LOGO_WHITE_LOCATION = new ResourceLocation(BCLib.MOD_ID, "icon_bright.png");

    public BCLibLayoutScreen(
            Component component
    ) {
        super(BCLIB_LOGO_LOCATION, component);
    }

    public BCLibLayoutScreen(
            @Nullable Screen parent,
            Component component
    ) {
        super(parent, BCLIB_LOGO_LOCATION, component);
    }

    public BCLibLayoutScreen(
            @Nullable Screen parent,
            Component component,
            int topPadding,
            int bottomPadding,
            int sidePadding
    ) {
        super(parent, BCLIB_LOGO_LOCATION, component, topPadding, bottomPadding, sidePadding);
    }
}
