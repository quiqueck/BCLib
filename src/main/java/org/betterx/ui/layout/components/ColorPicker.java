package org.betterx.ui.layout.components;

import org.betterx.ui.ColorUtil;
import org.betterx.ui.layout.values.Value;

import net.minecraft.network.chat.Component;

public class ColorPicker extends HorizontalStack {
    ColorSwatch swatch;
    Input input;

    public ColorPicker(Value width, Value height, Component title, int color) {
        super(width, height);
        swatch = addColorSwatch(Value.fixed(20), Value.fixed(20), color);
        input = addInput(Value.fill(), Value.fit(), title, ColorUtil.toRGBHex(color));

        //input.setFilter(ColorUtil::validHexColor);
        input.setResponder(this::inputResponder);
    }

    private void inputResponder(String value) {
        if (ColorUtil.validHexColor(value)) {
            int color = ColorUtil.parseHex(value);
            swatch.setColor(color);
            swatch.setOffsetInner(false);
            swatch.setBorderColor(ColorUtil.BLACK);
        } else {
            swatch.setOffsetInner(true);
            swatch.setBorderColor(ColorUtil.RED);
        }
    }

    @Override
    public int getContentWidth() {
        return swatch.getContentWidth() + input.getContentWidth();
    }

    @Override
    public int getContentHeight() {
        return Math.max(swatch.getContentHeight(), input.getContentHeight());
    }

    @Override
    public boolean changeFocus(boolean bl) {
        return input.changeFocus(bl);
    }
}
