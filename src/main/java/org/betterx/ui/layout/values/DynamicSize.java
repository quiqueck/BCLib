package org.betterx.ui.layout.values;

public class DynamicSize {
    private SizeType sizeType;
    private int calculatedSize;

    public DynamicSize(SizeType sizeType) {
        this.sizeType = sizeType;
        this.calculatedSize = 0;
    }

    public static DynamicSize fixed(int size) {
        return new DynamicSize(new SizeType.Fixed(size));
    }

    public static DynamicSize relative(double percentage) {
        return new DynamicSize(new SizeType.Relative(percentage));
    }

    public static DynamicSize fill() {
        return new DynamicSize(SizeType.FILL);
    }

    public static DynamicSize fit() {
        return new DynamicSize(SizeType.FIT_CONTENT);
    }

    public int calculatedSize() {
        return calculatedSize;
    }

    public int setCalculatedSize(int value) {
        calculatedSize = value;
        return value;
    }

    public DynamicSize attachComponent(SizeType.FitContent.ContentSizeSupplier c) {
        if (sizeType instanceof SizeType.FitContent fit && fit.contentSize() == null) {
            sizeType = fit.copyForSupplier(c);
        }
        return this;
    }

    public int calculateFixed() {
        return calculate(0);
    }

    public double calculateRelative() {
        if (sizeType instanceof SizeType.Relative rel) {
            return rel.percentage();
        }
        return 0;
    }

    public int calculate(int parentSize) {
        calculatedSize = 0;
        if (sizeType instanceof SizeType.Fixed fixed) {
            calculatedSize = fixed.size();
        } else if (sizeType instanceof SizeType.FitContent fit) {
            calculatedSize = fit.contentSize().get();
        } else if (sizeType instanceof SizeType.Relative rel) {
            calculatedSize = (int) (parentSize * rel.percentage());
        }

        return calculatedSize;
    }

    public int calculateOrFill(int parentSize) {
        calculatedSize = calculate(parentSize);
        if (sizeType instanceof SizeType.Fill) {
            calculatedSize = parentSize;
        }

        return calculatedSize;
    }

    public double fillWeight() {
        if (sizeType instanceof SizeType.Fill fill) {
            return 1;
        }
        return 0;
    }

    public int fill(int fillSize) {
        return fill(fillSize, fillWeight());
    }

    public int fill(int fillSize, double totalFillWeight) {
        if (sizeType instanceof SizeType.Fill) {
            calculatedSize = (int) Math.round(fillSize * (fillWeight() / totalFillWeight));
        }
        return calculatedSize;
    }

    @Override
    public String toString() {
        return "DynamicSize{" +
                "sizeType=" + sizeType.getClass().getSimpleName() +
                ", calculatedSize=" + calculatedSize +
                '}';
    }
}
