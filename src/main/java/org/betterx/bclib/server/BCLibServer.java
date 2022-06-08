package org.betterx.bclib.server;

import org.betterx.bclib.api.v2.ModIntegrationAPI;
import org.betterx.bclib.api.v2.PostInitAPI;
import org.betterx.bclib.api.v2.dataexchange.DataExchangeAPI;

import net.fabricmc.api.DedicatedServerModInitializer;

public class BCLibServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        ModIntegrationAPI.registerAll();
        DataExchangeAPI.prepareServerside();

        PostInitAPI.postInit(false);
    }

}
