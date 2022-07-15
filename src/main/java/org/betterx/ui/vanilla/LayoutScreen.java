package org.betterx.ui.vanilla;

import org.betterx.ui.layout.components.HorizontalStack;
import org.betterx.ui.layout.components.Panel;
import org.betterx.ui.layout.components.Text;
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
    protected final int topPadding;
    protected final int bottomPadding;
    protected final int sidePadding;

    public LayoutScreen(Component component) {
        this(null, component, 20, 10, 20);
    }

    public LayoutScreen(@Nullable Screen parent, Component component) {
        this(parent, component, 20, 10, 20);
    }

    public LayoutScreen(
            @Nullable Screen parent,
            Component component,
            int topPadding,
            int bottomPadding,
            int sidePadding
    ) {
        super(component);
        this.parent = parent;
        this.topPadding = topPadding;
        this.bottomPadding = topPadding;
        this.sidePadding = sidePadding;
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
        VerticalStack rows = new VerticalStack(DynamicSize.fill(), DynamicSize.fill());

        if (topPadding > 0) rows.addSpacer(topPadding);
        rows.add(new Text(DynamicSize.fill(), DynamicSize.fit(), title).centerHorizontal());
        rows.addSpacer(15);
        rows.add(content);
        if (bottomPadding > 0) rows.addSpacer(bottomPadding);

        if (sidePadding <= 0) return rows;

        HorizontalStack cols = new HorizontalStack(DynamicSize.fill(), DynamicSize.fill());
        cols.addSpacer(sidePadding);
        cols.add(rows);
        cols.addSpacer(sidePadding);

        return cols;
    }

    protected void renderBackground(PoseStack poseStack, int i, int j, float f) {
        renderDirtBackground(i);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        renderBackground(poseStack, i, j, f);
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
