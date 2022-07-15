package org.betterx.bclib.client.gui.modmenu;

import org.betterx.bclib.BCLib;
import org.betterx.ui.layout.components.*;
import org.betterx.ui.layout.values.DynamicSize;
import org.betterx.ui.layout.values.Size;
import org.betterx.ui.vanilla.LayoutScreen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class TestScreen extends LayoutScreen {
    public TestScreen(Component component) {
        super(component);
    }

    public TestScreen(Screen parent, Component component) {
        super(parent, component);
    }

    @Override
    protected org.betterx.ui.layout.components.Component<?> initContent() {
        VerticalStack rows = new VerticalStack(DynamicSize.fit(), DynamicSize.fitOrFill());

        rows.addFiller();
        rows.add(new Text(
                        DynamicSize.fitOrFill(), DynamicSize.fixed(20),
                        Component.literal("Some Text")
                ).alignRight()
        );
        rows.add(new Text(
                        DynamicSize.fitOrFill(), DynamicSize.fixed(20),
                        Component.literal("Some other, longer Text")
                ).centerHorizontal()
        );
        rows.addSpacer(16);
        rows.add(new Image(
                        DynamicSize.fixed(24), DynamicSize.fixed(24),
                        BCLib.makeID("icon.png"),
                        new Size(512, 512)
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

        return HorizontalStack.centered(VerticalScroll.create(DynamicSize.fit(), DynamicSize.relative(1), rows));
    }
}
