package org.betterx.ui.layout.values;

public record Size(int width, int height) {
    public static Size of(int size) {
        return new Size(size, size);
    }

    public static Size of(int width, int height) {
        return new Size(width, height);
    }
}
