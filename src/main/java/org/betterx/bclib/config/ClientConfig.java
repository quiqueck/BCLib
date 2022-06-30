package org.betterx.bclib.config;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.dataexchange.handler.autosync.AutoSync;

public class ClientConfig extends NamedPathConfig {
    public static final ConfigToken<Boolean> SUPPRESS_EXPERIMENTAL_DIALOG = ConfigToken.Boolean(
            false,
            "suppressExperimentalDialogOnLoad",
            "ui"
    );

    @ConfigUI(topPadding = 12)
    public static final ConfigToken<Boolean> ENABLED = ConfigToken.Boolean(true, "enabled", AutoSync.SYNC_CATEGORY);

    @ConfigUI(leftPadding = 8)
    public static final DependendConfigToken<Boolean> ACCEPT_CONFIGS = DependendConfigToken.Boolean(
            true,
            "acceptConfigs",
            AutoSync.SYNC_CATEGORY,
            (config) -> config.get(
                    ENABLED)
    );
    @ConfigUI(leftPadding = 8)
    public static final DependendConfigToken<Boolean> ACCEPT_FILES = DependendConfigToken.Boolean(
            true,
            "acceptFiles",
            AutoSync.SYNC_CATEGORY,
            (config) -> config.get(
                    ENABLED)
    );
    @ConfigUI(leftPadding = 8)
    public static final DependendConfigToken<Boolean> ACCEPT_MODS = DependendConfigToken.Boolean(
            false,
            "acceptMods",
            AutoSync.SYNC_CATEGORY,
            (config) -> config.get(
                    ENABLED)
    );
    @ConfigUI(leftPadding = 8)
    public static final DependendConfigToken<Boolean> DISPLAY_MOD_INFO = DependendConfigToken.Boolean(
            true,
            "displayModInfo",
            AutoSync.SYNC_CATEGORY,
            (config) -> config.get(
                    ENABLED)
    );

    @ConfigUI(topPadding = 12)
    public static final ConfigToken<Boolean> DEBUG_HASHES = ConfigToken.Boolean(
            false,
            "debugHashes",
            AutoSync.SYNC_CATEGORY
    );

    @ConfigUI(leftPadding = 8)
    public static final ConfigToken<Boolean> CUSTOM_FOG_RENDERING = ConfigToken.Boolean(
            true,
            "customFogRendering",
            "rendering"
    );
    @ConfigUI(leftPadding = 8)
    public static final ConfigToken<Boolean> NETHER_THICK_FOG = ConfigToken.Boolean(
            true,
            "netherThickFog",
            "rendering"
    );

    public static final ConfigToken<Float> FOG_DENSITY = ConfigToken.Float(
            1.0f,
            "FogDensity",
            "rendering"
    );

    public ClientConfig() {
        super(BCLib.MOD_ID, "client", false);
    }

    public boolean shouldPrintDebugHashes() {
        return get(DEBUG_HASHES);
    }

    public boolean isAllowingAutoSync() {
        return get(ENABLED);
    }

    public boolean isAcceptingMods() {
        return get(ACCEPT_MODS) /*&& isAllowingAutoSync()*/;
    }

    public boolean isAcceptingConfigs() {
        return get(ACCEPT_CONFIGS) /*&& isAllowingAutoSync()*/;
    }

    public boolean isAcceptingFiles() {
        return get(ACCEPT_FILES) /*&& isAllowingAutoSync()*/;
    }

    public boolean isShowingModInfo() {
        return get(DISPLAY_MOD_INFO) /*&& isAllowingAutoSync()*/;
    }

    public boolean suppressExperimentalDialog() {
        return get(SUPPRESS_EXPERIMENTAL_DIALOG);
    }

    public boolean netherThickFog() {
        return get(NETHER_THICK_FOG);
    }

    public boolean renderCustomFog() {
        return get(CUSTOM_FOG_RENDERING);
    }

    public float fogDensity() {
        return get(FOG_DENSITY);
    }
}
