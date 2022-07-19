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
        VerticalStack page1 = new VerticalStack(Value.fill(), Value.fit());
        page1.addText(Value.fit(), Value.fit(), Component.literal("Page 1")).alignLeft().centerVertical();
        page1.addButton(Value.fit(), Value.fit(), Component.literal("A")).onPress((bt) -> System.out.println("A"))
             .centerHorizontal();

        VerticalStack page2 = new VerticalStack(Value.fill(), Value.fit());
        page2.addText(Value.fit(), Value.fit(), Component.literal("Page 2")).alignRight().centerVertical();
        page1.addButton(Value.fit(), Value.fit(), Component.literal("B")).onPress((bt) -> System.out.println("B"))
             .centerHorizontal();

        Container c = new Container(Value.fill(), Value.fixed(40));
        c.addChild(new Button(Value.fit(), Value.fit(), Component.literal("Containerd")).onPress(bt -> {
            System.out.println("Containerd");
        }).centerHorizontal().centerVertical());
        c.setBackgroundColor(0x77000000);
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
        rows.add(new Tabs(Value.fixed(300), Value.fit()).addPage(Component.literal("PAGE 1"), page1)
                                                        .addPage(Component.literal("PAGE 2"), page2));
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
        rows.add(c);
        rows.addCheckbox(
                Value.fitOrFill(),
                Value.fit(),
                Component.literal("Hide"),
                false
        ).onChange(
                (cb, state) -> c.setVisible(!state)
        );
        rows.addSpacer(16);
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
                10, 90, 20
        ).onChange(
                (slider, value) -> {
                    System.out.println(value);
                }
        ));
        rows.addSpacer(8);
        rows.add(new Range<>(
                Value.fill(), Value.fit(),
                Component.literal("Float"),
                10f, 90f, 20f
        ).onChange(
                (slider, value) -> {
                    System.out.println(value);
                }
        ));
        rows.addSpacer(16);
        Checkbox cb1 = new Checkbox(
                Value.fit(), Value.fit(),
                Component.literal("Some Sub-State"),
                false, true
        ).onChange(
                (checkbox, value) -> {
                    System.out.println(value);
                }
        );
        rows.add(new Checkbox(
                Value.fit(), Value.fit(),
                Component.literal("Some Selectable State"),
                false, true
        ).onChange(
                (checkbox, value) -> {
                    System.out.println(value);
                    cb1.setEnabled(value);
                }
        ));
        rows.add(cb1);
        rows.addSpacer(16);
        rows.add(new Button(
                        Value.fit(), Value.fit(),
                        Component.literal("test")
                ).onPress(
                        (bt) -> {
                            System.out.println("clicked test");
                        }
                ).centerHorizontal()
        );
        rows.addSpacer(8);
        rows.add(new Button(
                        Value.fit(), Value.fit(),
                        Component.literal("Hello World")
                ).onPress(
                        (bt) -> {
                            System.out.println("clicked hello");
                        }
                ).centerHorizontal()
        );
        rows.addFiller();

        return HorizontalStack.centered(VerticalScroll.create(Value.fit(), Value.relative(1), rows));
    }
}
