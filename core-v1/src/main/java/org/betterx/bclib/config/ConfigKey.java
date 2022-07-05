package org.betterx.bclib.config;

import org.betterx.bclib.util.Pair;

import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

public class ConfigKey {
    private final String[] path;
    private final String entry;
    private final boolean root;

    public ConfigKey(String entry, String... path) {
        this.validate(entry);
        this.path = path;
        this.entry = entry;
        this.root = path.length == 0 || (path.length == 1 && path[0].isEmpty());
    }

    public ConfigKey(String entry, ResourceLocation path) {
        this(entry, path.getNamespace(), path.getPath());
    }

    public String[] getPath() {
        return path;
    }

    public String getEntry() {
        return entry;
    }

    public boolean isRoot() {
        return root;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(path);
        result = prime * result + entry.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ConfigKey)) {
            return false;
        }
        ConfigKey other = (ConfigKey) obj;
        if (other.path.length != path.length) {
            return false;
        }
        for (int i = 0; i < path.length; i++) {
            if (!path[i].equals(other.path[i])) {
                return false;
            }
        }
        return entry.equals(other.entry);
    }

    @Override
    public String toString() {
        if (root) {
            return String.format("[root]:%s", entry);
        }
        String p = path[0];
        for (int i = 1; i < path.length; i++) {
            p += "." + path[i];
        }
        return String.format("%s:%s", p, entry);
    }

    private void validate(String entry) {
        if (entry == null) {
            throw new NullPointerException("Config key must be not null!");
        }
        if (entry.isEmpty()) {
            throw new IndexOutOfBoundsException("Config key must be not empty!");
        }
    }

    public static Pair<String, String> realKey(@NotNull String key) {
        String[] parts = key.split("\\[default:", 2);
        if (parts.length == 1) {
            return new Pair(parts[0].trim(), "");
        } else if (parts.length == 2) {
            return new Pair(parts[0].trim(), " " + ("[default:" + parts[1]).trim());
        }
        return new Pair(key, "");
    }
}
