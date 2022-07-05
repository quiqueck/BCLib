package org.betterx.bclib.util;

import org.betterx.bclib.BCLib;
import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.util.PathUtil;

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

/**
 * @deprecated Replaced by {@link org.betterx.worlds.together.util.ModUtil}
 */
@Deprecated(forRemoval = true)
public class ModUtil {
    private static Map<String, ModInfo> mods;

    /**
     * @deprecated Replaced by {@link org.betterx.worlds.together.util.ModUtil#invalidateCachedMods()}
     */
    @Deprecated(forRemoval = true)
    public static void invalidateCachedMods() {
        mods = null;
    }

    /**
     * @deprecated Replaced by {@link org.betterx.worlds.together.util.ModUtil#getMods()} ()}
     */
    @Deprecated(forRemoval = true)
    public static Map<String, ModInfo> getMods() {
        if (mods != null) return mods;

        mods = new HashMap<>();
        PathUtil.fileWalker(PathUtil.MOD_FOLDER.toFile(), false, (ModUtil::accept));

        return mods;
    }

    private static ModMetadata readJSON(InputStream is, String sourceFile) throws IOException {
        try (com.google.gson.stream.JsonReader reader = new JsonReader(new InputStreamReader(
                is,
                StandardCharsets.UTF_8
        ))) {
            JsonObject data = JsonParser.parseReader(reader)
                                        .getAsJsonObject();
            Version ver;
            try {
                ver = SemanticVersion.parse(data.get("version").getAsString());
            } catch (VersionParsingException e) {
                BCLib.LOGGER.error("Unable to parse Version in " + sourceFile);
                return null;
            }

            if (data.get("id") == null) {
                BCLib.LOGGER.error("Unable to read ID in " + sourceFile);
                return null;
            }

            if (data.get("name") == null) {
                BCLib.LOGGER.error("Unable to read name in " + sourceFile);
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
                        BCLib.LOGGER.warning("No environment specified in " + sourceFile);
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
                        BCLib.LOGGER.error("Unable to read environment in " + sourceFile);
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
     * @deprecated Replaced by {@link org.betterx.worlds.together.util.ModUtil#getModInfo(String)}
     */
    @Deprecated(forRemoval = true)
    public static ModInfo getModInfo(String modID) {
        return getModInfo(modID, true);
    }

    /**
     * @deprecated Replaced by {@link org.betterx.worlds.together.util.ModUtil#getModInfo(String, boolean)}
     */
    @Deprecated(forRemoval = true)
    public static ModInfo getModInfo(String modID, boolean matchVersion) {
        getMods();
        final ModInfo mi = mods.get(modID);
        if (mi == null || (matchVersion && !org.betterx.worlds.together.util.ModUtil.getModVersion(modID)
                                                                                    .equals(mi.getVersion())))
            return null;
        return mi;
    }

    /**
     * @deprecated Replaced by {@link org.betterx.worlds.together.util.ModUtil#getModVersion(String)}
     */
    @Deprecated(forRemoval = true)
    public static String getModVersion(String modID) {
        if (modID == WorldsTogether.MOD_ID) modID = BCLib.MOD_ID;

        Optional<ModContainer> optional = FabricLoader.getInstance()
                                                      .getModContainer(modID);
        if (optional.isPresent()) {
            ModContainer modContainer = optional.get();
            return org.betterx.worlds.together.util.ModUtil.ModInfo.versionToString(modContainer.getMetadata()
                                                                                                .getVersion());

        }

        return org.betterx.worlds.together.util.ModUtil.getModVersionFromJar(modID);
    }

    /**
     * @deprecated Replaced by {@link org.betterx.worlds.together.util.ModUtil#getModVersionFromJar(String)}
     */
    @Deprecated(forRemoval = true)
    public static String getModVersionFromJar(String modID) {
        final ModInfo mi = getModInfo(modID, false);
        if (mi != null) return mi.getVersion();

        return "0.0.0";
    }

    /**
     * @deprecated Replaced by {@link org.betterx.worlds.together.util.ModUtil#convertModVersion(String)}
     */
    @Deprecated(forRemoval = true)
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
     * @deprecated Replaced by {@link org.betterx.worlds.together.util.ModUtil#convertModVersion(int)}
     */
    @Deprecated(forRemoval = true)
    public static String convertModVersion(int version) {
        int a = (version >> 22) & 0xFF;
        int b = (version >> 14) & 0xFF;
        int c = version & 0x3FFF;
        return String.format(Locale.ROOT, "%d.%d.%d", a, b, c);
    }

    /**
     * @deprecated Replaced by {@link org.betterx.worlds.together.util.ModUtil#isLargerVersion(String, String)}
     */
    @Deprecated(forRemoval = true)
    public static boolean isLargerVersion(String v1, String v2) {
        return org.betterx.worlds.together.util.ModUtil.convertModVersion(v1) > org.betterx.worlds.together.util.ModUtil.convertModVersion(
                v2);
    }

    /**
     * @deprecated Replaced by {@link org.betterx.worlds.together.util.ModUtil#isLargerOrEqualVersion(String, String)}
     */
    @Deprecated(forRemoval = true)
    public static boolean isLargerOrEqualVersion(String v1, String v2) {
        return org.betterx.worlds.together.util.ModUtil.convertModVersion(v1) >= org.betterx.worlds.together.util.ModUtil.convertModVersion(
                v2);
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
                    BCLib.LOGGER.error("Error for " + uri + ": " + e);
                }
                //if (doClose) fs.close();
            }
        } catch (Exception e) {
            BCLib.LOGGER.error("Error for " + file.toUri() + ": " + e);
            e.printStackTrace();
        }
    }

    /**
     * @deprecated Replaced by {@link org.betterx.worlds.together.util.ModUtil.ModInfo}
     */
    @Deprecated(forRemoval = true)
    public static class ModInfo {
        public final ModMetadata metadata;
        public final Path jarPath;

        ModInfo(ModMetadata metadata, Path jarPath) {
            this.metadata = metadata;
            this.jarPath = jarPath;
        }

        /**
         * @deprecated Replaced by {@link org.betterx.worlds.together.util.ModUtil.ModInfo#versionToString(Version)}
         */
        @Deprecated(forRemoval = true)
        public static String versionToString(Version v) {
            if (v instanceof SemanticVersion) {
                return org.betterx.worlds.together.util.ModUtil.ModInfo.versionToString((SemanticVersion) v);
            }
            return org.betterx.worlds.together.util.ModUtil.convertModVersion(
                    org.betterx.worlds.together.util.ModUtil.convertModVersion(v.toString())
            );
        }

        /**
         * @deprecated Replaced by {@link org.betterx.worlds.together.util.ModUtil.ModInfo#versionToString(SemanticVersion)}
         */
        @Deprecated(forRemoval = true)
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
            return org.betterx.worlds.together.util.ModUtil.ModInfo.versionToString(metadata.getVersion());
        }
    }
}
