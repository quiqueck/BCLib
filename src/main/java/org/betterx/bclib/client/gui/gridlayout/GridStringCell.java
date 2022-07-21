package org.betterx.bclib.client.gui.gridlayout;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Deprecated(forRemoval = true)
@Environment(EnvType.CLIENT)
public class GridStringCell extends GridCell {
    private Component text;

    GridStringCell(
            double width,
            GridLayout.GridValueType widthType,
            int height,
            GridLayout.Alignment contentAlignment,
            GridScreen parent,
            Component text
    ) {
        this(width, widthType, height, contentAlignment, parent, text, GridLayout.COLOR_WHITE);

    }

    GridStringCell(
            double width,
            GridLayout.GridValueType widthType,
            int height,
            GridLayout.Alignment contentAlignment,
            GridScreen parent,
            Component text,
            int color
    ) {
        super(width, height, widthType, null, null);
        this.text = text;
        this.customRender = (poseStack, transform, context) -> {
            if (contentAlignment == GridLayout.Alignment.CENTER) {
                GuiComponent.drawCenteredString(
                        poseStack,
                        parent.getFont(),
                        this.text,
                        transform.width / 2 + transform.left,
                        transform.top,
                        color
                );
            } else if (contentAlignment == GridLayout.Alignment.LEFT) {
                GuiComponent.drawString(poseStack, parent.getFont(), this.text, transform.left, transform.top, color);
            }
        };
    }

    public void setText(Component newText) {
        this.text = newText;
    }
}
