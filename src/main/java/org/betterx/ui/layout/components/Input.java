package org.betterx.ui.layout.components;

import org.betterx.ui.layout.components.render.EditBoxRenderer;
import org.betterx.ui.layout.values.Value;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public class Input extends AbstractVanillaComponent<EditBox, Input> {
    private Consumer<String> responder;
    private BiFunction<String, Integer, FormattedCharSequence> formatter;
    private Predicate<String> filter;
    private String initialValue = "";

    public Input(
            Value width,
            Value height,
            Component component,
            String initialValue
    ) {
        super(width, height, new EditBoxRenderer(), component);
        this.initialValue = initialValue;
    }

    @Override
    protected EditBox createVanillaComponent() {
        EditBox eb = new EditBox(renderer.getFont(),
                0, 0,
                relativeBounds.width, relativeBounds.height,
                null,
                component
        );
        if (responder != null) eb.setResponder(responder);
        if (filter != null) eb.setFilter(filter);
        if (formatter != null) eb.setFormatter(formatter);
        eb.setValue(initialValue);
        eb.setBordered(true);
        eb.setEditable(true);

        return eb;
    }

    public Input setResponder(Consumer<String> consumer) {
        this.responder = consumer;
        if (vanillaComponent != null) vanillaComponent.setResponder(responder);
        return this;
    }

    public Input setFormatter(BiFunction<String, Integer, FormattedCharSequence> formatter) {
        this.formatter = formatter;
        if (vanillaComponent != null) vanillaComponent.setFormatter(formatter);
        return this;
    }

    public Input setFilter(Predicate<String> filter) {
        this.filter = filter;
        if (vanillaComponent != null) vanillaComponent.setFilter(filter);
        return this;
    }

    public String getValue() {
        if (vanillaComponent != null) return vanillaComponent.getValue();
        return "";
    }

    public Input setValue(String value) {
        if (vanillaComponent != null) vanillaComponent.setValue(value);
        else initialValue = value;

        return this;
    }
}
