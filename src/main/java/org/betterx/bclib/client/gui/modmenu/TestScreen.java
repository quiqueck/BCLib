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
        
        addRenderableWidget(main);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        renderDirtBackground(i);
        super.render(poseStack, i, j, f);
    }
}
