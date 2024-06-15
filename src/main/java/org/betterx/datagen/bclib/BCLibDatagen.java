package org.betterx.datagen.bclib;

import org.betterx.bclib.BCLib;
import org.betterx.datagen.bclib.advancement.BCLAdvancementDataProvider;
import org.betterx.datagen.bclib.advancement.RecipeDataProvider;
import org.betterx.datagen.bclib.integrations.NullscapeBiomes;
import org.betterx.datagen.bclib.worldgen.BiomeDatagenProvider;
import org.betterx.datagen.bclib.worldgen.BlockTagProvider;
import org.betterx.datagen.bclib.worldgen.ItemTagProvider;
import org.betterx.wover.core.api.ModCore;
import org.betterx.wover.datagen.api.PackBuilder;
import org.betterx.wover.datagen.api.WoverDataGenEntryPoint;
import org.betterx.wover.tag.datagen.BiomeTagProvider;

import net.minecraft.core.RegistrySetBuilder;

public class BCLibDatagen extends WoverDataGenEntryPoint {

    @Override
    protected void onInitializeProviders(PackBuilder globalPack) {
        BCLib.LOGGER.info("Bootstrap onInitializeDataGenerator");
        NullscapeBiomes.ensureStaticallyLoaded();

        //globalPack.addProvider((mc) -> new org.betterx.wover.tag.datagen.BlockTagProvider(Wover.C));
        //globalPack.addProvider((mc) -> new org.betterx.wover.tag.datagen.ItemTagProvider(Wover.C));
        globalPack.addProvider(BiomeTagProvider::new);
        globalPack.addProvider(BlockTagProvider::new);
        globalPack.addProvider(ItemTagProvider::new);

//        globalPack.addRegistryProvider(WorldPresetProvider::new);
//        globalPack.addRegistryProvider(NoiseGeneratorSettingsProvider::new);
//        globalPack.addMultiProvider(VanillaBiomeDataProvider::new);
//        globalPack.addRegistryProvider(WorldPresetInfoProvider::new);
//
//        globalPack.addRegistryProvider(NoiseRegistryProvider::new);

        globalPack.callOnInitializeDatapack((generator, pack, location) -> {
            if (location == null) {

                pack.addProvider(BiomeDatagenProvider::new);


//                pack.addProvider(BlockTagProvider::new);
//                pack.addProvider(ItemTagProvider::new);
                pack.addProvider(RecipeDataProvider::new);
                //pack.addProvider(BCLibRegistriesDataProvider::new);
                pack.addProvider(BCLAdvancementDataProvider::new);
            }
        });
    }

    @Override
    protected ModCore modCore() {
        return BCLib.C;
    }

    @Override
    protected void onBuildRegistry(RegistrySetBuilder registryBuilder) {
        super.onBuildRegistry(registryBuilder);
        BCLRegistrySupplier.INSTANCE.bootstrapRegistries(registryBuilder);
    }
}
