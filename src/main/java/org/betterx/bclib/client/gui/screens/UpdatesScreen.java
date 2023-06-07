package org.betterx.bclib.client.gui.screens;

import de.ambertation.wunderlib.ui.ColorHelper;
import de.ambertation.wunderlib.ui.layout.components.HorizontalStack;
import de.ambertation.wunderlib.ui.layout.components.LayoutComponent;
import de.ambertation.wunderlib.ui.layout.components.VerticalStack;
import de.ambertation.wunderlib.ui.layout.values.Size;
import de.ambertation.wunderlib.ui.layout.values.Value;
import org.betterx.bclib.BCLib;
import org.betterx.bclib.config.Configs;
import org.betterx.bclib.networking.VersionChecker;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;

@Environment(EnvType.CLIENT)
public class UpdatesScreen extends BCLibLayoutScreen {
    public static final String DONATION_URL = "https://www.buymeacoffee.com/quiqueck";
    static final ResourceLocation UPDATE_LOGO_LOCATION = new ResourceLocation(BCLib.MOD_ID, "icon_updater.png");

    public UpdatesScreen(Screen parent) {
        super(parent, Component.translatable("bclib.updates.title"), 10, 10, 10);
    }
    
    public static void showUpdateUI() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> Minecraft.getInstance()
                                                         .setScreen(new UpdatesScreen(Minecraft.getInstance().screen)));
        } else {
            Minecraft.getInstance().setScreen(new UpdatesScreen(Minecraft.getInstance().screen));
        }
    }

    public ResourceLocation getUpdaterIcon(String modID) {
        if (modID.equals(BCLib.MOD_ID)) {
            return UPDATE_LOGO_LOCATION;
        }
        ModContainer nfo = FabricLoader.getInstance().getModContainer(modID).orElse(null);
        if (nfo != null) {
            CustomValue element = nfo.getMetadata().getCustomValue("bclib");
            if (element != null) {
                CustomValue.CvObject obj = element.getAsObject();
                if (obj != null) {
                    CustomValue icon = obj.get("updater_icon");
                    return new ResourceLocation(modID, icon.getAsString());
                }
            }
        }
        return null;
    }

    @Override
    protected LayoutComponent<?, ?> initContent() {
        VerticalStack rows = new VerticalStack(relative(1), fit()).centerHorizontal();
        rows.addMultilineText(fill(), fit(), Component.translatable("bclib.updates.description"))
            .centerHorizontal();

        rows.addSpacer(8);

        VersionChecker.forEachUpdate((mod, cur, updated) -> {
            ModContainer nfo = FabricLoader.getInstance().getModContainer(mod).orElse(null);
            ResourceLocation icon = getUpdaterIcon(mod);
            HorizontalStack row = rows.addRow(fixed(320), fit()).centerHorizontal();
            if (icon != null) {
                row.addImage(Value.fit(), Value.fit(), icon, Size.of(32));
                row.addSpacer(4);
            } else {
                row.addSpacer(36);
            }
            if (nfo != null) {
                row.addText(fit(), fit(), Component.literal(nfo.getMetadata().getName()))
                   .setColor(ColorHelper.WHITE);
            } else {
                row.addText(fit(), fit(), Component.literal(mod)).setColor(ColorHelper.WHITE);
            }
            row.addSpacer(4);
            row.addText(fit(), fit(), Component.literal(cur));
            row.addText(fit(), fit(), Component.literal(" -> "));
            row.addText(fit(), fit(), Component.literal(updated)).setColor(ColorHelper.GREEN);
            row.addFiller();
            boolean createdDownloadLink = false;
            if (nfo != null
                    && nfo.getMetadata().getCustomValue("bclib") != null
                    && nfo.getMetadata().getCustomValue("bclib").getAsObject().get("downloads") != null) {
                CustomValue.CvObject downloadLinks = nfo.getMetadata()
                                                        .getCustomValue("bclib")
                                                        .getAsObject()
                                                        .get("downloads")
                                                        .getAsObject();
                String link = null;
                Component name = null;
                if (Configs.CLIENT_CONFIG.preferModrinthForUpdates() && downloadLinks.get("modrinth") != null) {
                    link = downloadLinks.get("modrinth").getAsString();
                    name = Component.translatable("bclib.updates.modrinth_link");
//                    row.addButton(fit(), fit(), Component.translatable("bclib.updates.modrinth_link"))
//                       .onPress((bt) -> {
//                           this.openLink(downloadLinks.get("modrinth").getAsString());
//                       }).centerVertical();
//                    createdDownloadLink = true;
                } else if (downloadLinks.get("curseforge") != null) {
                    link = downloadLinks.get("curseforge").getAsString();
                    name = Component.translatable("bclib.updates.curseforge_link");
//                    row.addButton(fit(), fit(), Component.translatable("bclib.updates.curseforge_link"))
//                       .onPress((bt) -> {
//                           this.openLink(downloadLinks.get("curseforge").getAsString());
//                       }).centerVertical();
//                    createdDownloadLink = true;
                }

                if (link != null) {
                    createdDownloadLink = true;
                    final String finalLink = link;
                    row.addButton(fit(), fit(), name)
                       .onPress((bt) -> {
                           this.openLink(finalLink);
                       }).centerVertical();
                }
            }

            if (!createdDownloadLink && nfo != null && nfo.getMetadata().getContact().get("homepage").isPresent()) {
                row.addButton(fit(), fit(), Component.translatable("bclib.updates.download_link"))
                   .onPress((bt) -> {
                       this.openLink(nfo.getMetadata().getContact().get("homepage").get());
                   }).centerVertical();
            }
        });

        VerticalStack layout = new VerticalStack(relative(1), fill()).centerHorizontal();
        //layout.addSpacer(8);
        layout.addScrollable(rows);
        layout.addSpacer(8);


        HorizontalStack footer = layout.addRow(fill(), fit());
        if (Configs.CLIENT_CONFIG.isDonor()) {
            footer.addButton(
                          fit(),
                          fit(),
                          Component.translatable("bclib.updates.donate").setStyle(Style.EMPTY.withColor(ColorHelper.YELLOW))
                  )
                  .onPress((bt) -> openLink(DONATION_URL));
            footer.addSpacer(2);
            footer.addMultilineText(fit(), fit(), Component.translatable("bclib.updates.donate_pre"))
                  .alignBottom();
        }

        footer.addFiller();
        footer.addCheckbox(
                      fit(), fit(),
                      Component.translatable("Disable Check"),
                      !Configs.CLIENT_CONFIG.checkVersions()
              )
              .onChange((cb, state) -> {
                  Configs.CLIENT_CONFIG.setCheckVersions(!state);
                  Configs.CLIENT_CONFIG.saveChanges();
              });
        footer.addSpacer(4);
        footer.addButton(fit(), fit(), CommonComponents.GUI_DONE).onPress((bt -> {
            onClose();
        }));
        return layout;
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.fill(0, 0, width, height, 0xBD343444);
    }
}
