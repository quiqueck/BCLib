package org.betterx.ui.layout.components;

import org.betterx.ui.layout.components.render.NullRenderer;
import org.betterx.ui.layout.values.Value;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Empty extends LayoutComponent<NullRenderer, Empty> {
    public Empty(
            Value width,
            Value height
    ) {
        super(width, height, null);
    }

    @Override
    public int getContentWidth() {
        return 0;
    }

    @Override
    public int getContentHeight() {
        return 0;
    }

    @Override
    public boolean isMouseOver(double d, double e) {
        return false;
    }
}
