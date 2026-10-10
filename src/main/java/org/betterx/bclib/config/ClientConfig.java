package org.betterx.bclib.config;

import de.ambertation.wunderlib.configs.ConfigFile;
import org.betterx.bclib.BCLib;
import de.ambertation.wover.config.api.MainConfig;

public class ClientConfig extends ConfigFile {

    // The custom fog renderer (CustomFogRenderer/FogRendererMixin) was removed with the 1.21.2+ fog rewrite,
    // so nothing reads these three values anymore. Keep the keys for existing config files and API
    // compatibility, but hide them from the config screen instead of offering dead controls.
    public final BooleanValue customFogRendering = new BooleanValue(
            MainConfig.RENDERING_GROUP.title(),
            "custom_fog_rendering",
            true
    ).setGroup(MainConfig.RENDERING_GROUP).hideInUI();

    public final BooleanValue thickFogNether = new BooleanValue(
            MainConfig.RENDERING_GROUP.title(),
            "nether_thick_fog",
            true
    ).setGroup(MainConfig.RENDERING_GROUP).setDependency(customFogRendering).hideInUI();

    public final FloatValue fogDensity = new FloatValue(
            MainConfig.RENDERING_GROUP.title(),
            "fog_density",
            1.0f
    ).setGroup(MainConfig.RENDERING_GROUP).setDependency(customFogRendering).min(0.0f).max(2.0f).hideInUI();


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
