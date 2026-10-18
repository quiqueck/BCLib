package org.betterx.bclib.config;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.util.JsonFactory;

import net.fabricmc.loader.api.FabricLoader;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Older versions read {@code config/<mod>/blocks.json}, {@code items.json} and similar files (entities, mobs,
 * enchantments, biomes, ...) to skip registering individual entries. Disabling them broke structures, features,
 * biomes, recipes and loot tables, so the option was removed: everything is always registered now. These helpers deal
 * with leftover entries in existing config files.
 */
public final class IgnoredConfigEntries {
    private IgnoredConfigEntries() {
    }

    /**
     * Warns when a config file that is no longer read disables blocks or items. The file itself is left alone. Nested
     * entries are listed with their full path (e.g. {@code tools/cincinnasite_pickaxe}).
     */
    public static void warnIfDisabled(String modID, String group) {
        warnIfDisabled(modID, group, "blocks and items", "registered");
    }

    /**
     * Warns when a config file that is no longer read disables entries. The file itself is left alone. Nested entries
     * are listed with their full path (e.g. {@code mobs/spawn_egg_naga}).
     *
     * @param what what can no longer be disabled, used in the message (e.g. {@code "entities"})
     */
    public static void warnIfDisabled(String modID, String group, String what) {
        warnIfDisabled(modID, group, what, "enabled");
    }

    private static void warnIfDisabled(String modID, String group, String what, String result) {
        final File file = configFile(modID, group);
        if (!file.isFile()) return;
        final List<String> disabled = new ArrayList<>();
        collectDisabledPaths(load(file), "", disabled);
        if (!disabled.isEmpty()) {
            BCLib.LOGGER.warning(
                    "{} disables {} ({}), but {} can no longer be disabled. "
                            + "The file is ignored and everything is {}; delete it to silence this warning.",
                    file, entries(disabled.size()), String.join(", ", disabled), what, result
            );
        }
    }

    /**
     * Removes the boolean enable/disable entries at the root of a config file that is still used for other values,
     * so the file no longer offers them. Disabled entries are reported in the log.
     */
    public static void removeRootSwitches(String modID, String group) {
        final File file = configFile(modID, group);
        if (!file.isFile()) return;
        final JsonElement root = load(file);
        if (root == null || !root.isJsonObject()) return;

        final List<String> disabled = new ArrayList<>();
        collectDisabledSwitches(root, disabled);
        final JsonObject obj = root.getAsJsonObject();
        final List<String> switches = obj.entrySet()
                                         .stream()
                                         .filter(e -> isBoolean(e.getValue()))
                                         .map(Map.Entry::getKey)
                                         .toList();
        if (switches.isEmpty()) return;

        switches.forEach(obj::remove);
        JsonFactory.storeJson(file, obj);
        if (!disabled.isEmpty()) {
            BCLib.LOGGER.warning(
                    "{} disabled {} ({}), but blocks and items can no longer be disabled. "
                            + "The entries were removed and everything is registered.",
                    file, entries(disabled.size()), String.join(", ", disabled)
            );
        }
    }

    private static File configFile(String modID, String group) {
        return FabricLoader.getInstance().getConfigDir().resolve(modID).resolve(group + ".json").toFile();
    }

    private static JsonElement load(File file) {
        try {
            return JsonFactory.loadJson(file);
        } catch (Exception e) {
            // an unreadable leftover is just as ignored as a readable one
            return null;
        }
    }

    private static boolean isBoolean(JsonElement value) {
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean();
    }

    private static String entryName(String key) {
        return key.replaceFirst("\\s*\\[default: .*]$", "");
    }

    private static void collectDisabledPaths(JsonElement element, String prefix, List<String> disabled) {
        if (element == null || !element.isJsonObject()) return;
        for (Map.Entry<String, JsonElement> e : element.getAsJsonObject().entrySet()) {
            final JsonElement value = e.getValue();
            final String path = prefix + entryName(e.getKey());
            if (value.isJsonObject()) {
                collectDisabledPaths(value, path + "/", disabled);
            } else if (isBoolean(value) && !value.getAsBoolean()) {
                disabled.add(path);
            }
        }
    }

    /**
     * Collects the disabled boolean entries at the root of a config file.
     */
    private static void collectDisabledSwitches(JsonElement element, List<String> disabled) {
        if (element == null || !element.isJsonObject()) return;
        for (Map.Entry<String, JsonElement> e : element.getAsJsonObject().entrySet()) {
            if (isBoolean(e.getValue()) && !e.getValue().getAsBoolean()) {
                disabled.add(entryName(e.getKey()));
            }
        }
    }

    private static String entries(int count) {
        return count == 1 ? "1 entry" : count + " entries";
    }
}
