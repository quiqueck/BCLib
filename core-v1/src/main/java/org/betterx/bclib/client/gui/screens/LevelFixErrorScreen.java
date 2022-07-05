package org.betterx.bclib.client.gui.screens;

import org.betterx.bclib.client.gui.gridlayout.GridColumn;
import org.betterx.bclib.client.gui.gridlayout.GridLayout;
import org.betterx.bclib.client.gui.gridlayout.GridRow;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class LevelFixErrorScreen extends BCLibScreen {
    private final String[] errors;
    final Listener onContinue;

    public LevelFixErrorScreen(Screen parent, String[] errors, Listener onContinue) {
        super(parent, Component.translatable("title.bclib.datafixer.error"), 10, true);
        this.errors = errors;
        this.onContinue = onContinue;
    }

    @Override
    protected void initLayout() {
        grid.addSpacerRow();
        grid.addRow()
            .addMessage(Component.translatable("message.bclib.datafixer.error"), font, GridLayout.Alignment.CENTER);
        grid.addSpacerRow(8);

        GridRow row = grid.addRow();
        row.addSpacer(10);
        GridColumn col = row.addColumn(300, GridLayout.GridValueType.CONSTANT);
        for (String error : errors) {
            Component dash = Component.literal("-");
            row = col.addRow();
            row.addString(dash, this);

            row.addSpacer(4);
            row.addString(Component.literal(error), this);
        }

        grid.addSpacerRow(8);
        row = grid.addRow();
        row.addFiller();
        row.addButton(Component.translatable("title.bclib.datafixer.error.continue"), 0.5f, 20, font, (n) -> {
            onClose();
            onContinue.doContinue(true);
        });
        row.addSpacer();
        row.addButton(CommonComponents.GUI_CANCEL, 20, font, (n) -> {
            this.minecraft.setScreen(null);
        });
        row.addFiller();
    }

    @Environment(EnvType.CLIENT)
    public interface Listener {
        void doContinue(boolean markFixed);
    }
}
