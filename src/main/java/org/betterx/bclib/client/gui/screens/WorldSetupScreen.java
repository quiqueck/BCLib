package org.betterx.bclib.client.gui.screens;

import org.betterx.bclib.api.v2.generator.BCLibEndBiomeSource;
import org.betterx.bclib.api.v2.generator.BCLibNetherBiomeSource;
import org.betterx.bclib.api.v2.generator.config.BCLEndBiomeSourceConfig;
import org.betterx.bclib.api.v2.generator.config.BCLNetherBiomeSourceConfig;
import org.betterx.bclib.api.v2.levelgen.LevelGenUtil;
import org.betterx.bclib.client.gui.gridlayout.GridCheckboxCell;
import org.betterx.bclib.client.gui.gridlayout.GridLayout;
import org.betterx.bclib.registry.PresetsRegistry;
import org.betterx.worlds.together.worldPreset.TogetherWorldPreset;
import org.betterx.worlds.together.worldPreset.WorldGenSettingsComponentAccessor;

import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Map;
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
        BCLEndBiomeSourceConfig endConfig = BCLEndBiomeSourceConfig.VANILLA;
        BCLNetherBiomeSourceConfig netherConfig = BCLNetherBiomeSourceConfig.VANILLA;
        if (createWorldScreen.worldGenSettingsComponent instanceof WorldGenSettingsComponentAccessor acc
                && acc.bcl_getPreset()
                      .isPresent() && acc.bcl_getPreset()
                                         .get()
                                         .value() instanceof TogetherWorldPreset wp) {

            LevelStem endStem = wp.getDimension(LevelStem.END);
            if (endStem != null && endStem.generator().getBiomeSource() instanceof BCLibEndBiomeSource bs) {
                endConfig = bs.getTogetherConfig();
            }
            LevelStem netherStem = wp.getDimension(LevelStem.NETHER);
            if (netherStem != null && netherStem.generator().getBiomeSource() instanceof BCLibNetherBiomeSource bs) {
                netherConfig = bs.getTogetherConfig();
            }
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
                netherConfig.mapVersion == BCLNetherBiomeSourceConfig.NetherBiomeMapType.SQUARE,
                1.0,
                GridLayout.GridValueType.PERCENTAGE,
                (state) -> {
                }
        );
        bclibNether = mainSettingsRow.addCheckbox(
                Component.translatable(
                        "title.screen.bclib.worldgen.custom_biome_source"),
                netherConfig.mapVersion != BCLNetherBiomeSourceConfig.NetherBiomeMapType.VANILLA,
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
                endConfig.generatorVersion != BCLEndBiomeSourceConfig.EndBiomeGeneratorType.VANILLA,
                1.0,
                GridLayout.GridValueType.PERCENTAGE,
                (state) -> {
                }
        );

        row = colEnd.addRow();
        row.addSpacer(20);
        generateEndVoid = row.addCheckbox(
                Component.translatable("title.screen.bclib.worldgen.end_void"),
                endConfig.withVoidBiomes,
                1.0,
                GridLayout.GridValueType.PERCENTAGE,
                (state) -> {
                }
        );

        row = colEnd.addRow();
        row.addSpacer(20);
        endLegacy = row.addCheckbox(
                Component.translatable("title.screen.bclib.worldgen.legacy_square"),
                endConfig.mapVersion == BCLEndBiomeSourceConfig.EndBiomeMapType.SQUARE,
                1.0,
                GridLayout.GridValueType.PERCENTAGE,
                (state) -> {
                }
        );

        bclibEnd = mainSettingsRow.addCheckbox(
                Component.translatable(
                        "title.screen.bclib.worldgen.custom_biome_source"),
                endConfig.mapVersion != BCLEndBiomeSourceConfig.EndBiomeMapType.VANILLA,
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
        Map<ResourceKey<LevelStem>, ChunkGenerator> betterxDimensions = TogetherWorldPreset.getDimensionsMap(
                PresetsRegistry.BCL_WORLD);
        Map<ResourceKey<LevelStem>, ChunkGenerator> vanillaDimensions = TogetherWorldPreset.getDimensionsMap(
                WorldPresets.NORMAL);
        BCLEndBiomeSourceConfig.EndBiomeMapType endVersion = BCLEndBiomeSourceConfig.DEFAULT.mapVersion;


        if (bclibEnd.isChecked()) {
            BCLEndBiomeSourceConfig endConfig = new BCLEndBiomeSourceConfig(
                    endLegacy.isChecked()
                            ? BCLEndBiomeSourceConfig.EndBiomeMapType.SQUARE
                            : BCLEndBiomeSourceConfig.EndBiomeMapType.HEX,
                    endCustomTerrain.isChecked()
                            ? BCLEndBiomeSourceConfig.EndBiomeGeneratorType.PAULEVS
                            : BCLEndBiomeSourceConfig.EndBiomeGeneratorType.VANILLA,
                    generateEndVoid.isChecked(),
                    BCLEndBiomeSourceConfig.DEFAULT.innerVoidRadiusSquared,
                    BCLEndBiomeSourceConfig.DEFAULT.centerBiomesSize,
                    BCLEndBiomeSourceConfig.DEFAULT.voidBiomesSize,
                    BCLEndBiomeSourceConfig.DEFAULT.landBiomesSize,
                    BCLEndBiomeSourceConfig.DEFAULT.barrensBiomesSize
            );

            ChunkGenerator endGenerator = betterxDimensions.get(LevelStem.END);
            ((BCLibEndBiomeSource) endGenerator.getBiomeSource()).setTogetherConfig(endConfig);

            updateConfiguration(LevelStem.END, BuiltinDimensionTypes.END, endGenerator);
        } else {
            ChunkGenerator endGenerator = vanillaDimensions.get(LevelStem.END);
            updateConfiguration(LevelStem.END, BuiltinDimensionTypes.END, endGenerator);
        }

        if (bclibNether.isChecked()) {
            BCLNetherBiomeSourceConfig netherConfig = new BCLNetherBiomeSourceConfig(
                    netherLegacy.isChecked()
                            ? BCLNetherBiomeSourceConfig.NetherBiomeMapType.SQUARE
                            : BCLNetherBiomeSourceConfig.NetherBiomeMapType.HEX,
                    BCLNetherBiomeSourceConfig.DEFAULT.biomeSize,
                    BCLNetherBiomeSourceConfig.DEFAULT.biomeSizeVertical,
                    BCLNetherBiomeSourceConfig.DEFAULT.useVerticalBiomes
            );

            ChunkGenerator netherGenerator = betterxDimensions.get(LevelStem.NETHER);
            ((BCLibNetherBiomeSource) netherGenerator.getBiomeSource()).setTogetherConfig(netherConfig);

            updateConfiguration(LevelStem.NETHER, BuiltinDimensionTypes.NETHER, netherGenerator);
        } else {
            ChunkGenerator endGenerator = vanillaDimensions.get(LevelStem.NETHER);
            updateConfiguration(LevelStem.NETHER, BuiltinDimensionTypes.NETHER, endGenerator);
        }

        if (createWorldScreen.worldGenSettingsComponent instanceof WorldGenSettingsComponentAccessor acc
                && acc.bcl_getPreset()
                      .isPresent() && acc.bcl_getPreset()
                                         .get()
                                         .value() instanceof TogetherWorldPreset worldPreset) {
            acc.bcl_setPreset(Optional.of(Holder.direct(
                    worldPreset.withDimensions(
                            createWorldScreen
                                    .worldGenSettingsComponent
                                    .settings()
                                    .worldGenSettings()
                                    .dimensions()
                    )
            )));
        }
    }


    private void updateConfiguration(
            ResourceKey<LevelStem> dimensionKey,
            ResourceKey<DimensionType> dimensionTypeKey,
            ChunkGenerator chunkGenerator
    ) {
        createWorldScreen.worldGenSettingsComponent.updateSettings(
                (registryAccess, worldGenSettings) -> LevelGenUtil.replaceGenerator(
                        dimensionKey,
                        dimensionTypeKey,
                        registryAccess,
                        worldGenSettings,
                        chunkGenerator
                )
        );
    }


}
