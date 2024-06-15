package org.betterx.bclib.config;

public class Configs {
    public static final String MAIN_PATCH_CATEGORY = "patches";

    public static final ClientConfig CLIENT_CONFIG = new ClientConfig();
    public static final MainConfig MAIN_CONFIG = new MainConfig();


    public static void save() {
        org.betterx.wover.config.api.Configs.saveConfigs();
    }
}
