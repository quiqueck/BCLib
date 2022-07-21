package org.betterx.ui.vanilla;

import org.betterx.ui.ColorUtil;
import org.betterx.ui.layout.components.*;
import org.betterx.ui.layout.values.Value;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
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

    protected abstract LayoutComponent<?, ?> initContent();

    protected void openLink(String uri) {
        ConfirmLinkScreen cls = new ConfirmLinkScreen(bl -> {
            if (bl) {
                Util.getPlatform().openUri(uri);
            }
            this.minecraft.setScreen(this);
        }, uri, true);

        Minecraft.getInstance().setScreen(cls);
    }

    @Override
    protected final void init() {
        super.init();
        main = new Panel(this.width, this.height);
        main.setChild(addTitle(initContent()));

        main.calculateLayout();
        addRenderableWidget(main);
    }

    protected LayoutComponent<?, ?> buildTitle() {
        var text = new Text(fit(), fit(), title).centerHorizontal()
                                                .setColor(ColorUtil.WHITE)
                                                .setDebugName("title");
        return text;
    }

    protected LayoutComponent<?, ?> addTitle(LayoutComponent<?, ?> content) {
        VerticalStack rows = new VerticalStack(fill(), fill()).setDebugName("title stack");

        if (topPadding > 0) rows.addSpacer(topPadding);
        rows.add(buildTitle());
        rows.addSpacer(15);
        rows.add(content);
        if (bottomPadding > 0) rows.addSpacer(bottomPadding);

        if (sidePadding <= 0) return rows;

        HorizontalStack cols = new HorizontalStack(fill(), fill()).setDebugName("padded side");
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

    public static Value fit() {
        return Value.fit();
    }

    public static Value fitOrFill() {
        return Value.fitOrFill();
    }

    public static Value fill() {
        return Value.fill();
    }

    public static Value fixed(int size) {
        return Value.fixed(size);
    }

    public static Value relative(double percentage) {
        return Value.relative(percentage);
    }
}
