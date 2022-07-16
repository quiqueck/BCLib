package org.betterx.ui.vanilla;

import org.betterx.ui.layout.components.HorizontalStack;
import org.betterx.ui.layout.components.LayoutComponent;
import org.betterx.ui.layout.values.Size;
import org.betterx.ui.layout.values.Value;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

public abstract class LayoutScreenWithIcon extends LayoutScreen {
    protected final ResourceLocation icon;

    public LayoutScreenWithIcon(ResourceLocation icon, Component component) {
        this(null, icon, component);
    }

    public LayoutScreenWithIcon(
            @Nullable Screen parent,
            ResourceLocation icon,
            Component component
    ) {
        this(parent, icon, component, 20, 10, 20);
    }

    public LayoutScreenWithIcon(
            @Nullable Screen parent,
            ResourceLocation icon,
            Component component,
            int topPadding,
            int bottomPadding,
            int sidePadding
    ) {
        super(parent, component, topPadding, bottomPadding, sidePadding);
        this.icon = icon;
    }

    @Override
    protected LayoutComponent<?> buildTitle() {
        LayoutComponent<?> title = super.buildTitle();
        HorizontalStack row = new HorizontalStack(Value.fill(), Value.fit());
        row.addFiller();
        row.addIcon(icon, Size.of(512));
        row.addSpacer(4);
        row.add(title);
        row.addFiller();
        return row;
    }
}
