package org.betterx.ui.layout.components;

import org.betterx.ui.layout.components.render.CheckboxRenderer;
import org.betterx.ui.layout.values.Value;

import net.minecraft.network.chat.Component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Checkbox extends AbstractVanillaComponent<net.minecraft.client.gui.components.Checkbox, Checkbox> {
    public static SelectionChanged IGNORE_CHANGE = (a, b) -> {
    };

    @FunctionalInterface
    public interface SelectionChanged {
        void now(Checkbox checkBox, boolean selected);
    }

    private final boolean selected;
    private final boolean showLabel;

    private SelectionChanged onSelectionChange;

    public Checkbox(
            Value width,
            Value height,
            Component component,
            boolean selected, boolean showLabel
    ) {
        super(width, height, new CheckboxRenderer(), component);
        onSelectionChange = IGNORE_CHANGE;
        this.selected = selected;
        this.showLabel = showLabel;
    }

    public Checkbox onChange(SelectionChanged onSelectionChange) {
        this.onSelectionChange = onSelectionChange;
        return this;
    }

    public boolean isChecked() {
        if (vanillaComponent != null) return vanillaComponent.selected();
        return selected;
    }

    @Override
    protected net.minecraft.client.gui.components.Checkbox createVanillaComponent() {
        Checkbox self = this;
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
                onSelectionChange.now(self, this.selected());
            }
        };

        onSelectionChange.now(this, cb.selected());
        return cb;
    }
}
