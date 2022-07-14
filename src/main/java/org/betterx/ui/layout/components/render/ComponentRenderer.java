package org.betterx.ui.layout.components.render;

import org.betterx.ui.layout.values.Rectangle;

public interface ComponentRenderer {
    void renderInBounds(Rectangle bounds, Rectangle clipRect);
}
