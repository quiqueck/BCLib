package org.betterx.datagen.bclib.worldgen;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v3.datagen.TagDataProvider;
import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.tag.v3.TagManager;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.item.Item;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ItemTagProvider extends TagDataProvider<Item> {
    public ItemTagProvider(
            FabricDataOutput output,
            CompletableFuture<HolderLookup.Provider> registriesFuture
    ) {
        super(TagManager.ITEMS, List.of(BCLib.MOD_ID, WorldsTogether.MOD_ID, "c"), output, registriesFuture);
    }

    public static void bootstrap(BootstapContext<Item> bootstrapContext) {

    }
}
