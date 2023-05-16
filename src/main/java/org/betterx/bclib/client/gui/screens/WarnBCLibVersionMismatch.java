package org.betterx.bclib.client.gui.screens;

import de.ambertation.wunderlib.ui.layout.components.HorizontalStack;
import de.ambertation.wunderlib.ui.layout.components.LayoutComponent;
import de.ambertation.wunderlib.ui.layout.components.VerticalStack;

import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class WarnBCLibVersionMismatch extends BCLibLayoutScreen {
    private final Component description;
    private final Listener listener;

    public WarnBCLibVersionMismatch(Listener listener) {
        super(Component.translatable("title.bclib.bclibmissmatch"));

        this.description = Component.translatable("message.bclib.bclibmissmatch");
        this.listener = listener;
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected LayoutComponent<?, ?> initContent() {
        VerticalStack grid = new VerticalStack(fill(), fill());
        grid.addFiller();
        grid.addMultilineText(fill(), fit(), this.description).centerHorizontal();
        grid.addSpacer(20);

        HorizontalStack row = grid.addRow().centerHorizontal();
        row.addButton(fit(), fit(), CommonComponents.GUI_NO).onPress((button) -> listener.proceed(false));
        row.addSpacer(4);
        row.addButton(fit(), fit(), CommonComponents.GUI_YES).onPress((button) -> listener.proceed(true));

        grid.addFiller();
        return grid;
    }

    @Environment(EnvType.CLIENT)
    public interface Listener {
        void proceed(boolean download);
    }
}
