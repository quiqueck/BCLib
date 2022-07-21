package org.betterx.bclib.client.gui.screens;

import org.betterx.bclib.config.Configs;
import org.betterx.bclib.networking.VersionChecker;
import org.betterx.ui.ColorUtil;
import org.betterx.ui.layout.components.*;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
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
        Checkbox check = content.addCheckbox(
                                        fit(),
                                        fit(),
                                        translatable("bclib.welcome.updater.title"),
                                        Configs.CLIENT_CONFIG.checkVersions()
                                )
                                .onChange((cb, state) -> {
                                    Configs.CLIENT_CONFIG.setCheckVersions(state);
                                });
        content.addSpacer(2);
        HorizontalStack dscBox = content.indent(24);
        dscBox.addMultilineText(fill(), fit(), translatable("bclib.welcome.updater.description"))
              .setColor(ColorUtil.GRAY);
        dscBox.addSpacer(8);

        content.addSpacer(16);
        content.addButton(fit(), fit(), CommonComponents.GUI_PROCEED).onPress((bt) -> {
            Configs.CLIENT_CONFIG.setDidShowWelcomeScreen();
            Configs.CLIENT_CONFIG.setCheckVersions(check.isChecked());
            Configs.CLIENT_CONFIG.saveChanges();
            VersionChecker.startCheck(true);
            onClose();
        }).alignRight();

        return VerticalScroll.create(fill(), fill(), content);
    }

    @Override
    protected void renderBackground(PoseStack poseStack, int i, int j, float f) {
        GuiComponent.fill(poseStack, 0, 0, width, height, 0xBD343444);
    }
}
