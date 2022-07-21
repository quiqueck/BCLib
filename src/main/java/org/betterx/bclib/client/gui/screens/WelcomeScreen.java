package org.betterx.bclib.client.gui.screens;

import org.betterx.bclib.config.Configs;
import org.betterx.ui.ColorUtil;
import org.betterx.ui.layout.components.*;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class WelcomeScreen extends BCLibLayoutScreen {
    public WelcomeScreen(Screen parent) {
        super(parent, translatable("bclib.welcome.title"));
    }

    @Override
    protected LayoutComponent<?, ?> initContent() {
        VerticalStack content = new VerticalStack(fill(), fit()).setDebugName("content");
        content.addMultilineText(fill(), fit(), MultiLineText.parse(translatable("bclib.welcome.description")))
               .centerHorizontal();
        if (Configs.CLIENT_CONFIG.isDonor()) {
            content.addHorizontalSeparator(48);
            HorizontalStack donationRow = content.addRow(relative(0.9), fit())
                                                 .setDebugName("donationRow")
                                                 .centerHorizontal();

            donationRow.addMultilineText(fill(), fit(), translatable("bclib.welcome.donation"))
                       .alignLeft()
                       .alignRight();
            donationRow.addSpacer(4);
            donationRow.addButton(
                               fit(), fit(),
                               Component.translatable("bclib.updates.donate").setStyle(Style.EMPTY.withColor(ColorUtil.YELLOW))
                       )
                       .onPress((bt) -> openLink(UpdatesScreen.DONATION_URL)).centerVertical();
        }

        content.addHorizontalSeparator(48);
        content.addCheckbox(fit(), fit(), translatable("bclib.welcome.updater.title"), true)
               .onChange((cb, state) -> Configs.MAIN_CONFIG.setCheckVersions(state));
        content.addSpacer(2);
        content.indent(24)
               .addMultilineText(fill(), fit(), translatable("bclib.welcome.updater.description"))
               .setColor(ColorUtil.GRAY);

        content.addSpacer(16);
        content.addButton(fit(), fit(), CommonComponents.GUI_PROCEED).onPress((bt) -> {
            Configs.MAIN_CONFIG.setDidShowWelcomeScreen();
            onClose();
        }).alignRight();

        return VerticalScroll.create(fill(), fill(), content);
    }
}
