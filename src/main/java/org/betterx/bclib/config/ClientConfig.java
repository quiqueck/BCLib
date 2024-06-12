package org.betterx.bclib.config;

import org.betterx.bclib.BCLib;

public class ClientConfig extends NamedPathConfig {
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
        super(BCLib.MOD_ID, "client");
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

    public boolean survivesOnHint() {
        return get(ClientConfig.SURVIES_ON_HINT);
    }
}
