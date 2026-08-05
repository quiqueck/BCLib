package org.betterx.datagen.bclib;

import org.betterx.bclib.BCLib;
import org.betterx.datagen.bclib.advancement.BCLAdvancementDataProvider;
import org.betterx.datagen.bclib.worldgen.BlockTagProvider;
import org.betterx.datagen.bclib.worldgen.BoneMealBlockTagProvider;
import de.ambertation.wover.core.api.ModCore;
import de.ambertation.wover.datagen.api.PackBuilder;
import de.ambertation.wover.datagen.api.WoverDataGenEntryPoint;

import net.minecraft.core.RegistrySetBuilder;

public class BCLibDatagen extends WoverDataGenEntryPoint {
    @Override
    protected void onInitializeProviders(PackBuilder globalPack) {
        if (MinecraftAuditDatagen.isEnabled()) {
            // Audit-only run (./gradlew :runMinecraftAudit). Its output folder is the shared
            // vanilla reference checkout, not this mod's generated folder, so BCLib's own
            // providers must stay out of it - they would drop bclib tags and advancements in
            // there. Nothing but the four vanilla snapshots is generated.
            BCLib.LOGGER.info("Bootstrap onInitializeDataGenerator (minecraft audit only)");
            MinecraftAuditDatagen.addProviders(globalPack);
            return;
        }

        BCLib.LOGGER.info("Bootstrap onInitializeDataGenerator");
        globalPack.addProvider(BoneMealBlockTagProvider::new);
        globalPack.addProvider(BlockTagProvider::new);

        globalPack.callOnInitializeDatapack((generator, pack, location) -> {
            if (location == null) {
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
