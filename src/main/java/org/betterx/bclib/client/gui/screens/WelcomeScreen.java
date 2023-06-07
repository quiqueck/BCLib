package org.betterx.bclib.client.gui.screens;

import de.ambertation.wunderlib.ui.ColorHelper;
import de.ambertation.wunderlib.ui.layout.components.*;
import de.ambertation.wunderlib.ui.layout.values.Size;
import org.betterx.bclib.BCLib;
import org.betterx.bclib.config.Configs;
import org.betterx.bclib.networking.VersionChecker;
import org.betterx.bclib.registry.PresetsRegistry;
import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.worldPreset.WorldPresets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public class WelcomeScreen extends BCLibLayoutScreen {
    static final ResourceLocation BETTERX_LOCATION = new ResourceLocation(BCLib.MOD_ID, "betterx.png");
    static final ResourceLocation BACKGROUND = new ResourceLocation(BCLib.MOD_ID, "header.jpg");
    static final ResourceLocation ICON_BETTERNETHER = new ResourceLocation(BCLib.MOD_ID, "icon_betternether.png");
    static final ResourceLocation ICON_BETTEREND = new ResourceLocation(BCLib.MOD_ID, "icon_betterend.png");

    public WelcomeScreen(Screen parent) {
        super(parent, translatable("bclib.welcome.title"));
    }

    @Override
    protected LayoutComponent<?, ?> initContent() {
        VerticalStack content = new VerticalStack(fill(), fit()).setDebugName("content");

        content.addImage(fill(), fit(), BACKGROUND, new Size(854 / 2, 200 / 2));
        content.addHorizontalLine(1).setColor(ColorHelper.BLACK);
        content.addSpacer(16);
        HorizontalStack headerRow = content.addRow(fit(), fit()).setDebugName("title bar").centerHorizontal();
        headerRow.addIcon(icon, Size.of(512)).setDebugName("icon");
        headerRow.addSpacer(4);
        headerRow.addText(fit(), fit(), title).centerHorizontal().setColor(ColorHelper.WHITE).setDebugName("title");
        headerRow.addImage(fixed(178 / 2), fixed(40 / 2), BETTERX_LOCATION, Size.of(178, 40)).setDebugName("betterx");
        content.addSpacer(16);

        content.addMultilineText(fill(), fit(), MultiLineText.parse(translatable("bclib.welcome.description")))
               .centerHorizontal();

        Container padContainer = new Container(fill(), fit()).setPadding(10, 0, 10, 10).setDebugName("padContainer");
        VerticalStack innerContent = new VerticalStack(fill(), fit()).setDebugName("innerContent");
        padContainer.addChild(innerContent);
        content.add(padContainer);
        if (Configs.CLIENT_CONFIG.isDonor()) {
            addSeparator(innerContent, ICON_BETTEREND);
            HorizontalStack donationRow = innerContent.addRow(relative(0.9), fit())
                                                      .setDebugName("donationRow")
                                                      .centerHorizontal();

            donationRow.addMultilineText(fill(), fit(), translatable("bclib.welcome.donation"))
                       .alignLeft()
                       .alignRight();
            donationRow.addSpacer(4);
            donationRow.addButton(
                               fit(), fit(),
                               Component.translatable("bclib.updates.donate").setStyle(Style.EMPTY.withColor(ColorHelper.YELLOW))
                       )
                       .onPress((bt) -> openLink(UpdatesScreen.DONATION_URL)).centerVertical();
        }

        addSeparator(innerContent, ICON_BETTERNETHER);

        // Do Update Checks
        Checkbox check = innerContent.addCheckbox(
                                             fit(),
                                             fit(),
                                             translatable("title.config.bclib.client.version.check"),
                                             Configs.CLIENT_CONFIG.checkVersions()
                                     )
                                     .onChange((cb, state) -> {
                                         Configs.CLIENT_CONFIG.setCheckVersions(state);
                                     });
        innerContent.addSpacer(2);
        HorizontalStack dscBox = innerContent.indent(24);
        dscBox.addMultilineText(fill(), fit(), translatable("description.config.bclib.client.version.check"))
              .setColor(ColorHelper.GRAY);
        dscBox.addSpacer(8);

        // Hide Experimental Dialog
        innerContent.addSpacer(8);
        Checkbox experimental = innerContent.addCheckbox(
                                                    fit(),
                                                    fit(),
                                                    translatable("title.config.bclib.client.ui.suppressExperimentalDialogOnLoad"),
                                                    Configs.CLIENT_CONFIG.suppressExperimentalDialog()
                                            )
                                            .onChange((cb, state) -> {
                                                Configs.CLIENT_CONFIG.setSuppressExperimentalDialog(state);
                                            });
        innerContent.addSpacer(2);
        dscBox = innerContent.indent(24);
        dscBox.addMultilineText(
                      fill(),
                      fit(),
                      translatable("description.config.bclib.client.ui.suppressExperimentalDialogOnLoad")
              )
              .setColor(ColorHelper.GRAY);
        dscBox.addSpacer(8);

        // Use BetterX WorldType
        innerContent.addSpacer(8);
        Checkbox betterx = innerContent.addCheckbox(
                                               fit(),
                                               fit(),
                                               translatable("title.config.bclib.client.ui.forceBetterXPreset"),
                                               Configs.CLIENT_CONFIG.forceBetterXPreset()
                                       )
                                       .onChange((cb, state) -> {
                                           Configs.CLIENT_CONFIG.setForceBetterXPreset(state);
                                       });
        innerContent.addSpacer(2);
        dscBox = innerContent.indent(24);
        dscBox.addMultilineText(
                      fill(), fit(),
                      translatable("warning.config.bclib.client.ui.forceBetterXPreset")
                              .setStyle(Style.EMPTY
                                      .withBold(true)
                                      .withColor(ColorHelper.RED)
                              )
                              .append(translatable(
                                      "description.config.bclib.client.ui.forceBetterXPreset").setStyle(
                                      Style.EMPTY
                                              .withBold(false)
                                              .withColor(ColorHelper.GRAY))
                              )
              )
              .setColor(ColorHelper.GRAY);
        dscBox.addSpacer(8);

        innerContent.addSpacer(16);
        innerContent.addButton(fit(), fit(), CommonComponents.GUI_PROCEED).onPress((bt) -> {
            Configs.CLIENT_CONFIG.setDidShowWelcomeScreen();
            Configs.CLIENT_CONFIG.setCheckVersions(check.isChecked());
            Configs.CLIENT_CONFIG.setSuppressExperimentalDialog(experimental.isChecked());
            Configs.CLIENT_CONFIG.setForceBetterXPreset(betterx.isChecked());
            Configs.CLIENT_CONFIG.saveChanges();

            WorldsTogether.SURPRESS_EXPERIMENTAL_DIALOG = Configs.CLIENT_CONFIG.suppressExperimentalDialog();
            if (Configs.CLIENT_CONFIG.forceBetterXPreset())
                WorldPresets.setDEFAULT(PresetsRegistry.BCL_WORLD);
            else
                WorldPresets.setDEFAULT(net.minecraft.world.level.levelgen.presets.WorldPresets.NORMAL);
            VersionChecker.startCheck(true);
            onClose();
        }).alignRight();

        return VerticalScroll.create(fill(), fill(), content).setScrollerPadding(0);
    }

    private void addSeparator(VerticalStack innerContent, ResourceLocation image) {
        final int sepWidth = (int) (427 / 1.181) / 2;
        HorizontalStack separator = new HorizontalStack(fit(), fit()).centerHorizontal();
        separator.addHLine(fixed((sepWidth - 32) / 2), fixed(32)).centerVertical();
        separator.addSpacer(1);
        separator.addImage(fixed(32), fixed(32), image, Size.of(64)).alignBottom();
        separator.addHLine(fixed((sepWidth - 32) / 2), fixed(32)).centerVertical();
        innerContent.addSpacer(16);
        innerContent.add(separator);
        innerContent.addSpacer(4);
    }

    @Override
    protected LayoutComponent<?, ?> createScreen(LayoutComponent<?, ?> content) {
        return content;
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.fill(0, 0, width, height, 0xBD343444);
//        Rectangle BANNER_UV = new Rectangle(0, 0, 427, 100);
//        Size BANNER_RESOURCE_SIZE = BANNER_UV.size();
//        Size BANNER_SIZE = BANNER_UV.sizeFromWidth(this.width);
//
//        RenderHelper.renderImage(
//                poseStack,
//                BANNER_SIZE.width(),
//                BANNER_SIZE.height(),
//                BACKGROUND,
//                BANNER_UV,
//                BANNER_RESOURCE_SIZE,
//                1.0f
//        );
    }
}
