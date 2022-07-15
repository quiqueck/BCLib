package org.betterx.bclib.client.gui.modmenu;

import org.betterx.ui.layout.components.*;
import org.betterx.ui.layout.values.DynamicSize;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class TestScreen extends Screen {
    public TestScreen(Component component) {
        super(component);
    }

    Panel main;

    @Override
    protected void init() {
        super.init();
        main = new Panel(this.width, this.height);
        VerticalStack rows = new VerticalStack(DynamicSize.fit(), DynamicSize.relative(1));

        rows.addFiller();
        rows.add(new Text(
                        DynamicSize.fitOrFill(), DynamicSize.fit(),
                        Component.literal("Some Text")
                ).centerHorizontal()
        );
        rows.addSpacer(16);
        rows.add(new Range<>(
                DynamicSize.fill(), DynamicSize.fit(),
                Component.literal("Integer"),
                10, 90, 20,
                (slider, value) -> {
                    System.out.println(value);
                }
        ));
        rows.addSpacer(8);
        rows.add(new Range<>(
                DynamicSize.fill(), DynamicSize.fit(),
                Component.literal("Float"),
                10f, 90f, 20f,
                (slider, value) -> {
                    System.out.println(value);
                }
        ));
        rows.addSpacer(16);
        Checkbox cb1 = new Checkbox(
                DynamicSize.fit(), DynamicSize.fit(),
                Component.literal("Some Sub-State"),
                false, true,
                (checkbox, value) -> {
                    System.out.println(value);
                }
        );
        rows.add(new Checkbox(
                DynamicSize.fit(), DynamicSize.fit(),
                Component.literal("Some Selectable State"),
                false, true,
                (checkbox, value) -> {
                    System.out.println(value);
                    cb1.setEnabled(value);
                }
        ));
        rows.add(cb1);
        rows.addSpacer(16);
        rows.add(new Button(
                        DynamicSize.fit(), DynamicSize.fit(),
                        Component.literal("test"),
                        (bt) -> {
                            System.out.println("clicked test");
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
                            System.out.println("clicked hello");
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
