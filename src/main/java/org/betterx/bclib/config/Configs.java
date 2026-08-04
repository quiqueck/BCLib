package org.betterx.bclib.config;

public class Configs {
    public static final String MAIN_PATCH_CATEGORY = "patches";

    public static final ClientConfig CLIENT_CONFIG = de.ambertation.wover.config.api.Configs.register(ClientConfig::new);
    public static final MainConfig MAIN_CONFIG = de.ambertation.wover.config.api.Configs.register(MainConfig::new);


    public static void save() {
        de.ambertation.wover.config.api.Configs.saveConfigs();
    }
}
