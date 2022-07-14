package org.betterx.ui.layout.values;

public interface SizeType {
    FitContent FIT_CONTENT = new FitContent();
    Fill FILL = new Fill();

    record Fill() implements SizeType {
    }

    record FitContent(ContentSizeSupplier contentSize) implements SizeType {
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
