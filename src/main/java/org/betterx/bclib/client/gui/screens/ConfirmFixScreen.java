package org.betterx.bclib.client.gui.screens;


import de.ambertation.wunderlib.ui.layout.components.Checkbox;
import de.ambertation.wunderlib.ui.layout.components.HorizontalStack;
import de.ambertation.wunderlib.ui.layout.components.LayoutComponent;
import de.ambertation.wunderlib.ui.layout.components.VerticalStack;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ConfirmFixScreen extends BCLibLayoutScreen {
    protected final ConfirmFixScreen.Listener listener;
    private final Component description;
    protected int id;

    public ConfirmFixScreen(@Nullable Screen parent, ConfirmFixScreen.Listener listener) {
        super(parent, Component.translatable("bclib.datafixer.backupWarning.title"));
        this.listener = listener;

        this.description = Component.translatable("bclib.datafixer.backupWarning.message");
    }

    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    protected LayoutComponent<?, ?> initContent() {
        VerticalStack grid = new VerticalStack(fill(), fill());
        grid.addFiller();
        grid.addMultilineText(fill(), fit(), this.description).centerHorizontal();
        grid.addSpacer(8);
        Checkbox backup = grid.addCheckbox(
                fit(), fit(),
                Component.translatable("bclib.datafixer.backupWarning.backup"),
                true
        );
        grid.addSpacer(4);
        Checkbox fix = grid.addCheckbox(
                fit(), fit(),
                Component.translatable("bclib.datafixer.backupWarning.fix"),
                true
        );
        grid.addSpacer(20);

        HorizontalStack row = grid.addRow().centerHorizontal();
        row.addButton(fit(), fit(), CommonComponents.GUI_CANCEL).onPress((button) -> onClose());
        row.addSpacer(4);
        row.addButton(fit(), fit(), CommonComponents.GUI_PROCEED)
           .onPress((button) -> this.listener.proceed(backup.isChecked(), fix.isChecked()));

        return grid;
    }

    @Environment(EnvType.CLIENT)
    public interface Listener {
        void proceed(boolean createBackup, boolean applyPatches);
    }
}
