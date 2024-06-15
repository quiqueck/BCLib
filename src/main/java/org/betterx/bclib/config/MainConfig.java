package org.betterx.bclib.config;

import org.betterx.bclib.BCLib;

public class MainConfig extends NamedPathConfig {
    public static final ConfigToken<Boolean> APPLY_PATCHES = ConfigToken.Boolean(
            true,
            "applyPatches",
            Configs.MAIN_PATCH_CATEGORY
    );


    public MainConfig() {
        super(BCLib.MOD_ID, "main");
    }

    public boolean applyPatches() {
        return get(APPLY_PATCHES);
    }
}
