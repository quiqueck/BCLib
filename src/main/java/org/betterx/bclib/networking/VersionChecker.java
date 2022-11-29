package org.betterx.bclib.networking;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.config.Configs;
import org.betterx.worlds.together.util.ModUtil;

import net.fabricmc.loader.api.FabricLoader;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;

public class VersionChecker implements Runnable {
    @FunctionalInterface
    public interface UpdateInfoProvider {
        void send(String modID, String currentVersion, String newVersion);
    }

    private static final List<String> KNOWN_MODS = new LinkedList<>();
    private static final List<ModVersion> NEW_VERSIONS = new LinkedList<>();

    public static class ModVersion {
        String n;
        String v;

        @Override
        public String toString() {
            return n + ":" + v;
        }
    }

    public static class Versions {
        String mc;
        String loader;
        List<ModVersion> mods;

        @Override
        public String toString() {
            return "Versions{" +
                    "mc='" + mc + '\'' +
                    ", loader='" + loader + '\'' +
                    ", mods=" + mods +
                    '}';
        }
    }

    public static final int WAIT_FOR_DAYS = 5;
    private static final String BASE_URL = "https://wunderreich.ambertation.de/api/v1/versions/";
    private static Thread versionChecker;

    public static void startCheck(boolean isClient) {
        if (versionChecker == null && isClient) {
            if (Configs.CLIENT_CONFIG.checkVersions() && Configs.CLIENT_CONFIG.didShowWelcomeScreen()) {
                versionChecker = new Thread(isClient ? new VersionCheckerClient() : new VersionChecker());
                versionChecker.start();
            }
        }
    }

    public static void registerMod(String modID) {
        KNOWN_MODS.add(modID);
    }

    boolean needRecheck() {
        Instant lastCheck = Configs.CACHED_CONFIG.lastCheckDate().plus(WAIT_FOR_DAYS, ChronoUnit.DAYS);
        Instant now = Instant.now();

        return now.isAfter(lastCheck);
    }

    @Override
    public void run() {
        Gson gson = new Gson();
        if (needRecheck()) {
            String minecraftVersion = ModUtil.getModVersion("minecraft").replace(".", "_");
            BCLib.LOGGER.info("Check Versions for minecraft=" + minecraftVersion);

            try {
                String fileName = "mc_fabric_" + URLEncoder.encode(
                        minecraftVersion,
                        StandardCharsets.ISO_8859_1.toString()
                ) + ".json";

                URL url = new URL(BASE_URL + fileName);
                try (InputStreamReader reader = new InputStreamReader(url.openStream())) {
                    Versions json = gson.fromJson(reader, Versions.class);
                    String str = gson.getAdapter(Versions.class).toJson(json);
                    Configs.CACHED_CONFIG.setLastVersionJson(str);
                    Configs.CACHED_CONFIG.setLastCheckDate();
                    Configs.CACHED_CONFIG.saveChanges();

                    processVersions(json);
                }
            } catch (UnsupportedEncodingException e) {
                BCLib.LOGGER.error("Failed to encode URL during VersionCheck", e);
                return;
            } catch (MalformedURLException e) {
                BCLib.LOGGER.error("Invalid URL during VersionCheck", e);
                return;
            } catch (IOException e) {
                BCLib.LOGGER.error("I/O Error during VersionCheck", e);
                return;
            }
        } else {
            String str = Configs.CACHED_CONFIG.lastVersionJson();
            if (str != null && str.trim().length() > 0) {
                Versions json = gson.fromJson(str, Versions.class);
                processVersions(json);
            }
        }
    }

    private void processVersions(Versions json) {
        if (json != null) {
            BCLib.LOGGER.info("Received Version Info for minecraft=" + json.mc + ", loader=" + json.loader);
            if (json.mods != null) {
                for (ModVersion mod : json.mods) {
                    if (!KNOWN_MODS.contains(mod.n)) {
                        if (FabricLoader.getInstance().getModContainer(mod.n).isPresent())
                            registerMod(mod.n);
                    }
                    if (mod.n != null && mod.v != null && KNOWN_MODS.contains(mod.n)) {
                        String installedVersion = ModUtil.getModVersion(mod.n);
                        boolean isNew = ModUtil.isLargerVersion(mod.v, installedVersion)
                                && !installedVersion.equals("0.0.0");
                        BCLib.LOGGER.info(" - " + mod.n + ":" + mod.v + (isNew ? " (update available)" : ""));
                        if (isNew)
                            NEW_VERSIONS.add(mod);
                    }
                }
            }
        } else {
            BCLib.LOGGER.warning("No valid Version Info");
        }
    }

    public static boolean isEmpty() {
        return NEW_VERSIONS.isEmpty();
    }

    public static void forEachUpdate(UpdateInfoProvider consumer) {
        for (ModVersion v : NEW_VERSIONS) {
            String currrent = ModUtil.getModVersion(v.n);
            consumer.send(v.n, currrent, v.v);
        }
    }
}
