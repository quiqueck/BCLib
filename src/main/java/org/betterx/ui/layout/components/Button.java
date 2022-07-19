package org.betterx.ui.layout.components;

import org.betterx.ui.layout.components.render.ButtonRenderer;
import org.betterx.ui.layout.values.Value;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Button extends AbstractVanillaComponent<net.minecraft.client.gui.components.Button, Button> {
    public static final OnTooltip NO_TOOLTIP = (button, poseStack, i, j) -> {
    };
    public static final OnPress NO_ACTION = (button) -> {
    };

    @Environment(EnvType.CLIENT)
    public interface OnTooltip {
        void onTooltip(Button button, PoseStack poseStack, int mouseX, int mouseY);
    }

    @Environment(EnvType.CLIENT)
    public interface OnPress {
        void onPress(Button button);
    }

    OnPress onPress;
    OnTooltip onTooltip;

    boolean glow = false;

    public Button(
            Value width,
            Value height,
            net.minecraft.network.chat.Component component
    ) {
        super(width, height, new ButtonRenderer(), component);
        this.onPress = NO_ACTION;
        this.onTooltip = NO_TOOLTIP;
    }

    public Button onPress(OnPress onPress) {
        this.onPress = onPress;
        return this;
    }

    public Button onToolTip(OnTooltip onTooltip) {
        this.onTooltip = onTooltip;
        return this;
    }

    @Override
    protected net.minecraft.client.gui.components.Button createVanillaComponent() {
        Button self = this;
        return new net.minecraft.client.gui.components.Button(
                0,
                0,
                relativeBounds.width,
                relativeBounds.height,
                component,
                (bt) -> onPress.onPress(self),
                (bt, stack, x, y) -> onTooltip.onTooltip(self, stack, x, y)
        );
    }

    public boolean isGlowing() {
        return glow;
    }
}
