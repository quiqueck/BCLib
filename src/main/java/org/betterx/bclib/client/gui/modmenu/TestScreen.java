package org.betterx.bclib.client.gui.modmenu;

import de.ambertation.wunderlib.ui.ColorHelper;
import de.ambertation.wunderlib.ui.layout.components.*;
import de.ambertation.wunderlib.ui.layout.values.Size;
import de.ambertation.wunderlib.ui.vanilla.LayoutScreen;
import org.betterx.bclib.BCLib;

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
        VerticalStack page1 = new VerticalStack(fill(), fit());
        page1.addText(fit(), fit(), Component.literal("Page 1")).alignLeft().centerVertical();
        page1.addButton(fit(), fit(), Component.literal("A1")).onPress((bt) -> System.out.println("A1"))
             .centerHorizontal();
        page1.addButton(fit(), fit(), Component.literal("A2")).onPress((bt) -> System.out.println("A2"))
             .centerHorizontal();
        page1.addButton(fit(), fit(), Component.literal("A3")).onPress((bt) -> System.out.println("A3"))
             .centerHorizontal();
        page1.addButton(fit(), fit(), Component.literal("A4")).onPress((bt) -> System.out.println("A4"))
             .centerHorizontal();
        page1.addRange(fixed(100), fit(), Component.literal("N1"), 0, 10, 5);

        VerticalStack page2 = new VerticalStack(fill(), fit());
        page2.addText(fit(), fit(), Component.literal("Page 2")).alignRight().centerVertical();
        page2.addButton(fit(), fit(), Component.literal("B1")).onPress((bt) -> System.out.println("B1"))
             .centerHorizontal();
        page2.addButton(fit(), fit(), Component.literal("B2")).onPress((bt) -> System.out.println("B2"))
             .centerHorizontal();
        page2.addButton(fit(), fit(), Component.literal("B3")).onPress((bt) -> System.out.println("B3"))
             .centerHorizontal();
        page2.addButton(fit(), fit(), Component.literal("B4")).onPress((bt) -> System.out.println("B4"))
             .centerHorizontal();
        page2.addRange(fixed(100), fit(), Component.literal("N2"), 0, 10, 5);


        Container c = new Container(fill(), fixed(40));
        c.addChild(new Button(fit(), fit(), Component.literal("Containerd")).onPress(bt -> {
            System.out.println("Containerd");
        }).centerHorizontal().centerVertical());
        c.setBackgroundColor(0x77000000);
        VerticalStack rows = new VerticalStack(fit(), fitOrFill());

        rows.addFiller();
        rows.add(new Text(
                        fitOrFill(), fixed(20),
                        Component.literal("Some Text")
                ).alignRight()
        );
        rows.add(new Text(
                        fitOrFill(), fixed(20),
                        Component.literal("Some other, longer Text")
                ).centerHorizontal()
        );
        rows.addHorizontalSeparator(16).alignTop();
        rows.add(new Tabs(fixed(300), fixed(80)).addPage(
                                                        Component.literal("PAGE 1"),
                                                        VerticalScroll.create(page1)
                                                )
                                                .addPage(
                                                        Component.literal("PAGE 2"),
                                                        VerticalScroll.create(page2)
                                                ));
        rows.add(new Input(fitOrFill(), fit(), Component.literal("Input"), "0xff00ff"));
        rows.add(new ColorSwatch(fit(), fit(), ColorHelper.LIGHT_PURPLE).centerHorizontal());
        rows.add(new ColorPicker(
                fill(),
                fit(),
                Component.literal("Color"),
                ColorHelper.GREEN
        ).centerHorizontal());
        rows.add(new Text(
                        fitOrFill(), fixed(20),
                        Component.literal("Some blue text")
                ).centerHorizontal().setColor(ColorHelper.BLUE)
        );
        rows.addHLine(fixed(32), fixed(16));
        rows.add(c);
        rows.addCheckbox(
                fitOrFill(),
                fit(),
                Component.literal("Hide"),
                false
        ).onChange(
                (cb, state) -> c.setVisible(!state)
        );
        rows.addSpacer(16);
        rows.add(new Image(
                        fixed(24), fixed(24),
                        BCLib.makeID("icon.png"),
                        new Size(512, 512)
                ).centerHorizontal()
        );
        rows.add(new MultiLineText(
                        fill(), fit(),
                        Component.literal(
                                "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.")
                ).setColor(ColorHelper.LIGHT_PURPLE).centerHorizontal()
        );

        rows.addHorizontalLine(16);
        rows.add(new Range<>(
                fill(), fit(),
                Component.literal("Integer"),
                10, 90, 20
        ).onChange(
                (slider, value) -> {
                    System.out.println(value);
                }
        ));
        rows.addSpacer(8);
        rows.add(new Range<>(
                fill(), fit(),
                Component.literal("Float"),
                10f, 90f, 20f
        ).onChange(
                (slider, value) -> {
                    System.out.println(value);
                }
        ));
        rows.addSpacer(16);
        Checkbox cb1 = new Checkbox(
                fit(), fit(),
                Component.literal("Some Sub-State"),
                false, true
        ).onChange(
                (checkbox, value) -> {
                    System.out.println(value);
                }
        );
        rows.add(new Checkbox(
                fit(), fit(),
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
                        fit(), fit(),
                        Component.literal("test")
                ).onPress(
                        (bt) -> {
                            System.out.println("clicked test");
                        }
                ).centerHorizontal()
        );
        rows.addSpacer(8);
        rows.add(new Button(
                        fit(), fit(),
                        Component.literal("Hello World")
                ).onPress(
                        (bt) -> {
                            System.out.println("clicked hello");
                        }
                ).centerHorizontal()
        );
        rows.addFiller();

        return HorizontalStack.centered(VerticalScroll.create(fit(), relative(1), rows));
    }
}
