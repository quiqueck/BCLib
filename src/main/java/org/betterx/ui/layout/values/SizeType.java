package org.betterx.ui.layout.values;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface SizeType {
    FitContent FIT_CONTENT = new FitContent();
    FitContentOrFill FIT_CONTENT_OR_FILL = new FitContentOrFill();
    Fill FILL = new Fill();

    record Fill() implements SizeType {
    }

    class FitContentOrFill extends FitContent {
        public FitContentOrFill(ContentSizeSupplier contentSize) {
            super(contentSize);
        }

        public FitContentOrFill() {
            super();
        }

        @Override
        public FitContentOrFill copyForSupplier(ContentSizeSupplier component) {
            return new FitContentOrFill(component);
        }
    }

    class FitContent implements SizeType {
        private final ContentSizeSupplier contentSize;

        public FitContent(ContentSizeSupplier contentSize) {
            this.contentSize = contentSize;
        }

        @FunctionalInterface
        public interface ContentSizeSupplier {
            int get();
        }

        public FitContent() {
            this(null);
        }

        public FitContent copyForSupplier(ContentSizeSupplier component) {
            return new FitContent(component);
        }

        public ContentSizeSupplier contentSize() {
            return contentSize;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }

    }

    record Fixed(int size) implements SizeType {
        @Override
        public String toString() {
            return getClass().getSimpleName() + "(" + size + ")";
        }
    }

    record Relative(double percentage) implements SizeType {
        @Override
        public String toString() {
            return getClass().getSimpleName() + "(" + percentage + ")";
        }
    }
}
