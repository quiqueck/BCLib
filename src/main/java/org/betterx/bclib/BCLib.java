package org.betterx.bclib;

import org.betterx.bclib.api.v2.FuelValueRegistration;
import org.betterx.bclib.api.v2.levelgen.LevelGenEvents;
import org.betterx.bclib.api.v2.levelgen.structures.TemplatePiece;
import org.betterx.bclib.api.v3.tag.BCLBlockTags;
import org.betterx.bclib.commands.CommandRegistry;
import org.betterx.bclib.commands.arguments.BCLibArguments;
import org.betterx.bclib.config.Configs;
import org.betterx.bclib.mixin.common.ItemAccessor;
import org.betterx.bclib.recipes.AlloyingRecipe;
import org.betterx.bclib.recipes.AnvilRecipe;
import org.betterx.bclib.registry.BaseBlockEntities;
import org.betterx.bclib.util.BCLDataComponents;
import org.betterx.datagen.bclib.worldgen.BCLAutoBlockTagProvider;
import org.betterx.datagen.bclib.worldgen.BCLAutoItemTagProvider;
import de.ambertation.wover.core.api.Logger;
import de.ambertation.wover.core.api.ModCore;
import de.ambertation.wover.datagen.api.WoverDataGenEntryPoint;
import de.ambertation.wover.state.api.WorldConfig;
import de.ambertation.wover.ui.api.VersionChecker;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class BCLib implements ModInitializer {
    public static final ModCore C = ModCore.create("bclib");
    public static final String MOD_ID = C.namespace;
    public static final Logger LOGGER = C.LOG;

    public static final boolean RUNS_NULLSCAPE = FabricLoader.getInstance()
                                                             .getModContainer("nullscape")
                                                             .isPresent();

    private void onDatagen() {

    }


    @Override
    public void onInitialize() {
        BCLibArguments.register();
        FuelValueRegistration.register();
        LevelGenEvents.register();
        BCLDataComponents.ensureStaticInitialization();
        BaseBlockEntities.register();
        WorldConfig.registerMod(C);
        AnvilRecipe.register();
        AlloyingRecipe.register();
        CommandRegistry.register();
        BCLBlockTags.ensureStaticallyLoaded();

        BCLibPatch.register();
        TemplatePiece.ensureStaticInitialization();
        Configs.save();

        VersionChecker.registerMod(C);

        // Water Bottles are potions and vanilla does not give them a crafting-remainder item
        // (the empty bottle) - fix that once here, no need to wait for other mods to load.
        ItemStackTemplate potionRemainder = ((ItemAccessor) Items.POTION).bcl_craftingRemainingItem();
        if (potionRemainder == null || potionRemainder.item().value() == Items.AIR) {
            ((ItemAccessor) Items.POTION).bcl_setCraftingRemainingItem(new ItemStackTemplate(Items.GLASS_BOTTLE));
        }

        if (isDatagen()) {
            WoverDataGenEntryPoint.registerAutoProvider(BCLAutoBlockTagProvider::new);
            WoverDataGenEntryPoint.registerAutoProvider(BCLAutoItemTagProvider::new);
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

    public static Identifier makeID(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

}
