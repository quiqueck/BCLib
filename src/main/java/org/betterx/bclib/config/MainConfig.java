package org.betterx.bclib.config;

import org.betterx.bclib.BCLib;

public class MainConfig extends NamedPathConfig {
    public static final ConfigToken<Boolean> VERBOSE_LOGGING = ConfigToken.Boolean(
            true,
            "verbose",
            Configs.MAIN_INFO_CATEGORY
    );

    public static final ConfigToken<Boolean> SURVIES_ON_HINT = ConfigToken.Boolean(
            true,
            "survives_on_hint",
            Configs.MAIN_INFO_CATEGORY
    );
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


    public MainConfig() {
        super(BCLib.MOD_ID, "main", true, true);
    }

    public boolean applyPatches() {
        return get(APPLY_PATCHES);
    }

    public boolean repairBiomes() {
        return get(REPAIR_BIOMES);
    }

    public boolean verboseLogging() {
        return get(VERBOSE_LOGGING);
    }

    public boolean survivesOnHint() {
        return get(SURVIES_ON_HINT);
    }
}
