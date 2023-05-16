package org.betterx.bclib.client.gui.screens;

import de.ambertation.wunderlib.ui.layout.components.Checkbox;
import de.ambertation.wunderlib.ui.layout.components.HorizontalStack;
import de.ambertation.wunderlib.ui.layout.components.LayoutComponent;
import de.ambertation.wunderlib.ui.layout.components.VerticalStack;
import org.betterx.bclib.api.v2.dataexchange.handler.autosync.HelloClient;
import org.betterx.worlds.together.util.ModUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SyncFilesScreen extends BCLibLayoutScreen {
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


    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected LayoutComponent<?, ?> initContent() {
        VerticalStack grid = new VerticalStack(fill(), fill());
        final int BUTTON_HEIGHT = 20;

        grid.addMultilineText(fill(), fit(), this.description).centerHorizontal();

        grid.addSpacer(10);

        HorizontalStack row;

        final Checkbox mods;
        row = grid.addRow();
        mods = row.addCheckbox(
                fit(), fit(),
                Component.translatable("message.bclib.syncfiles.mods"),
                hasMods
        );
        mods.setEnabled(hasMods);

        row.addSpacer(4);
        row.addButton(
                fit(), fit(),
                Component.translatable("title.bclib.syncfiles.modInfo")
        ).onPress((button) -> {
            ModListScreen scr = new ModListScreen(
                    this,
                    Component.translatable("title.bclib.syncfiles.modlist"),
                    Component.translatable("message.bclib.syncfiles.modlist"),
                    ModUtil.getMods(),
                    serverInfo
            );
            Minecraft.getInstance().setScreen(scr);
        });

        grid.addSpacer(4);


        final Checkbox configs;
        row = grid.addRow();
        configs = row.addCheckbox(
                fit(), fit(),
                Component.translatable("message.bclib.syncfiles.configs"),
                hasConfigFiles
        );
        configs.setEnabled(hasConfigFiles);

        grid.addSpacer(4);

        row = grid.addRow();

        final Checkbox folder;
        folder = row.addCheckbox(
                fit(), fit(),
                Component.translatable("message.bclib.syncfiles.folders"),
                hasFiles
        );
        folder.setEnabled(hasFiles);
        row.addSpacer(4);

        Checkbox delete;
        delete = row.addCheckbox(
                fit(), fit(),
                Component.translatable("message.bclib.syncfiles.delete"),
                shouldDelete
        );
        delete.setEnabled(shouldDelete);


        grid.addSpacer(30);
        row = grid.addRow().centerHorizontal();
        row.addButton(fit(), fit(), CommonComponents.GUI_NO).onPress((button) -> {
            listener.proceed(false, false, false, false);
        });
        row.addSpacer(4);
        row.addButton(fit(), fit(), CommonComponents.GUI_YES).onPress((button) -> {
            listener.proceed(
                    mods.isChecked(),
                    configs.isChecked(),
                    folder.isChecked(),
                    delete.isChecked()
            );
        });

        return grid;
    }

    @Environment(EnvType.CLIENT)
    public interface Listener {
        void proceed(boolean downloadMods, boolean downloadConfigs, boolean downloadFiles, boolean removeFiles);
    }
}
