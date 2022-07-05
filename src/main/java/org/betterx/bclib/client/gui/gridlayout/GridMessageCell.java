package org.betterx.bclib.client.gui.gridlayout;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.Component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;

@Environment(EnvType.CLIENT)
public class GridMessageCell extends GridCell {
    private final Font font;
    private Component text;
    private MultiLineLabel lastLabel;
    private GridTransform lastTransform;

    GridMessageCell(
            double width,
            GridLayout.GridValueType widthType,
            GridLayout.Alignment contentAlignment,
            Font font,
            Component text
    ) {
        this(width, widthType, contentAlignment, font, text, GridLayout.COLOR_WHITE);
    }

    GridMessageCell(
            double width,
            GridLayout.GridValueType widthType,
            GridLayout.Alignment contentAlignment,
            Font font,
            Component text,
            int color
    ) {
        super(width, -1, widthType, null, null);
        this.font = font;
        this.text = text;

        customRender = (poseStack, transform, context) -> {
            //MultiLineLabel label = (MultiLineLabel) context;
            if (contentAlignment == GridLayout.Alignment.CENTER) {
                lastLabel.renderCentered(
                        poseStack,
                        transform.width / 2 + transform.left,
                        transform.top,
                        font.lineHeight,
                        color
                );
            } else if (contentAlignment == GridLayout.Alignment.LEFT) {
                lastLabel.renderLeftAligned(poseStack, transform.left, transform.top, font.lineHeight, color);
            }
        };
    }

    public void setText(Component text) {
        this.text = text;
        if (lastTransform != null) {
            create(lastTransform);
        }
    }

    private MultiLineLabel getLabel(GridTransform transform) {
        return lastLabel;
    }

    protected void create(GridTransform transform) {
        this.lastTransform = transform;
        this.lastLabel = MultiLineLabel.create(font, text, transform.width);
    }

    @Override
    protected GridElement buildElementAt(int left, int top, int width, List<GridElement> collector) {
        create(new GridTransform(left, top, width, 0));
        int promptLines = this.lastLabel.getLineCount() + 1;
        int height = promptLines * 9;

        return new GridElement(left, top, width, height, this::getLabel, customRender);
    }
}
