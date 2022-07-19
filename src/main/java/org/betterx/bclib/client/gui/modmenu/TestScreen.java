package org.betterx.bclib.client.gui.modmenu;

import org.betterx.bclib.BCLib;
import org.betterx.ui.ColorUtil;
import org.betterx.ui.layout.components.*;
import org.betterx.ui.layout.values.Size;
import org.betterx.ui.layout.values.Value;
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
    protected LayoutComponent<?, ?> initContent() {
        VerticalStack rows = new VerticalStack(Value.fit(), Value.fitOrFill());

        rows.addFiller();
        rows.add(new Text(
                        Value.fitOrFill(), Value.fixed(20),
                        Component.literal("Some Text")
                ).alignRight()
        );
        rows.add(new Text(
                        Value.fitOrFill(), Value.fixed(20),
                        Component.literal("Some other, longer Text")
                ).centerHorizontal()
        );
        rows.addHorizontalSeparator(16).alignTop();
        rows.add(new Input(Value.fitOrFill(), Value.fit(), Component.literal("Input"), "0xff00ff"));
        rows.add(new ColorSwatch(Value.fit(), Value.fit(), ColorUtil.LIGHT_PURPLE).centerHorizontal());
        rows.add(new ColorPicker(
                Value.fill(),
                Value.fit(),
                Component.literal("Color"),
                ColorUtil.GREEN
        ).centerHorizontal());
        rows.add(new Text(
                        Value.fitOrFill(), Value.fixed(20),
                        Component.literal("Some blue text")
                ).centerHorizontal().setColor(ColorUtil.BLUE)
        );
        rows.addHLine(Value.fixed(32), Value.fixed(16));
        rows.add(new Image(
                        Value.fixed(24), Value.fixed(24),
                        BCLib.makeID("icon.png"),
                        new Size(512, 512)
                ).centerHorizontal()
        );
        rows.add(new MultiLineText(
                        Value.fill(), Value.fit(),
                        Component.literal(
                                "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.")
                ).setColor(ColorUtil.LIGHT_PURPLE).centerHorizontal()
        );

        rows.addHorizontalLine(16);
        rows.add(new Range<>(
                Value.fill(), Value.fit(),
                Component.literal("Integer"),
                10, 90, 20,
                (slider, value) -> {
                    System.out.println(value);
                }
        ));
        rows.addSpacer(8);
        rows.add(new Range<>(
                Value.fill(), Value.fit(),
                Component.literal("Float"),
                10f, 90f, 20f,
                (slider, value) -> {
                    System.out.println(value);
                }
        ));
        rows.addSpacer(16);
        Checkbox cb1 = new Checkbox(
                Value.fit(), Value.fit(),
                Component.literal("Some Sub-State"),
                false, true,
                (checkbox, value) -> {
                    System.out.println(value);
                }
        );
        rows.add(new Checkbox(
                Value.fit(), Value.fit(),
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
                        Value.fit(), Value.fit(),
                        Component.literal("test"),
                        (bt) -> {
                            System.out.println("clicked test");
                        }
                ).centerHorizontal()
        );
        rows.addSpacer(8);
        rows.add(new Button(
                        Value.fit(), Value.fit(),
                        Component.literal("Hello World"),
                        (bt) -> {
                            System.out.println("clicked hello");
                        }
                ).centerHorizontal()
        );
        rows.addFiller();

        return HorizontalStack.centered(VerticalScroll.create(Value.fit(), Value.relative(1), rows));
    }
}
