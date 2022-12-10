package org.betterx.bclib.api.v3.datagen;

import org.betterx.bclib.api.v2.advancement.AdvancementManager;

import net.minecraft.advancements.Advancement;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;

import java.util.List;
import java.util.function.Consumer;

public abstract class AdvancementDataProvider extends FabricAdvancementProvider {
    protected final List<String> modIDs;

    protected AdvancementDataProvider(List<String> modIDs, FabricDataOutput output) {
        super(output);
        this.modIDs = modIDs;
    }

    protected abstract void bootstrap();

    @Override
    public void generateAdvancement(Consumer<Advancement> consumer) {
        bootstrap();
        AdvancementManager.registerAllDataGen(modIDs, consumer);
    }
}
