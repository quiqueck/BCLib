package org.betterx.ui.layout.values;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Rectangle {
    public static final Rectangle ZERO = new Rectangle(0, 0, 0, 0);
    public final int left;
    public final int top;
    public final int width;
    public final int height;

    public Rectangle(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    public int right() {
        return left + width;
    }

    public int bottom() {
        return top + height;
    }

    public Size size() {
        return new Size(width, height);
    }

    public Rectangle movedBy(int left, int top) {
        return new Rectangle(this.left + left, this.top + top, this.width, this.height);
    }

    public Rectangle movedBy(int left, int top, int deltaWidth, int deltaHeight) {
        return new Rectangle(this.left + left, this.top + top, this.width + deltaWidth, this.height + deltaHeight);
    }

    public boolean overlaps(Rectangle r) {
        return this.left < r.right() && this.right() > r.left &&
                this.top < r.bottom() && this.bottom() > r.top;
    }

    public boolean contains(int x, int y) {
        return x >= left && x <= right() && y >= top && y <= bottom();
    }

    public Rectangle intersect(Rectangle r) {
        if (!overlaps(r)) return ZERO;
        int left = Math.max(this.left, r.left);
        int top = Math.max(this.top, r.top);
        int right = Math.min(this.right(), r.right());
        int bottom = Math.min(this.bottom(), r.bottom());

        return new Rectangle(left, top, right - left, bottom - top);
    }

    @Override
    public String toString() {
        return "rectangle{" +
                left +
                "x" + top +
                ", " + right() +
                "x" + bottom() +
                " [" + width +
                "x" + height +
                "]}";
    }
}
