package org.betterx.bclib.config;

import org.betterx.bclib.BCLib;

public class MainConfig extends NamedPathConfig {
    public static final ConfigToken<Boolean> APPLY_PATCHES = ConfigToken.Boolean(
            true,
            "applyPatches",
            Configs.MAIN_PATCH_CATEGORY
    );

    @ConfigUI(leftPadding = 8)
    public static final ConfigToken<Boolean> REPAIR_BIOMES = DependendConfigToken.Boolean(
            false,
            "repairBiomesOnLoad",
            Configs.MAIN_PATCH_CATEGORY,
            (config) -> config.get(
                    APPLY_PATCHES)
    );


    @ConfigUI(hide = true)
    public static final ConfigToken<Boolean> DID_SHOW_WELCOME = ConfigToken.Boolean(
            false,
            "did_show_welcome",
            "version"
    );

    public static final ConfigToken<Boolean> CHECK_VERSIONS = DependendConfigToken.Boolean(
            true,
            "check",
            "version",
            (config) -> !config.get(DID_SHOW_WELCOME)
    );


    public MainConfig() {
        super(BCLib.MOD_ID, "main", true, true);
    }

    public boolean applyPatches() {
        return get(APPLY_PATCHES);
    }

    public boolean repairBiomes() {
        return get(REPAIR_BIOMES);
    }

    public boolean checkVersions() {
        return get(CHECK_VERSIONS);
    }

    public boolean didShowWelcomeScreen() {
        return get(DID_SHOW_WELCOME);
    }

    public void setDidShowWelcomeScreen() {
        set(DID_SHOW_WELCOME, true);
    }


    public void setCheckVersions(boolean newValue) {
        set(CHECK_VERSIONS, newValue);
    }
}
