package org.betterx.datagen.bclib;

import org.betterx.bclib.BCLib;
import org.betterx.datagen.bclib.advancement.BCLAdvancementDataProvider;
import org.betterx.datagen.bclib.advancement.RecipeDataProvider;
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
        globalPack.addProvider(BiomeTagProvider::new);
        globalPack.addProvider(BlockTagProvider::new);
        globalPack.addProvider(ItemTagProvider::new);

        globalPack.callOnInitializeDatapack((generator, pack, location) -> {
            if (location == null) {
                pack.addProvider(RecipeDataProvider::new);
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
    }
}
