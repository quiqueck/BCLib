package org.betterx.bclib.config;

import de.ambertation.wunderlib.configs.ConfigFile;
import org.betterx.bclib.BCLib;
import org.betterx.wover.config.api.MainConfig;

public class ClientConfig extends ConfigFile {

    public final BooleanValue customFogRendering = new BooleanValue(
            MainConfig.RENDERING_GROUP.title(),
            "custom_fog_rendering",
            true
    ).setGroup(MainConfig.RENDERING_GROUP);

    public final BooleanValue thickFogNether = new BooleanValue(
            MainConfig.RENDERING_GROUP.title(),
            "nether_thick_fog",
            true
    ).setGroup(MainConfig.RENDERING_GROUP).setDependency(customFogRendering);

    public final FloatValue fogDensity = new FloatValue(
            MainConfig.RENDERING_GROUP.title(),
            "fog_density",
            1.0f
    ).setGroup(MainConfig.RENDERING_GROUP).setDependency(customFogRendering).min(0.0f).max(2.0f);


    public final BooleanValue survivesOnHint = new BooleanValue(
            MainConfig.UI_GROUP.title(),
            "survives_on_hint",
            true
    ).setGroup(MainConfig.UI_GROUP);

    public ClientConfig() {
        super(BCLib.C, "client");
    }

    public boolean netherThickFog() {
        return thickFogNether.get();
    }

    public boolean renderCustomFog() {
        return customFogRendering.get();
    }

    public float fogDensity() {
        return fogDensity.get();
    }

    public boolean survivesOnHint() {
        return survivesOnHint.get();
    }
}
