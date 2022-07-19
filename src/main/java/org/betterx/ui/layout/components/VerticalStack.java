package org.betterx.ui.layout.components;


import org.betterx.ui.layout.components.render.NullRenderer;
import org.betterx.ui.layout.values.Size;
import org.betterx.ui.layout.values.Value;
import org.betterx.ui.vanilla.VanillaScrollerRenderer;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class VerticalStack extends AbstractVerticalStack<VerticalStack> {
    public VerticalStack(Value width, Value height) {
        super(width, height);
    }

    public static VerticalStack centered(LayoutComponent<?, ?> c) {
        return new VerticalStack(Value.relative(1), Value.relative(1)).addFiller().add(c).addFiller();
    }

    public static VerticalStack bottom(LayoutComponent<?, ?> c) {
        return new VerticalStack(Value.relative(1), Value.relative(1)).add(c).addFiller();
    }


    @Override
    protected VerticalStack addEmpty(Value size) {
        this.components.add(new Empty(Value.fixed(0), size));
        return this;
    }

    public HorizontalStack indent(int width) {
        var h = new HorizontalStack(Value.fitOrFill(), Value.fit());
        h.addSpacer(width);
        add(h);
        return h;
    }

    @Override
    public HorizontalStack addRow(Value width, Value height) {
        return super.addRow(width, height);
    }

    @Override
    public HorizontalStack addRow() {
        return super.addRow();
    }

    @Override
    public VerticalStack add(LayoutComponent<?, ?> c) {
        return super.add(c);
    }

    @Override
    public VerticalStack addSpacer(int size) {
        return super.addSpacer(size);
    }

    @Override
    public VerticalStack addSpacer(float percentage) {
        return super.addSpacer(percentage);
    }

    @Override
    public VerticalStack addFiller() {
        return super.addFiller();
    }


    @Override
    public Button addButton(
            Value width,
            Value height,
            Component component
    ) {
        return super.addButton(width, height, component);
    }

    @Override
    public Checkbox addCheckbox(
            Value width,
            Value height,
            Component component,
            boolean selected
    ) {
        return super.addCheckbox(width, height, component, selected);
    }

    @Override
    public ColorPicker addColorPicker(Value width, Value height, Component titleOrNull, int color) {
        return super.addColorPicker(width, height, titleOrNull, color);
    }

    @Override
    public ColorSwatch addColorSwatch(Value width, Value height, int color) {
        return super.addColorSwatch(width, height, color);
    }

    @Override
    public Container addContainered(Value width, Value height, LayoutComponent<?, ?> content) {
        return super.addContainered(width, height, content);
    }

    @Override
    public HLine addHLine(Value width, Value height) {
        return super.addHLine(width, height);
    }

    @Override
    public HLine addHorizontalLine(int height) {
        return super.addHorizontalLine(height);
    }

    @Override
    public HLine addHorizontalSeparator(int height) {
        return super.addHorizontalSeparator(height);
    }

    @Override
    public Image addIcon(ResourceLocation location, Size resourceSize) {
        return super.addIcon(location, resourceSize);
    }

    @Override
    public Image addImage(Value width, Value height, ResourceLocation location, Size resourceSize) {
        return super.addImage(width, height, location, resourceSize);
    }

    @Override
    public Input addInput(Value width, Value height, Component titleOrNull, String initialValue) {
        return super.addInput(width, height, titleOrNull, initialValue);
    }

    @Override
    public MultiLineText addMultilineText(Value width, Value height, Component text) {
        return super.addMultilineText(width, height, text);
    }

    @Override
    public Range<Double> addRange(
            Value width,
            Value height,
            Component titleOrNull,
            double minValue,
            double maxValue,
            double initialValue
    ) {
        return super.addRange(width, height, titleOrNull, minValue, maxValue, initialValue);
    }

    @Override
    public Range<Float> addRange(
            Value width,
            Value height,
            Component titleOrNull,
            float minValue,
            float maxValue,
            float initialValue
    ) {
        return super.addRange(width, height, titleOrNull, minValue, maxValue, initialValue);
    }

    @Override
    public Range<Integer> addRange(
            Value width,
            Value height,
            Component titleOrNull,
            int minValue,
            int maxValue,
            int initialValue
    ) {
        return super.addRange(width, height, titleOrNull, minValue, maxValue, initialValue);
    }

    @Override
    public Text addText(Value width, Value height, Component text) {
        return super.addText(width, height, text);
    }

    @Override
    public VerticalScroll<NullRenderer, VanillaScrollerRenderer> addScrollable(LayoutComponent<?, ?> content) {
        return super.addScrollable(content);
    }

    @Override
    public VLine addVerticalLine(int width) {
        return super.addVerticalLine(width);
    }

    @Override
    public VLine addVerticalSeparator(int width) {
        return super.addVerticalSeparator(width);
    }

    @Override
    public VLine addVLine(Value width, Value height) {
        return super.addVLine(width, height);
    }

    @Override
    public VerticalScroll<NullRenderer, VanillaScrollerRenderer> addScrollable(
            Value width,
            Value height,
            LayoutComponent<?, ?> content
    ) {
        return super.addScrollable(width, height, content);
    }
}
