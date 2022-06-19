package org.betterx.bclib.client.gui.screens;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.generator.BCLBiomeSource;
import org.betterx.bclib.api.v2.levelgen.LevelGenUtil;
import org.betterx.bclib.client.gui.gridlayout.GridCheckboxCell;
import org.betterx.bclib.client.gui.gridlayout.GridLayout;
import org.betterx.bclib.interfaces.WorldGenSettingsComponentAccessor;
import org.betterx.bclib.presets.worldgen.BCLWorldPreset;
import org.betterx.bclib.presets.worldgen.BCLWorldPresetSettings;

import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class WorldSetupScreen extends BCLibScreen {
    private final WorldCreationContext context;
    private final CreateWorldScreen createWorldScreen;

    public WorldSetupScreen(@Nullable CreateWorldScreen parent, WorldCreationContext context) {
        super(parent, Component.translatable("title.screen.bclib.worldgen.main"), 10, true);
        this.context = context;
        this.createWorldScreen = parent;
    }


    private GridCheckboxCell bclibEnd;
    private GridCheckboxCell bclibNether;
    GridCheckboxCell endLegacy;
    GridCheckboxCell endCustomTerrain;
    GridCheckboxCell generateEndVoid;
    GridCheckboxCell netherLegacy;

    @Override
    protected void initLayout() {
        final int netherVersion;
        final int endVersion;
        final boolean customEndGen;
        final boolean haveEndVoid;
        if (createWorldScreen.worldGenSettingsComponent instanceof WorldGenSettingsComponentAccessor acc
                && acc.bcl_getPreset()
                      .isPresent() && acc.bcl_getPreset()
                                         .get()
                                         .value() instanceof BCLWorldPreset wp
                && wp.settings instanceof BCLWorldPresetSettings settings) {
            netherVersion = settings.netherVersion;
            endVersion = settings.endVersion;
            customEndGen = settings.useEndTerrainGenerator;
            haveEndVoid = settings.generateEndVoid;
        } else {
            throw new IllegalStateException("The WorldSetup Screen is only valid for BetterX Presets.");
        }

        final int BUTTON_HEIGHT = 20;
        grid.addSpacerRow(20);

        var row = grid.addRow();
        var colNether = row.addColumn(0.5, GridLayout.GridValueType.PERCENTAGE);
        var colEnd = row.addColumn(0.5, GridLayout.GridValueType.PERCENTAGE);

        row = colNether.addRow();
        row.addString(Component.translatable("title.bclib.the_nether"), GridLayout.Alignment.CENTER, this);
        colNether.addSpacerRow(15);

        var mainSettingsRow = colNether.addRow();
        mainSettingsRow.addSpacer(16);
        colNether.addSpacerRow(2);
        row = colNether.addRow();
        row.addSpacer(20);
        netherLegacy = row.addCheckbox(
                Component.translatable("title.screen.bclib.worldgen.legacy_square"),
                endVersion == BCLBiomeSource.BIOME_SOURCE_VERSION_SQUARE,
                1.0,
                GridLayout.GridValueType.PERCENTAGE,
                (state) -> {
                }
        );
        bclibNether = mainSettingsRow.addCheckbox(
                Component.translatable(
                        "title.screen.bclib.worldgen.custom_biome_source"),
                netherVersion != BCLBiomeSource.BIOME_SOURCE_VERSION_VANILLA,
                1.0,
                GridLayout.GridValueType.PERCENTAGE,
                (state) -> {
                    netherLegacy.setEnabled(state);
                }
        );


        row = colEnd.addRow(GridLayout.VerticalAlignment.CENTER);
        row.addString(Component.translatable("title.bclib.the_end"), GridLayout.Alignment.CENTER, this);
        colEnd.addSpacerRow(15);

        mainSettingsRow = colEnd.addRow();
        mainSettingsRow.addSpacer(16);
        colEnd.addSpacerRow(2);
        row = colEnd.addRow();
        row.addSpacer(20);
        endCustomTerrain = row.addCheckbox(
                Component.translatable("title.screen.bclib.worldgen.custom_end_terrain"),
                customEndGen,
                1.0,
                GridLayout.GridValueType.PERCENTAGE,
                (state) -> {
                }
        );

        row = colEnd.addRow();
        row.addSpacer(20);
        generateEndVoid = row.addCheckbox(
                Component.translatable("title.screen.bclib.worldgen.end_void"),
                haveEndVoid,
                1.0,
                GridLayout.GridValueType.PERCENTAGE,
                (state) -> {
                }
        );

        row = colEnd.addRow();
        row.addSpacer(20);
        endLegacy = row.addCheckbox(
                Component.translatable("title.screen.bclib.worldgen.legacy_square"),
                endVersion == BCLBiomeSource.BIOME_SOURCE_VERSION_SQUARE,
                1.0,
                GridLayout.GridValueType.PERCENTAGE,
                (state) -> {
                }
        );

        bclibEnd = mainSettingsRow.addCheckbox(
                Component.translatable(
                        "title.screen.bclib.worldgen.custom_biome_source"),
                endVersion != BCLBiomeSource.BIOME_SOURCE_VERSION_VANILLA,
                1.0,
                GridLayout.GridValueType.PERCENTAGE,
                (state) -> {
                    endLegacy.setEnabled(state);
                    endCustomTerrain.setEnabled(state);
                    generateEndVoid.setEnabled(state);
                }
        );


        grid.addSpacerRow(36);
        row = grid.addRow();
        row.addFiller();
        row.addButton(CommonComponents.GUI_DONE, BUTTON_HEIGHT, font, (button) -> {
            updateSettings();
            onClose();
        });
        grid.addSpacerRow(10);
    }

    private void updateSettings() {
        int endVersion = BCLBiomeSource.DEFAULT_BIOME_SOURCE_VERSION;
        if (bclibEnd.isChecked()) {
            if (endLegacy.isChecked()) endVersion = BCLBiomeSource.BIOME_SOURCE_VERSION_SQUARE;
            else endVersion = BCLBiomeSource.BIOME_SOURCE_VERSION_HEX;
        } else {
            endVersion = BCLBiomeSource.BIOME_SOURCE_VERSION_VANILLA;
        }

        int netherVersion = BCLBiomeSource.DEFAULT_BIOME_SOURCE_VERSION;
        if (bclibNether.isChecked()) {
            if (netherLegacy.isChecked()) netherVersion = BCLBiomeSource.BIOME_SOURCE_VERSION_SQUARE;
            else netherVersion = BCLBiomeSource.BIOME_SOURCE_VERSION_HEX;
        } else {
            netherVersion = BCLBiomeSource.BIOME_SOURCE_VERSION_VANILLA;
        }

        if (createWorldScreen.worldGenSettingsComponent instanceof WorldGenSettingsComponentAccessor acc
                && acc.bcl_getPreset()
                      .isPresent() && acc.bcl_getPreset()
                                         .get()
                                         .value() instanceof BCLWorldPreset worldPreset) {
            acc.bcl_setPreset(Optional.of(Holder.direct(worldPreset.withSettings(new BCLWorldPresetSettings(
                    netherVersion,
                    endVersion,
                    endCustomTerrain.isChecked(),
                    generateEndVoid.isChecked()
            )))));
        }

        BCLib.LOGGER.info("Custom World Versions: end=" + endVersion + ", nether=" + netherVersion);
        updateConfiguration(LevelStem.END, BuiltinDimensionTypes.END, endVersion);
        updateConfiguration(LevelStem.NETHER, BuiltinDimensionTypes.NETHER, netherVersion);


    }


    private void updateConfiguration(
            ResourceKey<LevelStem> dimensionKey,
            ResourceKey<DimensionType> dimensionTypeKey,
            int biomeSourceVersion
    ) {
        createWorldScreen.worldGenSettingsComponent.updateSettings(
                (registryAccess, worldGenSettings) -> LevelGenUtil.replaceGenerator(
                        dimensionKey,
                        dimensionTypeKey,
                        biomeSourceVersion,
                        registryAccess,
                        worldGenSettings
                )
        );
    }


}
