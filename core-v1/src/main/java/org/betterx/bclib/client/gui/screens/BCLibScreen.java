package org.betterx.bclib.client.gui.screens;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.client.gui.gridlayout.GridLayout;
import org.betterx.bclib.client.gui.gridlayout.GridRow;
import org.betterx.bclib.client.gui.gridlayout.GridScreen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
abstract class BCLibScreen extends GridScreen {
    static final ResourceLocation BCLIB_LOGO_LOCATION = new ResourceLocation(BCLib.MOD_ID, "icon.png");

    public BCLibScreen(Component title) {
        super(title);
    }

    public BCLibScreen(@Nullable Screen parent, Component title) {
        super(parent, title);
    }

    public BCLibScreen(Component title, int topPadding, boolean centerVertically) {
        super(title, topPadding, 20, centerVertically);
    }

    public BCLibScreen(@Nullable Screen parent, Component title, int topPadding, boolean centerVertically) {
        super(parent, title, topPadding, centerVertically);
    }

    public BCLibScreen(Component title, int topPadding, int sidePadding, boolean centerVertically) {
        super(title, topPadding, sidePadding, centerVertically);
    }

    public BCLibScreen(
            @Nullable Screen parent,
            Component title,
            int topPadding,
            int sidePadding,
            boolean centerVertically
    ) {
        super(parent, title, topPadding, sidePadding, centerVertically);
    }


    protected void addTitle() {
        GridRow row = grid.addRow(GridLayout.VerticalAlignment.CENTER);
        row.addFiller();
        row.addImage(BCLIB_LOGO_LOCATION, 24, GridLayout.GridValueType.CONSTANT, 24, 512, 512);
        row.addSpacer(4);
        row.addString(this.title, this);
        row.addFiller();
        grid.addSpacerRow(15);
    }
}
