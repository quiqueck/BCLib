package org.betterx.bclib.config;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.dataexchange.handler.autosync.AutoSync;

import java.util.ArrayList;
import java.util.List;

public class ServerConfig extends NamedPathConfig {
    public static final ConfigToken<Boolean> ENABLED = ConfigToken.Boolean(true, "enabled", AutoSync.SYNC_CATEGORY);
    public static final DependendConfigToken<Boolean> OFFER_CONFIGS = DependendConfigToken.Boolean(
            true,
            "offerConfigs",
            AutoSync.SYNC_CATEGORY,
            (config) -> config.get(ENABLED)
    );
    public static final DependendConfigToken<Boolean> OFFER_FILES = DependendConfigToken.Boolean(
            true,
            "offerFiles",
            AutoSync.SYNC_CATEGORY,
            (config) -> config.get(
                    ENABLED)
    );
    public static final DependendConfigToken<Boolean> OFFER_MODS = DependendConfigToken.Boolean(
            true,
            "offerMods",
            AutoSync.SYNC_CATEGORY,
            (config) -> config.get(
                    ENABLED)
    );
    public static final DependendConfigToken<Boolean> OFFER_ALL_MODS = DependendConfigToken.Boolean(
            false,
            "offerAllMods",
            AutoSync.SYNC_CATEGORY,
            (config) -> config.get(
                    OFFER_MODS)
    );
    public static final DependendConfigToken<Boolean> SEND_ALL_MOD_INFO = DependendConfigToken.Boolean(
            false,
            "sendAllModInfo",
            AutoSync.SYNC_CATEGORY,
            (config) -> config.get(
                    ENABLED)
    );


    public static final ConfigToken<List<String>> ADDITIONAL_MODS = ConfigToken.StringArray(
            new ArrayList<>(0),
            "additionalMods",
            AutoSync.SYNC_CATEGORY
    );
    public static final ConfigToken<List<String>> EXCLUDED_MODS = ConfigToken.StringArray(
            new ArrayList<>(0),
            "excludeMods",
            AutoSync.SYNC_CATEGORY
    );
    public static final ConfigToken<Boolean> FORCE_BETTERX_PRESET = ConfigToken.Boolean(
            true,
            "forceBetterXPreset",
            AutoSync.SYNC_CATEGORY
    );


    public ServerConfig() {
        super(BCLib.MOD_ID, "server", false);
    }

    public boolean isAllowingAutoSync() {
        return get(ENABLED);
    }

    public boolean isOfferingConfigs() {
        return get(OFFER_CONFIGS) /*&& isAllowingAutoSync()*/;
    }

    public boolean isOfferingFiles() {
        return get(OFFER_FILES) /*&& isAllowingAutoSync()*/;
    }

    public boolean isOfferingMods() {
        return get(OFFER_MODS) /*&& isAllowingAutoSync()*/;
    }

    public boolean isOfferingAllMods() {
        return get(OFFER_ALL_MODS) /*&& isAllowingAutoSync()*/;
    }

    public boolean isOfferingInfosForMods() {
        return get(SEND_ALL_MOD_INFO) /*&& isAllowingAutoSync()*/;
    }

    public boolean forceBetterXPreset() {
        return get(FORCE_BETTERX_PRESET);
    }

}
