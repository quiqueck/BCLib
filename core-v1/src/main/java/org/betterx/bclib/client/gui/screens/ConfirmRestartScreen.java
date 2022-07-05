package org.betterx.bclib.client.gui.screens;

import org.betterx.bclib.client.gui.gridlayout.GridLayout;
import org.betterx.bclib.client.gui.gridlayout.GridRow;

import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;


@Environment(EnvType.CLIENT)
public class ConfirmRestartScreen extends BCLibScreen {
    private final Component description;
    private final ConfirmRestartScreen.Listener listener;

    public ConfirmRestartScreen(ConfirmRestartScreen.Listener listener) {
        this(listener, null);
    }

    public ConfirmRestartScreen(ConfirmRestartScreen.Listener listener, Component message) {
        super(Component.translatable("title.bclib.confirmrestart"));

        this.description = message == null ? Component.translatable("message.bclib.confirmrestart") : message;
        this.listener = listener;
    }

    protected void initLayout() {
        final int BUTTON_HEIGHT = 20;

        grid.addRow().addMessage(this.description, this.font, GridLayout.Alignment.CENTER);

        grid.addSpacerRow();

        GridRow row = grid.addRow();
        row.addFiller();
        row.addButton(CommonComponents.GUI_PROCEED, BUTTON_HEIGHT, font, (button) -> {
            listener.proceed();
        });
        row.addFiller();
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Environment(EnvType.CLIENT)
    public interface Listener {
        void proceed();
    }
}
