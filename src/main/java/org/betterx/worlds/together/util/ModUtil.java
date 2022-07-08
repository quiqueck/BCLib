package org.betterx.worlds.together.util;

import org.betterx.worlds.together.WorldsTogether;

import net.fabricmc.loader.api.*;
import net.fabricmc.loader.api.metadata.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModUtil {
    private static Map<String, ModInfo> mods;

    /**
     * Unloads the cache of available mods created from {@link #getMods()}
     */
    public static void invalidateCachedMods() {
        mods = null;
    }

    /**
     * return a map of all mods that were found in the 'mods'-folder.
     * <p>
     * The method will cache the results. You can clear that cache (and free the memory) by
     * calling {@link #invalidateCachedMods()}
     * <p>
     * An error message is printed if a mod fails to load, but the parsing will continue.
     *
     * @return A map of all found mods. (key=ModID, value={@link ModInfo})
     */
    public static Map<String, ModInfo> getMods() {
        if (mods != null) return mods;

        mods = new HashMap<>();
        PathUtil.fileWalker(PathUtil.MOD_FOLDER.toFile(), false, (ModUtil::accept));

        return mods;
    }

    private static ModMetadata readJSON(InputStream is, String sourceFile) throws IOException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(
                is,
                StandardCharsets.UTF_8
        ))) {
            JsonObject data = JsonParser.parseReader(reader)
                                        .getAsJsonObject();
            Version ver;
            try {
                ver = SemanticVersion.parse(data.get("version").getAsString());
            } catch (VersionParsingException e) {
                WorldsTogether.LOGGER.error("Unable to parse Version in " + sourceFile);
                return null;
            }

            if (data.get("id") == null) {
                WorldsTogether.LOGGER.error("Unable to read ID in " + sourceFile);
                return null;
            }

            if (data.get("name") == null) {
                WorldsTogether.LOGGER.error("Unable to read name in " + sourceFile);
                return null;
            }

            return new ModMetadata() {
                @Override
                public Version getVersion() {
                    return ver;
                }

                @Override
                public String getType() {
                    return "fabric";
                }

                @Override
                public String getId() {
                    return data.get("id")
                               .getAsString();
                }

                @Override
                public Collection<String> getProvides() {
                    return new ArrayList<>();
                }

                @Override
                public ModEnvironment getEnvironment() {
                    JsonElement env = data.get("environment");
                    if (env == null) {
                        WorldsTogether.LOGGER.warning("No environment specified in " + sourceFile);
                        //return ModEnvironment.UNIVERSAL;
                    }
                    final String environment = env == null ? "" : env.getAsString()
                                                                     .toLowerCase(Locale.ROOT);

                    if (environment.isEmpty() || environment.equals("*") || environment.equals("\"*\"") || environment.equals(
                            "common")) {
                        JsonElement entrypoints = data.get("entrypoints");
                        boolean hasClient = true;

                        //check if there is an actual client entrypoint
                        if (entrypoints != null && entrypoints.isJsonObject()) {
                            JsonElement client = entrypoints.getAsJsonObject()
                                                            .get("client");
                            if (client != null && client.isJsonArray()) {
                                hasClient = client.getAsJsonArray()
                                                  .size() > 0;
                            } else if (client == null || !client.isJsonPrimitive()) {
                                hasClient = false;
                            } else if (!client.getAsJsonPrimitive()
                                              .isString()) {
                                hasClient = false;
                            }
                        }

                        //if (hasClient == false) return ModEnvironment.SERVER;
                        return ModEnvironment.UNIVERSAL;
                    } else if (environment.equals("client")) {
                        return ModEnvironment.CLIENT;
                    } else if (environment.equals("server")) {
                        return ModEnvironment.SERVER;
                    } else {
                        WorldsTogether.LOGGER.error("Unable to read environment in " + sourceFile);
                        return ModEnvironment.UNIVERSAL;
                    }
                }

                @Override
                public Collection<ModDependency> getDepends() {
                    return new ArrayList<>();
                }

                @Override
                public Collection<ModDependency> getRecommends() {
                    return new ArrayList<>();
                }

                @Override
                public Collection<ModDependency> getSuggests() {
                    return new ArrayList<>();
                }

                @Override
                public Collection<ModDependency> getConflicts() {
                    return new ArrayList<>();
                }

                @Override
                public Collection<ModDependency> getBreaks() {
                    return new ArrayList<>();
                }

                public Collection<ModDependency> getDependencies() {
                    return new ArrayList<>();
                }

                @Override
                public String getName() {
                    return data.get("name")
                               .getAsString();
                }

                @Override
                public String getDescription() {
                    return "";
                }

                @Override
                public Collection<Person> getAuthors() {
                    return new ArrayList<>();
                }

                @Override
                public Collection<Person> getContributors() {
                    return new ArrayList<>();
                }

                @Override
                public ContactInformation getContact() {
                    return null;
                }

                @Override
                public Collection<String> getLicense() {
                    return new ArrayList<>();
                }

                @Override
                public Optional<String> getIconPath(int size) {
                    return Optional.empty();
                }

                @Override
                public boolean containsCustomValue(String key) {
                    return false;
                }

                @Override
                public CustomValue getCustomValue(String key) {
                    return null;
                }

                @Override
                public Map<String, CustomValue> getCustomValues() {
                    return new HashMap<>();
                }

                @Override
                public boolean containsCustomElement(String key) {
                    return false;
                }

                public JsonElement getCustomElement(String key) {
                    return null;
                }
            };
        }
    }

    /**
     * Returns the {@link ModInfo} or {@code null} if the mod was not found.
     * <p>
     * The call will also return null if the mode-Version in the jar-File is not the same
     * as the version of the loaded Mod.
     *
     * @param modID The mod ID to query
     * @return A {@link ModInfo}-Object for the querried Mod.
     */
    public static ModInfo getModInfo(String modID) {
        return getModInfo(modID, true);
    }

    public static ModInfo getModInfo(String modID, boolean matchVersion) {
        getMods();
        final ModInfo mi = mods.get(modID);
        if (mi == null || (matchVersion && !getModVersion(modID).equals(mi.getVersion()))) return null;
        return mi;
    }

    /**
     * Local Mod Version for the queried Mod
     *
     * @param modID The mod ID to query
     * @return The version of the locally installed Mod
     */
    public static String getModVersion(String modID) {
        Optional<ModContainer> optional = FabricLoader.getInstance()
                                                      .getModContainer(modID);
        if (optional.isPresent()) {
            ModContainer modContainer = optional.get();
            return ModInfo.versionToString(modContainer.getMetadata()
                                                       .getVersion());

        }

        return getModVersionFromJar(modID);
    }

    /**
     * Local Mod Version for the queried Mod from the Jar-File in the games mod-directory
     *
     * @param modID The mod ID to query
     * @return The version of the locally installed Mod
     */
    public static String getModVersionFromJar(String modID) {
        final ModInfo mi = getModInfo(modID, false);
        if (mi != null) return mi.getVersion();

        return "0.0.0";
    }

    /**
     * Get mod version from string. String should be in format: %d.%d.%d
     *
     * @param version - {@link String} mod version.
     * @return int mod version.
     */
    public static int convertModVersion(String version) {
        if (version.isEmpty()) {
            return 0;
        }
        try {
            int res = 0;
            final String semanticVersionPattern = "(\\d+)\\.(\\d+)(\\.(\\d+))?\\D*";
            final Matcher matcher = Pattern.compile(semanticVersionPattern)
                                           .matcher(version);
            if (matcher.find()) {
                if (matcher.groupCount() > 0)
                    res = matcher.group(1) == null ? 0 : ((Integer.parseInt(matcher.group(1)) & 0xFF) << 22);
                if (matcher.groupCount() > 1)
                    res |= matcher.group(2) == null ? 0 : ((Integer.parseInt(matcher.group(2)) & 0xFF) << 14);
                if (matcher.groupCount() > 3)
                    res |= matcher.group(4) == null ? 0 : Integer.parseInt(matcher.group(4)) & 0x3FFF;
            }

            return res;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Get mod version from integer. String will be in format %d.%d.%d
     *
     * @param version - mod version in integer form.
     * @return {@link String} mod version.
     */
    public static String convertModVersion(int version) {
        int a = (version >> 22) & 0xFF;
        int b = (version >> 14) & 0xFF;
        int c = version & 0x3FFF;
        return String.format(Locale.ROOT, "%d.%d.%d", a, b, c);
    }

    /**
     * {@code true} if the version v1 is larger than v2
     *
     * @param v1 A Version string
     * @param v2 Another Version string
     * @return v1 &gt; v2
     */
    public static boolean isLargerVersion(String v1, String v2) {
        return convertModVersion(v1) > convertModVersion(v2);
    }

    /**
     * {@code true} if the version v1 is larger or equal v2
     *
     * @param v1 A Version string
     * @param v2 Another Version string
     * @return v1 &ge; v2
     */
    public static boolean isLargerOrEqualVersion(String v1, String v2) {
        return convertModVersion(v1) >= convertModVersion(v2);
    }

    private static void accept(Path file) {
        try {
            URI uri = URI.create("jar:" + file.toUri());

            FileSystem fs;
            // boolean doClose = false;
            try {
                fs = FileSystems.getFileSystem(uri);
            } catch (Exception e) {
                // doClose = true;
                fs = FileSystems.newFileSystem(file);
            }
            if (fs != null) {
                try {
                    Path modMetaFile = fs.getPath("fabric.mod.json");
                    if (modMetaFile != null) {
                        try (InputStream is = Files.newInputStream(modMetaFile)) {
                            //ModMetadata mc = ModMetadataParser.parseMetadata(is, uri.toString(), new LinkedList<String>());
                            ModMetadata mc = readJSON(is, uri.toString());
                            if (mc != null) {
                                mods.put(mc.getId(), new ModInfo(mc, file));
                            }
                        }
                    }
                } catch (Exception e) {
                    WorldsTogether.LOGGER.error("Error for " + uri + ": " + e);
                }
                //if (doClose) fs.close();
            }
        } catch (Exception e) {
            WorldsTogether.LOGGER.error("Error for " + file.toUri() + ": " + e);
            e.printStackTrace();
        }
    }

    public static class ModInfo {
        public final ModMetadata metadata;
        public final Path jarPath;

        ModInfo(ModMetadata metadata, Path jarPath) {
            this.metadata = metadata;
            this.jarPath = jarPath;
        }

        public static String versionToString(Version v) {
            if (v instanceof SemanticVersion) {
                return versionToString((SemanticVersion) v);
            }
            return convertModVersion(convertModVersion(v.toString()));
        }

        public static String versionToString(SemanticVersion v) {
            StringBuilder stringBuilder = new StringBuilder();
            boolean first = true;
            final int cCount = Math.min(v.getVersionComponentCount(), 3);
            for (int i = 0; i < cCount; i++) {
                if (first) {
                    first = false;
                } else {
                    stringBuilder.append('.');
                }

                stringBuilder.append(v.getVersionComponent(i));
            }

            return stringBuilder.toString();
        }

        @Override
        public String toString() {
            return "ModInfo{" + "id=" + metadata.getId() + ", version=" + metadata.getVersion() + ", jarPath=" + jarPath + '}';
        }

        public String getVersion() {
            if (metadata == null) {
                return "0.0.0";
            }
            return versionToString(metadata.getVersion());
        }
    }
}
