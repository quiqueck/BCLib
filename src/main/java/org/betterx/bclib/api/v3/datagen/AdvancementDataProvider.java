package org.betterx.bclib.api.v3.datagen;

import org.betterx.bclib.api.v2.advancement.AdvancementManager;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class AdvancementDataProvider extends FabricAdvancementProvider {
    protected final List<String> modIDs;

    protected AdvancementDataProvider(
            List<String> modIDs,
            FabricDataOutput output,
            CompletableFuture<HolderLookup.Provider> registryLookup
    ) {
        super(output, registryLookup);
        this.modIDs = modIDs;
    }

    protected abstract void bootstrap(HolderLookup.Provider registryLookup);

    @Override
    public void generateAdvancement(HolderLookup.Provider registryLookup, Consumer<AdvancementHolder> consumer) {
        bootstrap(registryLookup);
        AdvancementManager.registerAllDataGen(modIDs, consumer);
    }
}
