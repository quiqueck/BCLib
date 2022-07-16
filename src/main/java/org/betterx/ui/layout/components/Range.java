package org.betterx.ui.layout.components;

import org.betterx.ui.layout.components.render.RangeRenderer;
import org.betterx.ui.layout.values.Value;
import org.betterx.ui.vanilla.Slider;

import net.minecraft.network.chat.Component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Range<N extends Number> extends AbstractVanillaComponent<Slider<N>, Range<N>> {
    private final Slider.SliderValueChanged<N> onChange;
    private final N minValue;
    private final N maxValue;
    private final N initialValue;

    public Range(
            Value width,
            Value height,
            Component component,
            N minValue,
            N maxValue,
            N initialValue,
            Slider.SliderValueChanged<N> onChange
    ) {
        super(width, height, new RangeRenderer<>(), component);
        this.onChange = onChange;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.initialValue = initialValue;
    }

    public Range(
            Value width,
            Value height,
            N minValue,
            N maxValue,
            N initialValue,
            Slider.SliderValueChanged<N> onChange
    ) {
        this(width, height, null, minValue, maxValue, initialValue, onChange);
    }

    @Override
    protected Slider<N> createVanillaComponent() {
        return new Slider<>(
                0,
                0,
                relativeBounds.width,
                relativeBounds.height,
                component,
                minValue,
                maxValue,
                initialValue,
                onChange
        );
    }


    @Override
    protected Component contentComponent() {
        Slider<N> dummy = new Slider<>(
                0,
                0,
                100,
                20,
                component,
                minValue,
                maxValue,
                initialValue,
                (a, b) -> {
                }
        );
        return dummy.getValueComponent(maxValue);
    }
}
