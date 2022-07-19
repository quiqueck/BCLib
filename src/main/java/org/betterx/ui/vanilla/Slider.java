package org.betterx.ui.vanilla;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class Slider<N extends Number> extends AbstractSliderButton {
    @FunctionalInterface
    public interface SliderValueChanged<N extends Number> {
        void now(Slider<N> slider, N newValue);
    }

    protected final Component title;
    protected final N minValue;
    protected final N maxValue;

    protected final SliderValueChanged<N> onChange;

    public Slider(
            int x,
            int y,
            int width,
            int height,
            N minValue,
            N maxValue,
            N initialValue,
            SliderValueChanged<N> onChange
    ) {
        this(x, y, width, height, null, minValue, maxValue, initialValue, onChange);
    }

    public Slider(
            int x,
            int y,
            int width,
            int height,
            Component title,
            N minValue,
            N maxValue,
            N initialValue,
            SliderValueChanged<N> onChange
    ) {
        super(
                x,
                y,
                width,
                height,
                CommonComponents.EMPTY,
                (initialValue.doubleValue() - minValue.doubleValue()) / (maxValue.doubleValue() - minValue.doubleValue())
        );
        this.title = title;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.onChange = onChange;

        this.updateMessage();
    }

    public N currentValue() {
        Double res = value * (maxValue.doubleValue() - minValue.doubleValue()) + minValue.doubleValue();
        if (minValue instanceof Integer) {
            return (N) (Integer) res.intValue();
        }
        if (minValue instanceof Byte) {
            return (N) (Byte) res.byteValue();
        }
        if (minValue instanceof Short) {
            return (N) (Short) res.shortValue();
        }
        if (minValue instanceof Long) {
            return (N) (Long) res.longValue();
        }
        if (minValue instanceof Float) {
            return (N) (Float) res.floatValue();
        }
        if (minValue instanceof Double) {
            return (N) res;
        }
        throw new IllegalStateException("The Type " + minValue.getClass()
                                                              .getSimpleName() + " is not nativley supported. Please override currentValue with an implementation ofr that type");
    }

    protected String valueToString(N value) {
        if (minValue instanceof Float || minValue instanceof Double) {
            double v = value.doubleValue();
            double m = maxValue.doubleValue();
            if (m > 1000)
                return "" + (int) v;
            if (m > 100)
                return String.format("%.1f", v);
            if (m > 10)
                return String.format("%.2f", v);

            return String.format("%.4f", v);
        }
        return "" + value;
    }

    public Component getValueComponent(N value) {
        return Component.literal("" + this.valueToString(value));
    }

    @Override
    protected void updateMessage() {
        final Component valueComponent = getValueComponent(this.currentValue());
        this.setMessage(title == null ? valueComponent : title.copy()
                                                              .append(": ")
                                                              .append(valueComponent));
    }

    @Override
    protected void applyValue() {
        onChange.now(this, currentValue());
    }
}
