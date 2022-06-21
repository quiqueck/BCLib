package org.betterx.bclib.client.gui.screens;

import org.betterx.bclib.api.v2.dataexchange.handler.autosync.HelloClient;
import org.betterx.bclib.client.gui.gridlayout.GridCheckboxCell;
import org.betterx.bclib.client.gui.gridlayout.GridLayout;
import org.betterx.bclib.client.gui.gridlayout.GridRow;
import org.betterx.worlds.together.util.ModUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SyncFilesScreen extends BCLibScreen {
    private final Component description;
    private final SyncFilesScreen.Listener listener;
    private final boolean hasConfigFiles;
    private final boolean hasFiles;
    private final boolean hasMods;
    private final boolean shouldDelete;
    private final HelloClient.IServerModMap serverInfo;

    public SyncFilesScreen(
            int modFiles,
            int configFiles,
            int singleFiles,
            int folderFiles,
            int deleteFiles,
            HelloClient.IServerModMap serverInfo,
            Listener listener
    ) {
        super(Component.translatable("title.bclib.syncfiles"));

        this.serverInfo = serverInfo;
        this.description = Component.translatable("message.bclib.syncfiles");
        this.listener = listener;

        this.hasConfigFiles = configFiles > 0;
        this.hasFiles = singleFiles + folderFiles > 0;
        this.hasMods = modFiles > 0;
        this.shouldDelete = deleteFiles > 0;
    }

    protected void initLayout() {
        final int BUTTON_HEIGHT = 20;

        grid.addRow()
            .addMessage(this.description, this.font, GridLayout.Alignment.CENTER);

        grid.addSpacerRow(10);

        GridRow row;


        final GridCheckboxCell mods;
        row = grid.addRow();
        mods = row.addCheckbox(
                Component.translatable("message.bclib.syncfiles.mods"),
                hasMods,
                BUTTON_HEIGHT,
                this.font
        );
        mods.setEnabled(hasMods);

        row.addSpacer();
        row.addButton(Component.translatable("title.bclib.syncfiles.modInfo"), 20, font, (button) -> {
            ModListScreen scr = new ModListScreen(
                    this,
                    Component.translatable("title.bclib.syncfiles.modlist"),
                    Component.translatable("message.bclib.syncfiles.modlist"),
                    ModUtil.getMods(),
                    serverInfo
            );
            Minecraft.getInstance().setScreen(scr);
        });

        grid.addSpacerRow();


        final GridCheckboxCell configs;
        row = grid.addRow();
        configs = row.addCheckbox(
                Component.translatable("message.bclib.syncfiles.configs"),
                hasConfigFiles,
                BUTTON_HEIGHT,
                this.font
        );
        configs.setEnabled(hasConfigFiles);

        grid.addSpacerRow();

        row = grid.addRow();

        final GridCheckboxCell folder;
        folder = row.addCheckbox(
                Component.translatable("message.bclib.syncfiles.folders"),
                hasFiles,
                BUTTON_HEIGHT,
                this.font
        );
        folder.setEnabled(hasFiles);
        row.addSpacer();

        GridCheckboxCell delete;
        delete = row.addCheckbox(
                Component.translatable("message.bclib.syncfiles.delete"),
                shouldDelete,
                BUTTON_HEIGHT,
                this.font
        );
        delete.setEnabled(shouldDelete);


        grid.addSpacerRow(30);
        row = grid.addRow();
        row.addFiller();
        row.addButton(CommonComponents.GUI_NO, BUTTON_HEIGHT, this.font, (button) -> {
            listener.proceed(false, false, false, false);
        });
        row.addSpacer();
        row.addButton(CommonComponents.GUI_YES, BUTTON_HEIGHT, this.font, (button) -> {
            listener.proceed(
                    mods.isChecked(),
                    configs.isChecked(),
                    folder.isChecked(),
                    delete.isChecked()
            );
        });
        row.addFiller();
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Environment(EnvType.CLIENT)
    public interface Listener {
        void proceed(boolean downloadMods, boolean downloadConfigs, boolean downloadFiles, boolean removeFiles);
    }
}
