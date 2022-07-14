package org.betterx.ui.layout.components;

import org.betterx.ui.layout.values.DynamicSize;

public class Empty extends Component {
    public Empty(
            DynamicSize width,
            DynamicSize height
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
}
