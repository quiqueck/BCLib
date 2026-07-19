package org.betterx.bclib.client;

import org.betterx.bclib.api.v2.ModIntegrationAPI;
import org.betterx.bclib.api.v2.PostInitAPI;
import org.betterx.bclib.api.v2.dataexchange.DataExchangeAPI;
import org.betterx.bclib.client.textures.AtlasSetManager;
import org.betterx.bclib.registry.BaseBlockEntityRenders;

import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;

import net.fabricmc.api.ClientModInitializer;

public class BCLibClient implements ClientModInitializer {


    @Override
    public void onInitializeClient() {

        ModIntegrationAPI.registerAll();
        BaseBlockEntityRenders.register();
        DataExchangeAPI.prepareClientside();
        PostInitAPI.postInit(true);

        AtlasSetManager.addSource(AtlasSetManager.VANILLA_BLOCKS, new DirectoryLister("entity/chest", "entity/chest/"));
        AtlasSetManager.addSource(AtlasSetManager.VANILLA_BLOCKS, new DirectoryLister("blocks", "blocks/"));
    }


}
