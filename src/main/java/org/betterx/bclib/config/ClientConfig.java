package org.betterx.bclib.config;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.dataexchange.handler.autosync.AutoSync;

public class ClientConfig extends NamedPathConfig {

    @ConfigUI(hide = true)
    public static final ConfigToken<Boolean> DID_SHOW_WELCOME = ConfigToken.Boolean(
            false,
            "didShowWelcome",
            "version"
    );
    public static final ConfigToken<Boolean> CHECK_VERSIONS = ConfigToken.Boolean(
            true,
            "check",
            "version"
    );
    @ConfigUI(leftPadding = 8)
    public static final ConfigToken<Boolean> SHOW_UPDATE_INFO = ConfigToken.Boolean(
            true,
            "showUpdateInfo",
            "ui"
    );

    @ConfigUI(leftPadding = 8)
    public static final ConfigToken<Boolean> PREFER_MODRINTH_FOR_UPDATES = ConfigToken.Boolean(
            false,
            "useModrinthForUpdates",
            "ui"
    );

    @ConfigUI(hide = true)
    public static final ConfigToken<Boolean> FORCE_BETTERX_PRESET = ConfigToken.Boolean(
            true,
            "forceBetterXPreset",
            "ui"
    );
    @ConfigUI(topPadding = 12)
    public static final ConfigToken<Boolean> SUPPRESS_EXPERIMENTAL_DIALOG = ConfigToken.Boolean(
            false,
            "suppressExperimentalDialogOnLoad",
            "ui"
    );


    @ConfigUI(hide = true)
    public static final ConfigToken<Boolean> NO_DONOR = ConfigToken.Boolean(
            true,
            "notTheDonorType",
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
            true,
            "acceptMods",
            AutoSync.SYNC_CATEGORY,
            (config) -> config.get(ENABLED)
    );
    @ConfigUI(leftPadding = 8)
    public static final DependendConfigToken<Boolean> DISPLAY_MOD_INFO = DependendConfigToken.Boolean(
            true,
            "displayModInfo",
            AutoSync.SYNC_CATEGORY,
            (config) -> config.get(ENABLED)
    );

    @ConfigUI(leftPadding = 8)
    public static final ConfigToken<Boolean> DEBUG_HASHES = ConfigToken.Boolean(
            false,
            "debugHashes",
            AutoSync.SYNC_CATEGORY
    );

    @ConfigUI(topPadding = 12)
    public static final ConfigToken<Boolean> CUSTOM_FOG_RENDERING = ConfigToken.Boolean(
            true,
            "customFogRendering",
            "rendering"
    );
    @ConfigUI(leftPadding = 8)
    public static final ConfigToken<Boolean> NETHER_THICK_FOG = DependendConfigToken.Boolean(
            true,
            "netherThickFog",
            "rendering",
            (config) -> config.get(CUSTOM_FOG_RENDERING)
    );

    @ConfigUI(leftPadding = 8, minValue = 0, maxValue = 2)
    public static final ConfigToken<Float> FOG_DENSITY = DependendConfigToken.Float(
            1.0f,
            "FogDensity",
            "rendering",
            (config) -> config.get(CUSTOM_FOG_RENDERING)
    );

    @ConfigUI(topPadding = 12)
    public static final ConfigToken<Boolean> SURVIES_ON_HINT = ConfigToken.Boolean(
            true,
            "survives_on_hint",
            Configs.MAIN_INFO_CATEGORY
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

    public void setSuppressExperimentalDialog(boolean newValue) {
        set(ClientConfig.SUPPRESS_EXPERIMENTAL_DIALOG, newValue);
    }

    public boolean netherThickFog() {
        return get(NETHER_THICK_FOG);
    }

    public boolean renderCustomFog() {
        return get(CUSTOM_FOG_RENDERING);
    }

    public boolean showUpdateInfo() {
        return get(SHOW_UPDATE_INFO);
    }

    public boolean isDonor() {
        return !get(NO_DONOR);
    }

    public float fogDensity() {
        return get(FOG_DENSITY);
    }

    public boolean checkVersions() {
        return get(ClientConfig.CHECK_VERSIONS);
    }


    public void setCheckVersions(boolean newValue) {
        set(ClientConfig.CHECK_VERSIONS, newValue);
    }

    public boolean didShowWelcomeScreen() {
        return get(ClientConfig.DID_SHOW_WELCOME);
    }

    public void setDidShowWelcomeScreen() {
        set(ClientConfig.DID_SHOW_WELCOME, true);
    }

    public boolean forceBetterXPreset() {
        return get(FORCE_BETTERX_PRESET);
    }

    public void setForceBetterXPreset(boolean v) {
        set(FORCE_BETTERX_PRESET, v);
    }

    public boolean survivesOnHint() {
        return get(ClientConfig.SURVIES_ON_HINT);
    }

    public boolean preferModrinthForUpdates() {
        return get(ClientConfig.PREFER_MODRINTH_FOR_UPDATES);
    }
}
