package org.betterx.ui.layout.components;

import org.betterx.ui.layout.components.render.CheckboxRenderer;
import org.betterx.ui.layout.values.Value;

import net.minecraft.network.chat.Component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Checkbox extends AbstractVanillaComponent<net.minecraft.client.gui.components.Checkbox, Checkbox> {
    @FunctionalInterface
    public interface SelectionChanged {
        void now(net.minecraft.client.gui.components.Checkbox checkBox, boolean selected);
    }

    private final boolean selected;
    private final boolean showLabel;

    private final SelectionChanged onSelectionChange;

    public Checkbox(
            Value width,
            Value height,
            Component component,
            boolean selected, boolean showLabel,
            SelectionChanged onSelectionChange
    ) {
        super(width, height, new CheckboxRenderer(), component);
        this.selected = selected;
        this.showLabel = showLabel;
        this.onSelectionChange = onSelectionChange;
    }

    public boolean selected() {
        if (vanillaComponent != null) return vanillaComponent.selected();
        return selected;
    }

    @Override
    protected net.minecraft.client.gui.components.Checkbox createVanillaComponent() {
        net.minecraft.client.gui.components.Checkbox cb = new net.minecraft.client.gui.components.Checkbox(
                0, 0,
                relativeBounds.width, relativeBounds.height,
                component,
                selected,
                showLabel
        ) {
            @Override
            public void onPress() {
                super.onPress();
                onSelectionChange.now(this, this.selected());
            }
        };

        onSelectionChange.now(cb, cb.selected());
        return cb;
    }
}
