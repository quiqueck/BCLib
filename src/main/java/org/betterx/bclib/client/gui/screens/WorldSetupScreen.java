package org.betterx.bclib.client.gui.screens;

import de.ambertation.wunderlib.ui.layout.components.*;
import de.ambertation.wunderlib.ui.layout.components.render.RenderHelper;
import de.ambertation.wunderlib.ui.layout.values.Rectangle;
import de.ambertation.wunderlib.ui.layout.values.Size;
import de.ambertation.wunderlib.ui.vanilla.LayoutScreen;
import org.betterx.bclib.api.v2.generator.BCLibEndBiomeSource;
import org.betterx.bclib.api.v2.generator.BCLibNetherBiomeSource;
import org.betterx.bclib.api.v2.generator.config.BCLEndBiomeSourceConfig;
import org.betterx.bclib.api.v2.generator.config.BCLNetherBiomeSourceConfig;
import org.betterx.bclib.api.v2.levelgen.LevelGenUtil;
import org.betterx.bclib.registry.PresetsRegistry;
import org.betterx.worlds.together.worldPreset.TogetherWorldPreset;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Map;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class WorldSetupScreen extends LayoutScreen {
    private final WorldCreationContext context;
    private final CreateWorldScreen createWorldScreen;
    private Range<Integer> netherBiomeSize;
    private Range<Integer> netherVerticalBiomeSize;
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
    Checkbox netherVertical;
    Checkbox netherAmplified;

    public LayoutComponent<?, ?> netherPage(BCLNetherBiomeSourceConfig netherConfig) {
        VerticalStack content = new VerticalStack(fill(), fit()).centerHorizontal();
        content.addSpacer(8);

        bclibNether = content.addCheckbox(
                fit(), fit(),
                Component.translatable("title.screen.bclib.worldgen.custom_nether_biome_source"),
                netherConfig.mapVersion != BCLNetherBiomeSourceConfig.NetherBiomeMapType.VANILLA
        );

        netherLegacy = content.indent(20).addCheckbox(
                fit(), fit(),
                Component.translatable("title.screen.bclib.worldgen.legacy_square"),
                netherConfig.mapVersion == BCLNetherBiomeSourceConfig.NetherBiomeMapType.SQUARE
        );

        netherAmplified = content.indent(20).addCheckbox(
                fit(), fit(),
                Component.translatable("title.screen.bclib.worldgen.nether_amplified"),
                netherConfig.amplified
        );

        netherVertical = content.indent(20).addCheckbox(
                fit(), fit(),
                Component.translatable("title.screen.bclib.worldgen.nether_vertical"),
                netherConfig.useVerticalBiomes
        );

        content.addSpacer(12);
        content.addText(fit(), fit(), Component.translatable("title.screen.bclib.worldgen.avg_biome_size"))
               .centerHorizontal();
        content.addHorizontalSeparator(8).alignTop();

        netherBiomeSize = content.addRange(
                fixed(200),
                fit(),
                Component.translatable("title.screen.bclib.worldgen.nether_biome_size"),
                1,
                512,
                netherConfig.biomeSize / 16
        );

        netherVerticalBiomeSize = content.addRange(
                fixed(200),
                fit(),
                Component.translatable("title.screen.bclib.worldgen.nether_vertical_biome_size"),
                1,
                32,
                netherConfig.biomeSizeVertical / 16
        );

        bclibNether.onChange((cb, state) -> {
            netherLegacy.setEnabled(state);
            netherAmplified.setEnabled(state);
            netherVertical.setEnabled(state);
            netherBiomeSize.setEnabled(state);
            netherVerticalBiomeSize.setEnabled(state && netherVertical.isChecked());
        });

        netherVertical.onChange((cb, state) -> {
            netherVerticalBiomeSize.setEnabled(state && bclibNether.isChecked());
        });

        content.addSpacer(8);
        return content.setDebugName("Nether page");
    }

    public LayoutComponent<?, ?> endPage(BCLEndBiomeSourceConfig endConfig) {
        VerticalStack content = new VerticalStack(fill(), fit()).centerHorizontal();
        content.addSpacer(8);
        bclibEnd = content.addCheckbox(
                fit(), fit(),
                Component.translatable("title.screen.bclib.worldgen.custom_end_biome_source"),
                endConfig.mapVersion != BCLEndBiomeSourceConfig.EndBiomeMapType.VANILLA
        );

        endLegacy = content.indent(20).addCheckbox(
                fit(), fit(),
                Component.translatable("title.screen.bclib.worldgen.legacy_square"),
                endConfig.mapVersion == BCLEndBiomeSourceConfig.EndBiomeMapType.SQUARE
        );

        endCustomTerrain = content.indent(20).addCheckbox(
                fit(), fit(),
                Component.translatable("title.screen.bclib.worldgen.custom_end_terrain"),
                endConfig.generatorVersion != BCLEndBiomeSourceConfig.EndBiomeGeneratorType.VANILLA
        );

        generateEndVoid = content.indent(20).addCheckbox(
                fit(), fit(),
                Component.translatable("title.screen.bclib.worldgen.end_void"),
                endConfig.withVoidBiomes
        );

        content.addSpacer(12);
        content.addText(fit(), fit(), Component.translatable("title.screen.bclib.worldgen.avg_biome_size"))
               .centerHorizontal();
        content.addHorizontalSeparator(8).alignTop();

        landBiomeSize = content.addRange(
                fixed(200),
                fit(),
                Component.translatable("title.screen.bclib.worldgen.land_biome_size"),
                1,
                512,
                endConfig.landBiomesSize / 16
        );

        voidBiomeSize = content.addRange(
                fixed(200),
                fit(),
                Component.translatable("title.screen.bclib.worldgen.void_biome_size"),
                1,
                512,
                endConfig.voidBiomesSize / 16
        );

        centerBiomeSize = content.addRange(
                fixed(200),
                fit(),
                Component.translatable("title.screen.bclib.worldgen.center_biome_size"),
                1,
                512,
                endConfig.centerBiomesSize / 16
        );

        barrensBiomeSize = content.addRange(
                fixed(200),
                fit(),
                Component.translatable("title.screen.bclib.worldgen.barrens_biome_size"),
                1,
                512,
                endConfig.barrensBiomesSize / 16
        );

        content.addSpacer(12);
        content.addText(fit(), fit(), Component.translatable("title.screen.bclib.worldgen.other"))
               .centerHorizontal();
        content.addHorizontalSeparator(8).alignTop();

        innerRadius = content.addRange(
                fixed(200),
                fit(),
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
        Map<ResourceKey<LevelStem>, ChunkGenerator> betterxAmplifiedDimensions = TogetherWorldPreset.getDimensionsMap(
                PresetsRegistry.BCL_WORLD_AMPLIFIED);
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
                    centerBiomeSize.getValue() * 16,
                    voidBiomeSize.getValue() * 16,
                    landBiomeSize.getValue() * 16,
                    barrensBiomeSize.getValue() * 16
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
                    netherBiomeSize.getValue() * 16,
                    netherVerticalBiomeSize.getValue() * 16,
                    netherVertical.isChecked(),
                    netherAmplified.isChecked()
            );

            ChunkGenerator netherGenerator = (
                    netherAmplified.isChecked()
                            ? betterxAmplifiedDimensions
                            : betterxDimensions
            ).get(LevelStem.NETHER);
            ((BCLibNetherBiomeSource) netherGenerator.getBiomeSource()).setTogetherConfig(netherConfig);

            updateConfiguration(LevelStem.NETHER, BuiltinDimensionTypes.NETHER, netherGenerator);
        } else {
            ChunkGenerator endGenerator = vanillaDimensions.get(LevelStem.NETHER);
            updateConfiguration(LevelStem.NETHER, BuiltinDimensionTypes.NETHER, endGenerator);
        }

        final WorldCreationUiState acc = createWorldScreen.getUiState();
        final Holder<WorldPreset> configuredPreset = acc.getWorldType().preset();
        if (configuredPreset != null && configuredPreset.value() instanceof TogetherWorldPreset worldPreset) {
            ResourceKey<WorldPreset> key = configuredPreset.unwrapKey().orElse(null);
            if (key == null) key = worldPreset.parentKey;
            
            acc.setWorldType(new WorldCreationUiState.WorldTypeEntry(Holder.direct(
                    worldPreset.withDimensions(
                            createWorldScreen
                                    .getUiState()
                                    .getSettings()
                                    .selectedDimensions()
                                    .dimensions(),
                            key
                    )
            )));
        }
    }


    private void updateConfiguration(
            ResourceKey<LevelStem> dimensionKey,
            ResourceKey<DimensionType> dimensionTypeKey,
            ChunkGenerator chunkGenerator
    ) {
        createWorldScreen.getUiState().updateDimensions(
                (registryAccess, worldDimensions) -> new WorldDimensions(LevelGenUtil.replaceGenerator(
                        dimensionKey,
                        dimensionTypeKey,
                        registryAccess,
                        worldDimensions.dimensions(),
                        chunkGenerator
                ))
        );
    }

    @Override
    protected LayoutComponent<?, ?> createScreen(LayoutComponent<?, ?> content) {
        VerticalStack rows = new VerticalStack(fill(), fill()).setDebugName("title stack");

        if (topPadding > 0) rows.addSpacer(topPadding);
        rows.add(content);
        if (bottomPadding > 0) rows.addSpacer(bottomPadding);

        if (sidePadding <= 0) return rows;

        HorizontalStack cols = new HorizontalStack(fill(), fill()).setDebugName("padded side");
        cols.addSpacer(sidePadding);
        cols.add(rows);
        cols.addSpacer(sidePadding);
        return cols;
    }

    Button netherButton, endButton;
    VerticalScroll<?> scroller;
    HorizontalStack title;

    @Override
    protected LayoutComponent<?, ?> initContent() {
        BCLEndBiomeSourceConfig endConfig = BCLEndBiomeSourceConfig.VANILLA;
        BCLNetherBiomeSourceConfig netherConfig = BCLNetherBiomeSourceConfig.VANILLA;

        final WorldCreationUiState acc = createWorldScreen.getUiState();
        final Holder<WorldPreset> configuredPreset = acc.getWorldType().preset();
        if (configuredPreset.value() instanceof TogetherWorldPreset wp) {
            LevelStem endStem = wp.getDimension(LevelStem.END);
            if (endStem != null && endStem.generator().getBiomeSource() instanceof BCLibEndBiomeSource bs) {
                endConfig = bs.getTogetherConfig();
            }
            LevelStem netherStem = wp.getDimension(LevelStem.NETHER);
            if (netherStem != null && netherStem.generator().getBiomeSource() instanceof BCLibNetherBiomeSource bs) {
                netherConfig = bs.getTogetherConfig();
            }
        }

        LayoutComponent<?, ? extends LayoutComponent<?, ?>> netherPage = netherPage(netherConfig);
        LayoutComponent<?, ? extends LayoutComponent<?, ?>> endPage = endPage(endConfig);

        Tabs main = new Tabs(fill(), fill()).setPadding(8, 0, 0, 0);
        main.addPage(Component.translatable("title.bclib.the_nether"), VerticalScroll.create(netherPage));
        main.addSpacer(8);
        main.addPage(Component.translatable("title.bclib.the_end"), scroller = VerticalScroll.create(endPage));
        netherButton = main.getButton(0);
        endButton = main.getButton(1);

        title = new HorizontalStack(fit(), fit()).setDebugName("title bar").alignBottom();
        title.addImage(fixed(22), fixed(22), BCLibLayoutScreen.BCLIB_LOGO_WHITE_LOCATION, Size.of(256))
             .setDebugName("icon");
        title.addSpacer(4);
        VerticalStack logos = title.addColumn(fit(), fit());
        logos.addImage(fixed(178 / 3), fixed(40 / 3), WelcomeScreen.BETTERX_LOCATION, Size.of(178, 40));
        logos.add(super.createTitle());
        logos.addSpacer(2);

        main.addFiller();
        main.addComponent(title);


        VerticalStack rows = new VerticalStack(fill(), fill());
        rows.add(main);
        rows.addSpacer(4);
        rows.addButton(fit(), fit(), CommonComponents.GUI_DONE).onPress((bt) -> {
            updateSettings();
            onClose();
        }).alignRight();

        main.onPageChange((tabs, idx) -> {
            targetT = 1 - idx;
        });

        return rows;
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.fill(0, 0, width, height, 0xBD343444);
    }

    record IconState(int left, int top, int size) {
        //easing curves from https://easings.net/de
        static double easeInOutQuint(double t) {
            return t < 0.5 ? 16 * t * t * t * t * t : 1 - Math.pow(-2 * t + 2, 5) / 2;
        }

        static double easeOutBounce(double x) {
            final double n1 = 7.5625;
            final double d1 = 2.75;

            if (x < 1 / d1) {
                return n1 * x * x;
            } else if (x < 2 / d1) {
                return n1 * (x -= 1.5 / d1) * x + 0.75;
            } else if (x < 2.5 / d1) {
                return n1 * (x -= 2.25 / d1) * x + 0.9375;
            } else {
                return n1 * (x -= 2.625 / d1) * x + 0.984375;
            }
        }

        static int lerp(double t, int x0, int x1) {
            return (int) ((1 - t) * x0 + t * x1);
        }
    }

    IconState netherOff, netherOn, endOff, endOn;
    double iconT = 0.5;
    double targetT = 1;

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        final double SPEED = 0.05;
        if (targetT < iconT && iconT > 0) iconT = Math.max(0, iconT - f * SPEED);
        else if (targetT > iconT && iconT < 1) iconT = Math.min(1, iconT + f * SPEED);

        final double t;
        if (iconT > 0 && iconT < 1) {
            if (targetT > iconT) {
                t = IconState.easeOutBounce(iconT);
            } else {
                t = 1 - IconState.easeOutBounce(1 - iconT);
            }
        } else t = iconT;

        if (endButton != null) {
            if (endOff == null) {
                endOff = new IconState(
                        endButton.getScreenBounds().right() - 12,
                        endButton.getScreenBounds().top - 7,
                        16
                );
                endOn = new IconState(
                        (title.getScreenBounds().left - endButton.getScreenBounds().right()) / 2
                                + endButton.getScreenBounds().right()
                                - 14,
                        scroller.getScreenBounds().top - 16,
                        32
                );
            }
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(
                    IconState.lerp(t, endOn.left, endOff.left),
                    IconState.lerp(t, endOn.top, endOff.top),
                    0
            );
            int size = IconState.lerp(t, endOn.size, endOff.size);
            RenderHelper.renderImage(
                    guiGraphics, 0, 0,
                    size,
                    size,
                    WelcomeScreen.ICON_BETTEREND,
                    Size.of(32), new Rectangle(0, 0, 32, 32),
                    (float) 1
            );
            guiGraphics.pose().popPose();
        }

        if (netherButton != null) {
            if (netherOff == null) {
                netherOff = new IconState(
                        netherButton.getScreenBounds().right() - 12,
                        netherButton.getScreenBounds().top - 7,
                        16
                );
                netherOn = endOn;
            }
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(
                    IconState.lerp(t, netherOff.left, netherOn.left),
                    IconState.lerp(t, netherOff.top, netherOn.top),
                    0
            );
            int size = IconState.lerp(t, netherOff.size, netherOn.size);
            RenderHelper.renderImage(
                    guiGraphics, 0, 0,
                    size,
                    size,
                    WelcomeScreen.ICON_BETTERNETHER,
                    Size.of(32), new Rectangle(0, 0, 32, 32),
                    (float) 1
            );
            guiGraphics.pose().popPose();
        }
    }
}
