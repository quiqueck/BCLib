package org.betterx.bclib.client.gui.screens;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.generator.BCLibEndBiomeSource;
import org.betterx.bclib.api.v2.generator.BCLibNetherBiomeSource;
import org.betterx.bclib.api.v2.generator.config.BCLEndBiomeSourceConfig;
import org.betterx.bclib.api.v2.generator.config.BCLNetherBiomeSourceConfig;
import org.betterx.bclib.api.v2.levelgen.LevelGenUtil;
import org.betterx.bclib.registry.PresetsRegistry;
import org.betterx.ui.layout.components.*;
import org.betterx.ui.layout.values.Size;
import org.betterx.ui.layout.values.Value;
import org.betterx.ui.vanilla.LayoutScreen;
import org.betterx.worlds.together.worldPreset.TogetherWorldPreset;
import org.betterx.worlds.together.worldPreset.WorldGenSettingsComponentAccessor;

import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class WorldSetupScreen extends LayoutScreen {
    static final ResourceLocation BCLIB_LOGO_LOCATION = new ResourceLocation(BCLib.MOD_ID, "icon.png");

    private final WorldCreationContext context;
    private final CreateWorldScreen createWorldScreen;
    private Range<Integer> landBiomeSize;
    private Range<Integer> voidBiomeSize;
    private Range<Integer> centerBiomeSize;
    private Range<Integer> barrensBiomeSize;
    private Range<Integer> innerRadius;

    public WorldSetupScreen(@Nullable CreateWorldScreen parent, WorldCreationContext context) {
        super(parent, Component.translatable("title.screen.bclib.worldgen.main"), 10, 10, 10);
        this.context = context;
        this.createWorldScreen = parent;
    }


    private Checkbox bclibEnd;
    private Checkbox bclibNether;
    Checkbox endLegacy;
    Checkbox endCustomTerrain;
    Checkbox generateEndVoid;
    Checkbox netherLegacy;

    public LayoutComponent<?, ?> netherPage(BCLNetherBiomeSourceConfig netherConfig) {
        VerticalStack content = new VerticalStack(Value.fill(), Value.fit()).centerHorizontal();
        content.addSpacer(8);

        bclibNether = content.addCheckbox(
                Value.fit(), Value.fit(),
                Component.translatable("title.screen.bclib.worldgen.custom_biome_source"),
                netherConfig.mapVersion != BCLNetherBiomeSourceConfig.NetherBiomeMapType.VANILLA
        );

        netherLegacy = content.indent(20).addCheckbox(
                Value.fit(), Value.fit(),
                Component.translatable("title.screen.bclib.worldgen.legacy_square"),
                netherConfig.mapVersion == BCLNetherBiomeSourceConfig.NetherBiomeMapType.SQUARE
        );


        bclibNether.onChange((cb, state) -> {
            netherLegacy.setEnabled(state);
        });

        content.addSpacer(8);
        return content.setDebugName("Nether page");
    }

    public LayoutComponent<?, ?> endPage(BCLEndBiomeSourceConfig endConfig) {
        VerticalStack content = new VerticalStack(Value.fill(), Value.fit()).centerHorizontal();
        content.addSpacer(8);
        bclibEnd = content.addCheckbox(
                Value.fit(), Value.fit(),
                Component.translatable("title.screen.bclib.worldgen.custom_biome_source"),
                endConfig.mapVersion != BCLEndBiomeSourceConfig.EndBiomeMapType.VANILLA
        );

        endLegacy = content.indent(20).addCheckbox(
                Value.fit(), Value.fit(),
                Component.translatable("title.screen.bclib.worldgen.legacy_square"),
                endConfig.mapVersion == BCLEndBiomeSourceConfig.EndBiomeMapType.SQUARE
        );

        endCustomTerrain = content.indent(20).addCheckbox(
                Value.fit(), Value.fit(),
                Component.translatable("title.screen.bclib.worldgen.custom_end_terrain"),
                endConfig.generatorVersion != BCLEndBiomeSourceConfig.EndBiomeGeneratorType.VANILLA
        );

        generateEndVoid = content.indent(20).addCheckbox(
                Value.fit(), Value.fit(),
                Component.translatable("title.screen.bclib.worldgen.end_void"),
                endConfig.withVoidBiomes
        );

        content.addSpacer(12);
        content.addText(Value.fit(), Value.fit(), Component.translatable("title.screen.bclib.worldgen.avg_biome_size"))
               .centerHorizontal();
        content.addHorizontalSeparator(8).alignTop();

        landBiomeSize = content.addRange(
                Value.fixed(200),
                Value.fit(),
                Component.translatable("title.screen.bclib.worldgen.land_biome_size"),
                1,
                512,
                endConfig.landBiomesSize / 16
        );

        voidBiomeSize = content.addRange(
                Value.fixed(200),
                Value.fit(),
                Component.translatable("title.screen.bclib.worldgen.void_biome_size"),
                1,
                512,
                endConfig.voidBiomesSize / 16
        );

        centerBiomeSize = content.addRange(
                Value.fixed(200),
                Value.fit(),
                Component.translatable("title.screen.bclib.worldgen.center_biome_size"),
                1,
                512,
                endConfig.centerBiomesSize / 16
        );

        barrensBiomeSize = content.addRange(
                Value.fixed(200),
                Value.fit(),
                Component.translatable("title.screen.bclib.worldgen.barrens_biome_size"),
                1,
                512,
                endConfig.barrensBiomesSize / 16
        );

        content.addSpacer(12);
        content.addText(Value.fit(), Value.fit(), Component.translatable("title.screen.bclib.worldgen.other"))
               .centerHorizontal();
        content.addHorizontalSeparator(8).alignTop();

        innerRadius = content.addRange(
                Value.fixed(200),
                Value.fit(),
                Component.translatable("title.screen.bclib.worldgen.central_radius"),
                1,
                512,
                (int) Math.sqrt(endConfig.innerVoidRadiusSquared) / 16
        );


        bclibEnd.onChange((cb, state) -> {
            endLegacy.setEnabled(state);
            endCustomTerrain.setEnabled(state);
            generateEndVoid.setEnabled(state);

            landBiomeSize.setEnabled(state && endCustomTerrain.isChecked());
            voidBiomeSize.setEnabled(state && endCustomTerrain.isChecked() && generateEndVoid.isChecked());
            centerBiomeSize.setEnabled(state && endCustomTerrain.isChecked());
            barrensBiomeSize.setEnabled(state && endCustomTerrain.isChecked());
        });

        endCustomTerrain.onChange((cb, state) -> {
            landBiomeSize.setEnabled(state);
            voidBiomeSize.setEnabled(state && generateEndVoid.isChecked());
            centerBiomeSize.setEnabled(state);
            barrensBiomeSize.setEnabled(state);
        });

        generateEndVoid.onChange((cb, state) -> {
            voidBiomeSize.setEnabled(state && endCustomTerrain.isChecked());
        });

        content.addSpacer(8);
        return content.setDebugName("End Page");
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
                    (int) Math.pow(innerRadius.getValue() * 16, 2),
                    centerBiomeSize.getValue(),
                    voidBiomeSize.getValue(),
                    landBiomeSize.getValue(),
                    barrensBiomeSize.getValue()
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

    @Override
    protected LayoutComponent<?, ?> addTitle(LayoutComponent<?, ?> content) {
        VerticalStack rows = new VerticalStack(Value.fill(), Value.fill()).setDebugName("title stack");

        if (topPadding > 0) rows.addSpacer(topPadding);
        rows.add(content);
        if (bottomPadding > 0) rows.addSpacer(bottomPadding);

        if (sidePadding <= 0) return rows;

        HorizontalStack cols = new HorizontalStack(Value.fill(), Value.fill()).setDebugName("padded side");
        cols.addSpacer(sidePadding);
        cols.add(rows);
        cols.addSpacer(sidePadding);
        return cols;
    }

    @Override
    protected LayoutComponent<?, ?> initContent() {
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

        var netherPage = netherPage(netherConfig);
        var endPage = endPage(endConfig);

        Tabs main = new Tabs(Value.fill(), Value.fill()).setPadding(8, 0, 0, 0);
        main.addPage(Component.translatable("title.bclib.the_nether"), VerticalScroll.create(netherPage));
        main.addPage(Component.translatable("title.bclib.the_end"), VerticalScroll.create(endPage));

        HorizontalStack title = new HorizontalStack(Value.fit(), Value.fit()).setDebugName("title bar");
        title.addIcon(BCLIB_LOGO_LOCATION, Size.of(512)).setDebugName("icon");
        title.addSpacer(4);
        title.add(super.buildTitle());

        main.addFiller();
        main.addComponent(title);


        VerticalStack rows = new VerticalStack(Value.fill(), Value.fill());
        rows.add(main);
        rows.addSpacer(4);
        rows.addButton(Value.fit(), Value.fit(), CommonComponents.GUI_DONE).onPress((bt) -> {
            updateSettings();
            onClose();
        }).alignRight();

        return rows;
    }
}
