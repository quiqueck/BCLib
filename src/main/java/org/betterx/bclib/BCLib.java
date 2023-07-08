package org.betterx.bclib;

import org.betterx.bclib.api.v2.dataexchange.DataExchangeAPI;
import org.betterx.bclib.api.v2.dataexchange.handler.autosync.*;
import org.betterx.bclib.api.v2.generator.BCLibEndBiomeSource;
import org.betterx.bclib.api.v2.generator.BCLibNetherBiomeSource;
import org.betterx.bclib.api.v2.generator.GeneratorOptions;
import org.betterx.bclib.api.v2.levelgen.LevelGenEvents;
import org.betterx.bclib.api.v2.levelgen.biomes.BCLBiomeRegistry;
import org.betterx.bclib.api.v2.levelgen.structures.BCLStructurePoolElementTypes;
import org.betterx.bclib.api.v2.levelgen.structures.TemplatePiece;
import org.betterx.bclib.api.v2.levelgen.surface.rules.Conditions;
import org.betterx.bclib.api.v2.poi.PoiManager;
import org.betterx.bclib.api.v3.levelgen.features.blockpredicates.BlockPredicates;
import org.betterx.bclib.api.v3.levelgen.features.placement.PlacementModifiers;
import org.betterx.bclib.api.v3.tag.BCLBlockTags;
import org.betterx.bclib.blocks.signs.BaseHangingSignBlock;
import org.betterx.bclib.blocks.signs.BaseSignBlock;
import org.betterx.bclib.commands.CommandRegistry;
import org.betterx.bclib.commands.arguments.BCLibArguments;
import org.betterx.bclib.complexmaterials.BCLWoodTypeWrapper;
import org.betterx.bclib.config.Configs;
import org.betterx.bclib.config.PathConfig;
import org.betterx.bclib.networking.VersionChecker;
import org.betterx.bclib.recipes.AlloyingRecipe;
import org.betterx.bclib.recipes.AnvilRecipe;
import org.betterx.bclib.recipes.CraftingRecipes;
import org.betterx.bclib.registry.BaseBlockEntities;
import org.betterx.bclib.registry.BaseRegistry;
import org.betterx.bclib.registry.BlockRegistry;
import org.betterx.bclib.registry.PresetsRegistry;
import org.betterx.datagen.bclib.tests.TestStructure;
import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.util.Logger;
import org.betterx.worlds.together.world.WorldConfig;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.MapColor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.loader.api.FabricLoader;

import java.util.List;

public class BCLib implements ModInitializer {
    public static final String MOD_ID = "bclib";
    public static final Logger LOGGER = new Logger(MOD_ID);

    public static final boolean RUNS_NULLSCAPE = FabricLoader.getInstance()
                                                             .getModContainer("nullscape")
                                                             .isPresent();
    public static final boolean ADD_TEST_DATA = false;

    private void onDatagen() {

    }


    @Override
    public void onInitialize() {
        WorldsTogether.onInitialize();
        BCLibArguments.register();
        PresetsRegistry.register();
        LevelGenEvents.register();
        BlockPredicates.ensureStaticInitialization();
        BCLBiomeRegistry.register();
        BaseRegistry.register();
        GeneratorOptions.init();
        BaseBlockEntities.register();
        BCLibEndBiomeSource.register();
        BCLibNetherBiomeSource.register();
        CraftingRecipes.init();
        BCLStructurePoolElementTypes.ensureStaticallyLoaded();
        WorldConfig.registerModCache(MOD_ID);
        DataExchangeAPI.registerMod(MOD_ID);
        AnvilRecipe.register();
        AlloyingRecipe.register();
        Conditions.registerAll();
        CommandRegistry.register();
        BCLBlockTags.ensureStaticallyLoaded();
        PoiManager.registerAll();
        if (isDevEnvironment()) {
            TestStructure.registerBase();
        }

        if (ADD_TEST_DATA) {
            testObjects();
        }

        DataExchangeAPI.registerDescriptors(List.of(
                        HelloClient.DESCRIPTOR,
                        HelloServer.DESCRIPTOR,
                        RequestFiles.DESCRIPTOR,
                        SendFiles.DESCRIPTOR,
                        Chunker.DESCRIPTOR
                )
        );

        BCLibPatch.register();
        TemplatePiece.ensureStaticInitialization();
        PlacementModifiers.ensureStaticInitialization();
        Configs.save();

        WorldsTogether.FORCE_SERVER_TO_BETTERX_PRESET = Configs.SERVER_CONFIG.forceBetterXPreset();
        VersionChecker.registerMod(MOD_ID);

        if (isDatagen()) {
            onDatagen();
        }
    }

    public static boolean isDevEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static boolean isDatagen() {
        return System.getProperty("fabric-api.datagen") != null;
    }

    public static boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public static ResourceLocation makeID(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static BCLWoodTypeWrapper TEST_WOOD;
    public static BaseSignBlock TEST_SIGN = null;
    public static BaseHangingSignBlock TEST_HANGING_SIGN = null;

    private static void testObjects() {
        var bockReg = new BlockRegistry(new PathConfig(MOD_ID, "test"));
        bockReg.register(
                makeID("test_sign"),
                TEST_SIGN
        );
        bockReg.registerBlockOnly(
                makeID("test_wall_sign"),
                TEST_SIGN.getWallSignBlock()
        );
        bockReg.register(
                makeID("test_hanging_sign"),
                TEST_HANGING_SIGN
        );
        bockReg.registerBlockOnly(
                makeID("test_wall_hanging_sign"),
                TEST_HANGING_SIGN.getWallSignBlock()
        );
    }

    static {
        if (ADD_TEST_DATA) {
            TEST_WOOD = BCLWoodTypeWrapper.create(makeID("test_wood")).setColor(MapColor.COLOR_MAGENTA).build();
            TEST_SIGN = new BaseSignBlock.Wood(TEST_WOOD);
            TEST_HANGING_SIGN = new BaseHangingSignBlock.Wood(TEST_WOOD);


            final ResourceKey<CreativeModeTab> TAB_TEST_KEY = ResourceKey.create(
                    Registries.CREATIVE_MODE_TAB,
                    makeID("test_tab")
            );

            CreativeModeTab.Builder builder = FabricItemGroup
                    .builder();
            builder.title(Component.translatable("itemGroup.bclib.test"));
            builder.icon(() -> new ItemStack(Items.BARRIER));
            builder.displayItems((itemDisplayParameters, output) -> {

                var list = List.of(TEST_SIGN.asItem(), TEST_HANGING_SIGN.asItem())
                               .stream().map(b -> new ItemStack(b, 1)).toList();

                output.acceptAll(list);
            });
            final CreativeModeTab TAB_TEST = builder.build();
            ;

            Registry.register(
                    BuiltInRegistries.CREATIVE_MODE_TAB,
                    TAB_TEST_KEY,
                    TAB_TEST
            );


        }
    }
}
