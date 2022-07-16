package org.betterx.ui.layout.components;

import org.betterx.ui.layout.components.render.ButtonRenderer;
import org.betterx.ui.layout.values.Value;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Button extends AbstractVanillaComponent<net.minecraft.client.gui.components.Button, Button> {
    final net.minecraft.client.gui.components.Button.OnPress onPress;
    net.minecraft.client.gui.components.Button.OnTooltip onTooltip;

    public Button(
            Value width,
            Value height,
            net.minecraft.network.chat.Component component,
            net.minecraft.client.gui.components.Button.OnPress onPress
    ) {
        super(width, height, new ButtonRenderer(), component);
        this.onPress = onPress;
        this.onTooltip = net.minecraft.client.gui.components.Button.NO_TOOLTIP;
    }

    public Button setOnToolTip(net.minecraft.client.gui.components.Button.OnTooltip onTooltip) {
        this.onTooltip = onTooltip;
        return this;
    }

    @Override
    protected net.minecraft.client.gui.components.Button createVanillaComponent() {
        return new net.minecraft.client.gui.components.Button(
                0,
                0,
                relativeBounds.width,
                relativeBounds.height,
                component,
                onPress,
                onTooltip
        );
    }
}
