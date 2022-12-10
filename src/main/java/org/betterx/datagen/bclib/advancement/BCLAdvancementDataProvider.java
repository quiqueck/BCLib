package org.betterx.datagen.bclib.advancement;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v3.datagen.AdvancementDataProvider;
import org.betterx.worlds.together.WorldsTogether;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import java.util.List;

public class BCLAdvancementDataProvider extends AdvancementDataProvider {
    public BCLAdvancementDataProvider(
            FabricDataOutput output
    ) {
        super(List.of(BCLib.MOD_ID, WorldsTogether.MOD_ID), output);
    }

    @Override
    protected void bootstrap() {

    }
}
