package org.betterx.worlds.together.util;

import org.betterx.bclib.BCLib;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class DatapackConfigs {
    @FunctionalInterface
    public interface DatapackConfigReloadHandler {
        void onReload(ResourceLocation id, JsonObject root);
    }

    @FunctionalInterface
    public interface DatapackConfigReloadPrepare {
        void onReload();
    }

    private static DatapackConfigs INSTANCE = new DatapackConfigs();

    public static DatapackConfigs instance() {
        return INSTANCE;
    }

    private Map<ResourceLocation, DatapackConfigReloadHandler> handlers = new HashMap<>();

    public void register(
            String modID,
            String fileName,
            DatapackConfigReloadHandler handler
    ) {
        register(
                modID, fileName, () -> {
                    //nothing to do
                }, handler
        );
    }

    public void register(
            String modID,
            String fileName,
            DatapackConfigReloadPrepare prepare,
            DatapackConfigReloadHandler handler
    ) {
        final ResourceLocation handlerID = new ResourceLocation(
                modID,
                "config_manager_" + fileName.replaceAll("/", "_").replaceAll(".", "_")
        );
        ResourceManagerHelper
                .get(PackType.SERVER_DATA)
                .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return handlerID;
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager manager) {
                        prepare.onReload();
                        runForResources(manager, modID, fileName, handler);
                    }
                });
    }

    public void runForResources(
            ResourceManager manager,
            String modID,
            String fileName,
            DatapackConfigReloadHandler handler
    ) {
        for (Map.Entry<ResourceLocation, Resource> entry : manager.listResources(
                "config",
                id -> id.getNamespace().equals(modID) &&
                        id.getPath().endsWith(fileName)
        ).entrySet()) {
            try (Reader reader = entry.getValue().openAsReader()) {
                final JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                if (obj != null)
                    handler.onReload(entry.getKey(), obj);
            } catch (Exception e) {
                BCLib.LOGGER.error(
                        "Error occurred while loading resource json " + entry.getKey(),
                        e
                );
            }
        }
    }
}
