package org.betterx.ui.layout.components;

import org.betterx.ui.layout.components.render.ButtonRenderer;
import org.betterx.ui.layout.values.DynamicSize;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Button extends AbstractVanillaComponent<net.minecraft.client.gui.components.Button, Button> {
    final net.minecraft.client.gui.components.Button.OnPress onPress;
    final net.minecraft.client.gui.components.Button.OnTooltip onTooltip;

    public Button(
            DynamicSize width,
            DynamicSize height,
            net.minecraft.network.chat.Component component,
            net.minecraft.client.gui.components.Button.OnPress onPress,
            net.minecraft.client.gui.components.Button.OnTooltip onTooltip
    ) {
        super(width, height, new ButtonRenderer(), component);
        this.onPress = onPress;
        this.onTooltip = onTooltip;
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
