package org.betterx.bclib.util;

import org.betterx.bclib.BCLib;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.Nullable;

public class JsonFactory {
    public final static Gson GSON = new GsonBuilder().setPrettyPrinting()
                                                     .create();

    public static JsonObject getJsonObject(InputStream stream) {
        try {
            Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            JsonElement json = loadJson(reader);
            if (json != null && json.isJsonObject()) {
                JsonObject jsonObject = json.getAsJsonObject();
                return jsonObject != null ? jsonObject : new JsonObject();
            }
        } catch (Exception ex) {
            BCLib.LOGGER.catching(ex);
        }
        return new JsonObject();
    }

    public static JsonObject getJsonObject(File jsonFile) {
        if (jsonFile.exists()) {
            JsonElement json = loadJson(jsonFile);
            if (json != null && json.isJsonObject()) {
                JsonObject jsonObject = json.getAsJsonObject();
                return jsonObject != null ? jsonObject : new JsonObject();
            }
        }
        return new JsonObject();
    }

    /**
     * Loads {@link JsonObject} from resource location using Minecraft resource manager. Can be used to load JSON from resourcepacks and resources.
     *
     * @param location {@link ResourceLocation} to JSON file
     * @return {@link JsonObject}
     */
    @Nullable
    @Environment(EnvType.CLIENT)
    public static JsonObject getJsonObject(ResourceLocation location) {
        ResourceManager manager = Minecraft.getInstance()
                                           .getResourceManager();
        JsonObject obj = null;
        try {
            Resource resource = manager.getResource(location).orElse(null);
            if (resource != null) {
                InputStream stream = resource.open();
                InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                obj = JsonFactory.GSON.fromJson(reader, JsonObject.class);
                reader.close();
                stream.close();
            }
        } catch (IOException ex) {
        }
        return obj;
    }

    public static JsonElement loadJson(File jsonFile) {
        if (jsonFile.exists()) {
            try (Reader reader = new FileReader(jsonFile, StandardCharsets.UTF_8)) {
                return loadJson(reader);
            } catch (Exception ex) {
                BCLib.LOGGER.catching(ex);
            }
        }
        return null;
    }

    public static JsonElement loadJson(Reader reader) {
        return GSON.fromJson(reader, JsonElement.class);
    }

    public static void storeJson(File jsonFile, JsonElement jsonObject) {
        try (FileWriter writer = new FileWriter(jsonFile, StandardCharsets.UTF_8)) {
            String json = GSON.toJson(jsonObject);
            writer.write(json);
            writer.flush();
        } catch (IOException ex) {
            BCLib.LOGGER.catching(ex);
        }
    }

    public static void storeJson(OutputStream outStream, JsonElement jsonObject) {
        OutputStreamWriter writer = new OutputStreamWriter(outStream, StandardCharsets.UTF_8);
        GSON.toJson(jsonObject, writer);
        try {
            writer.flush();
        } catch (IOException e) {
            BCLib.LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public static int getInt(JsonObject object, String member, int def) {
        JsonElement elem = object.get(member);
        return elem == null ? def : elem.getAsInt();
    }

    public static float getFloat(JsonObject object, String member, float def) {
        JsonElement elem = object.get(member);
        return elem == null ? def : elem.getAsFloat();
    }

    public static boolean getBoolean(JsonObject object, String member, boolean def) {
        JsonElement elem = object.get(member);
        return elem == null ? def : elem.getAsBoolean();
    }

    public static String getString(JsonObject object, String member, String def) {
        JsonElement elem = object.get(member);
        return elem == null ? def : elem.getAsString();
    }

}
