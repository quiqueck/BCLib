package org.betterx.bclib.config;

import org.betterx.bclib.util.JsonFactory;

import net.fabricmc.loader.api.FabricLoader;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.nio.file.Path;

public class ConfigWriter {
    private final static Path GAME_CONFIG_DIR = FabricLoader.getInstance().getConfigDir();

    private final File configFile;
    private JsonObject configObject;

    public ConfigWriter(String modID, String configFile) {
        this.configFile = new File(GAME_CONFIG_DIR.resolve(modID).toFile(), configFile + ".json");
        File parent = this.configFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        this.load();
    }

    File getConfigFile() {
        return this.configFile;
    }

    public JsonObject getConfig() {
        return configObject;
    }

    public void save() {
        if (configObject == null) {
            return;
        }
        save(configFile, configObject);
    }

    JsonObject reload() {
        configObject = load(configFile);
        return configObject;
    }

    public JsonObject load() {
        if (configObject == null) {
            configObject = load(configFile);
        }
        return configObject;
    }

    public void save(JsonElement config) {
        this.configObject = config.getAsJsonObject();
        save(configFile, config);
    }

    public static JsonObject load(File configFile) {
        return JsonFactory.getJsonObject(configFile);
    }

    public static void save(File configFile, JsonElement config) {
        JsonFactory.storeJson(configFile, config);
    }

    public static String scrubFileName(String input) {
        input = input.replaceAll("[/\\ ]+", "_");
        input = input.replaceAll("[,:&\"\\|\\<\\>\\?\\*]", "_");
        return input;
    }
}
