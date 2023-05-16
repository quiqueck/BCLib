package org.betterx.bclib.client.gui.modmenu;

import de.ambertation.wunderlib.ui.layout.components.*;
import de.ambertation.wunderlib.ui.vanilla.LayoutScreenWithIcon;
import org.betterx.bclib.BCLib;
import org.betterx.bclib.config.ConfigKeeper;
import org.betterx.bclib.config.Configs;
import org.betterx.bclib.config.NamedPathConfig;
import org.betterx.bclib.config.NamedPathConfig.ConfigTokenDescription;
import org.betterx.bclib.config.NamedPathConfig.DependendConfigToken;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public class MainScreen extends LayoutScreenWithIcon {
    static final ResourceLocation BCLIB_LOGO_LOCATION = new ResourceLocation(BCLib.MOD_ID, "icon.png");

    public MainScreen(@Nullable Screen parent) {
        super(parent, BCLIB_LOGO_LOCATION, Component.translatable("title.bclib.modmenu.main"), 10, 10, 20);
    }

    protected <T> Component getComponent(NamedPathConfig config, ConfigTokenDescription<T> option, String type) {
        return Component.translatable(type + ".config." + config.configID + option.getPath());
    }

    Map<Checkbox, Supplier<Boolean>> dependentWidgets = new HashMap<>();

    protected void updateEnabledState() {
        dependentWidgets.forEach((cb, supl) -> cb.setEnabled(supl.get()));
    }

    @SuppressWarnings("unchecked")
    protected <T> void addRow(VerticalStack grid, NamedPathConfig config, ConfigTokenDescription<T> option) {
        if (ConfigKeeper.BooleanEntry.class.isAssignableFrom(option.token.type)) {
            addCheckbox(grid, config, (ConfigTokenDescription<Boolean>) option);
        } else if (ConfigKeeper.FloatEntry.class.isAssignableFrom(option.token.type)) {
            addFloat(grid, config, (ConfigTokenDescription<Float>) option);
        }

        grid.addSpacer(2);
    }

    protected void addFloat(VerticalStack grid, NamedPathConfig config, ConfigTokenDescription<Float> option) {
        if (option.topPadding > 0) {
            grid.addSpacer(option.topPadding);
        }
        HorizontalStack row = grid.addRow();
        if (option.leftPadding > 0) {
            row.addSpacer(option.leftPadding);
        }
        Range<Float> cb = row.addRange(
                fixed(200), fit(),
                getComponent(config, option, "title"),
                option.minRange,
                option.maxRange,
                config.getRaw(option.token)
        ).onChange(
                (caller, state) -> {
                    config.set(option.token, state);
                }
        );
    }

    protected void addCheckbox(VerticalStack grid, NamedPathConfig config, ConfigTokenDescription<Boolean> option) {
        if (option.topPadding > 0) {
            grid.addSpacer(option.topPadding);
        }
        HorizontalStack row = grid.addRow();
        if (option.leftPadding > 0) {
            row.addSpacer(option.leftPadding);
        }
        Checkbox cb = row.addCheckbox(
                fit(), fit(),
                getComponent(config, option, "title"),
                config.getRaw(option.token)
        ).onChange(
                (caller, state) -> {
                    config.set(option.token, state);
                    updateEnabledState();
                }
        );

        if (option.token instanceof DependendConfigToken) {
            dependentWidgets.put(cb, () -> option.token.dependenciesTrue(config));
            cb.setEnabled(option.token.dependenciesTrue(config));
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }


    @Override
    protected LayoutComponent initContent() {

        VerticalStack content = new VerticalStack(fit(), fit()).setDebugName("content");

        Configs.GENERATOR_CONFIG.getAllOptions()
                                .stream()
                                .filter(o -> !o.hidden)
                                .forEach(o -> addRow(content, Configs.GENERATOR_CONFIG, o));
        content.addSpacer(12);
        Configs.MAIN_CONFIG.getAllOptions()
                           .stream()
                           .filter(o -> !o.hidden)
                           .forEach(o -> addRow(content, Configs.MAIN_CONFIG, o));
        content.addSpacer(12);
        Configs.CLIENT_CONFIG.getAllOptions()
                             .stream()
                             .filter(o -> !o.hidden)
                             .forEach(o -> addRow(content, Configs.CLIENT_CONFIG, o));


        VerticalStack grid = new VerticalStack(fill(), fill()).setDebugName("main grid");
        grid.addScrollable(content);
        grid.addSpacer(8);
        grid.addButton(fit(), fit(), CommonComponents.GUI_DONE).onPress((button) -> {
            Configs.CLIENT_CONFIG.saveChanges();
            Configs.GENERATOR_CONFIG.saveChanges();
            Configs.MAIN_CONFIG.saveChanges();
            onClose();
        }).alignRight();
        return grid;
    }
}
