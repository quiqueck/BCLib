package org.betterx.bclib.client.gui.screens;

import de.ambertation.wunderlib.ui.ColorHelper;
import de.ambertation.wunderlib.ui.layout.components.HorizontalStack;
import de.ambertation.wunderlib.ui.layout.components.LayoutComponent;
import de.ambertation.wunderlib.ui.layout.components.VerticalStack;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class LevelFixErrorScreen extends BCLibLayoutScreen {
    private final String[] errors;
    final Listener onContinue;

    public LevelFixErrorScreen(Screen parent, String[] errors, Listener onContinue) {
        super(parent, Component.translatable("title.bclib.datafixer.error"), 10, 10, 10);
        this.errors = errors;
        this.onContinue = onContinue;
    }


    @Override
    protected LayoutComponent<?, ?> initContent() {
        VerticalStack grid = new VerticalStack(fill(), fill());
        grid.addSpacer(4);
        grid.addMultilineText(fill(), fit(), Component.translatable("message.bclib.datafixer.error"))
            .centerHorizontal();
        grid.addSpacer(8);

        HorizontalStack row = new HorizontalStack(fill(), fit());
        row.addSpacer(10);
        VerticalStack col = row.addColumn(fixed(300), fit());
        grid.addScrollable(row);

        for (String error : errors) {
            Component dash = Component.literal("-");
            row = col.addRow();
            row.addText(fit(), fit(), dash);

            row.addSpacer(4);
            row.addText(fit(), fit(), Component.literal(error)).setColor(ColorHelper.RED);
        }
        grid.addSpacer(8);

        row = grid.addRow().centerHorizontal();
        row.addButton(
                fit(), fit(),
                Component.translatable("title.bclib.datafixer.error.continue")
        ).setAlpha(0.5f).onPress((n) -> {
            onClose();
            onContinue.doContinue(true);
        });
        row.addSpacer(4);
        row.addButton(
                fit(), fit(),
                CommonComponents.GUI_CANCEL
        ).onPress((n) -> {
            this.minecraft.setScreen(null);
        });


        return grid;
    }

    @Environment(EnvType.CLIENT)
    public interface Listener {
        void doContinue(boolean markFixed);
    }
}
