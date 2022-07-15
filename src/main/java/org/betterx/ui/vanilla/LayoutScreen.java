package org.betterx.ui.vanilla;

import org.betterx.ui.layout.components.Panel;
import org.betterx.ui.layout.components.VerticalStack;
import org.betterx.ui.layout.values.DynamicSize;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class LayoutScreen extends Screen {
    public LayoutScreen(Component component) {
        this(null, component);
    }

    public LayoutScreen(@Nullable Screen parent, Component component) {
        super(component);
        this.parent = parent;
    }

    @Nullable
    protected Panel main;

    @Nullable
    public final Screen parent;

    protected abstract org.betterx.ui.layout.components.Component<?> initContent();

    @Override
    protected final void init() {
        super.init();
        main = new Panel(this.width, this.height);
        main.setChild(addTitle(initContent()));

        main.calculateLayout();
        addRenderableWidget(main);
    }

    protected org.betterx.ui.layout.components.Component<?> addTitle(org.betterx.ui.layout.components.Component<?> content) {
        VerticalStack rows = new VerticalStack(DynamicSize.relative(1), DynamicSize.relative(1));

        //rows.add(this.title, GridLayout.Alignment.CENTER, this);
        rows.addSpacer(15);
        rows.add(content);
        return rows;
    }


    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        renderDirtBackground(i);
        super.render(poseStack, i, j, f);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
