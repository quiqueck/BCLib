package org.betterx.bclib.client.gui.gridlayout;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class GridTransform {
    public final int left;
    public final int top;
    public final int width;
    public final int height;

    GridTransform(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return "{" + "left=" + left + ", top=" + top + ", width=" + width + ", height=" + height + '}';
    }

}
