package org.betterx.bclib.client.gui.modmenu;

import org.betterx.ui.layout.components.Button;
import org.betterx.ui.layout.components.HorizontalStack;
import org.betterx.ui.layout.components.Panel;
import org.betterx.ui.layout.components.VerticalStack;
import org.betterx.ui.layout.values.DynamicSize;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TestScreen extends Screen {
    public TestScreen(Component component) {
        super(component);
    }

    Panel main;

    @Override
    protected void init() {
        super.init();
        main = new Panel(this.width, this.height);
        HorizontalStack<?> columns = new HorizontalStack<>(DynamicSize.relative(1), DynamicSize.relative(1));
        VerticalStack<?> rows = new VerticalStack<>(DynamicSize.fit(), DynamicSize.relative(1));
//        columns.add(new Empty(DynamicSize.fill(), DynamicSize.fill()));
//        columns.add(rows);
//        columns.add(new Empty(DynamicSize.fill(), DynamicSize.fill()));

        rows.addFiller();
        rows.add(new Button(
                        DynamicSize.fit(), DynamicSize.fit(),
                        Component.literal("test"),
                        (bt) -> {
                        },
                        (bt, pose, x, y) -> {
                        }
                ).centerHorizontal()
        );
        rows.addSpacer(8);
        rows.add(new Button(
                        DynamicSize.fit(), DynamicSize.fit(),
                        Component.literal("Hello World"),
                        (bt) -> {
                        },
                        (bt, pose, x, y) -> {
                        }
                ).centerHorizontal()
        );
        rows.addFiller();
        main.setChild(HorizontalStack.centered(rows));
        main.calculateLayout();
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        renderDirtBackground(i);
        main.render(poseStack);
        super.render(poseStack, i, j, f);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        boolean res = super.mouseClicked(x, y, button);
        if (!res) {
            res = main.mouseClicked(x, y, button);
        }
        return res;
    }

    @Override
    public boolean mouseReleased(double d, double e, int i) {
        boolean res = super.mouseReleased(d, e, i);
        if (!res) {
            res = main.mouseReleased(d, e, i);
        }
        return res;
    }

    @Override
    public void mouseMoved(double d, double e) {
        super.mouseMoved(d, e);
        main.mouseMoved(d, e);
    }

    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g) {
        boolean res = super.mouseDragged(d, e, i, f, g);
        if (!res) {
            res = main.mouseDragged(d, e, i, f, g);
        }
        return res;
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        boolean res = super.mouseScrolled(d, e, f);
        if (!res) {
            res = main.mouseScrolled(d, e, f);
        }
        return res;
    }
}
