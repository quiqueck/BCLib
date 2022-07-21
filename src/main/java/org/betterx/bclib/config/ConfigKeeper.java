package org.betterx.bclib.config;

import org.betterx.bclib.api.v2.dataexchange.handler.autosync.FileContentWrapper;
import org.betterx.bclib.util.JsonFactory;
import org.betterx.bclib.util.Pair;

import net.minecraft.util.GsonHelper;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public final class ConfigKeeper {
    private final Map<ConfigKey, Entry<?>> configEntries = Maps.newHashMap();
    private final ConfigWriter writer;

    private JsonObject configObject;
    private boolean changed = false;

    public ConfigKeeper(String modID, String group) {
        this.writer = new ConfigWriter(modID, group);
        this.configObject = writer.load();
    }

    public File getConfigFile() {
        return this.writer.getConfigFile();
    }

    boolean compareAndUpdateForSync(FileContentWrapper content) {
        ByteArrayInputStream inputStream = content.getInputStream();
        final JsonObject other = JsonFactory.getJsonObject(inputStream);

        boolean changed = this.compareAndUpdateForSync(other);
        if (changed) {
            OutputStream outStream = content.getEmptyOutputStream();
            JsonFactory.storeJson(outStream, this.configObject);
            content.syncWithOutputStream();
        }
        return changed;
    }

    boolean compareAndUpdateForSync(JsonObject other) {
        return compareAndUpdateForSync(this.configObject, other);
    }

    private static Pair<JsonElement, Pair<String, String>> find(JsonObject json, Pair<String, String> key) {
        String kk = key.first + key.second;
        for (var entry : json.entrySet()) {
            final Pair<String, String> otherKey = ConfigKey.realKey(entry.getKey());
            if (kk.equals(entry)) return new Pair<>(entry.getValue(), otherKey);
        }

//        for (var entry : json.entrySet()) {
//            final Pair<String, String> otherKey = ConfigKey.realKey(entry.getKey());
//            if (otherKey.first.equals(key.first)) return new Pair<>(entry.getValue(), otherKey);
//        }

        return null;
    }

    /**
     * Called for content based auto-sync.
     *
     * @param me    - When called in AutoSync this represents the content of the client.
     * @param other - When called in AutoSync, this represents the content of the server
     * @return {@code true} if content was changed
     */
    static boolean compareAndUpdateForSync(JsonObject me, JsonObject other) {
        boolean changed = false;
        for (var otherEntry : other.entrySet()) {
            final Pair<String, String> otherKey = ConfigKey.realKey(otherEntry.getKey());
            final JsonElement otherValue = otherEntry.getValue();

            Pair<JsonElement, Pair<String, String>> temp = find(me, otherKey);
            //we already have an entry
            if (temp != null) {
                final Pair<String, String> myKey = temp.second;
                final JsonElement myValue = temp.first;

                if ((otherValue.isJsonNull() && !myValue.isJsonNull()) || (otherValue.isJsonPrimitive() && !myValue.isJsonPrimitive()) || (otherValue.isJsonObject() && !myValue.isJsonObject()) || (otherValue.isJsonArray() && !myValue.isJsonArray())) {
                    //types are different => replace with "server"-version in other
                    changed = true;
                    me.add(myKey.first + myKey.second, otherValue);
                } else if (otherValue.isJsonPrimitive() || otherValue.isJsonArray() || otherValue.isJsonNull()) {
                    if (!otherValue.equals(myValue)) {
                        changed = true;
                        me.add(myKey.first + myKey.second, otherValue);
                    }
                } else if (otherValue.isJsonObject()) {
                    changed |= compareAndUpdateForSync(myValue.getAsJsonObject(), otherValue.getAsJsonObject());
                }
            } else { //no entry, just copy the value from other
                if (!otherValue.isJsonNull()) {
                    changed = true;
                    temp = find(me, otherKey);
                    me.add(otherKey.first + otherKey.second, otherValue);
                }
            }
        }

        return changed;
    }


    public void save() {
        if (!changed) return;
        this.writer.save();
        this.changed = false;
    }

    void reload() {
        this.configObject = this.writer.reload();
        this.configEntries.clear();
        this.changed = false;
    }

    private <T, E extends Entry<T>> void initializeEntry(ConfigKey key, E entry) {
        if (configObject == null) {
            return;
        }
        String[] path = key.getPath();
        JsonObject obj = configObject;

        if (!key.isRoot()) {
            for (String group : path) {
                JsonElement element = obj.get(group);
                if (element == null || !element.isJsonObject()) {
                    element = new JsonObject();
                    obj.add(group, element);
                }
                obj = element.getAsJsonObject();
            }
        }

        String paramKey = key.getEntry();
        if (entry.hasDefaultInName()) {
            paramKey += " [default: " + entry.getDefault() + "]";
        }

        this.changed |= entry.setLocation(obj, paramKey);
    }

    private <T, E extends Entry<T>> void storeValue(E entry, T value) {
        if (configObject == null) {
            return;
        }
        T val = entry.getValue();
        if (value.equals(val)) return;
        entry.toJson(value);
        this.changed = true;
    }

    private <T, E extends Entry<T>> T getValue(E entry) {
        if (!entry.hasLocation()) {
            return entry.getDefault();
        }
        return entry.fromJson();
    }

    @Nullable
    public <T, E extends Entry<T>> E getEntry(ConfigKey key, Class<E> type) {
        Entry<?> entry = this.configEntries.get(key);
        if (type.isInstance(entry)) {
            return type.cast(entry);
        }
        return null;
    }

    @Nullable
    public <T, E extends Entry<T>> T getValue(ConfigKey key, Class<E> type) {
        Entry<T> entry = this.getEntry(key, type);
        if (entry == null) {
            return null;
        }
        return entry.getValue();
    }

    public <T, E extends Entry<T>> E registerEntry(ConfigKey key, E entry) {
        entry.setWriter(value -> this.storeValue(entry, value));
        entry.setReader(() -> {
            return this.getValue(entry);
        });
        this.initializeEntry(key, entry);
        this.configEntries.put(key, entry);
        return entry;
    }

    public static class BooleanEntry extends Entry<Boolean> {

        public BooleanEntry(Boolean defaultValue) {
            super(defaultValue);
        }

        @Override
        public Boolean fromJson() {
            return GsonHelper.getAsBoolean(location, key, defaultValue);
        }

        @Override
        public void toJson(Boolean value) {
            this.location.addProperty(key, value);
        }
    }

    public static class FloatEntry extends Entry<Float> {

        public FloatEntry(Float defaultValue) {
            super(defaultValue);
        }

        @Override
        public Float fromJson() {
            return GsonHelper.getAsFloat(location, key, defaultValue);
        }

        @Override
        public void toJson(Float value) {
            this.location.addProperty(key, value);
        }
    }

    public static class FloatRange extends RangeEntry<Float> {

        public FloatRange(Float defaultValue, float minVal, float maxVal) {
            super(defaultValue, minVal, maxVal);
        }

        @Override
        public Float fromJson() {
            return GsonHelper.getAsFloat(location, key, defaultValue);
        }

        @Override
        public void toJson(Float value) {
            this.location.addProperty(key, value);
        }
    }

    public static class IntegerEntry extends Entry<Integer> {

        public IntegerEntry(Integer defaultValue) {
            super(defaultValue);
        }

        @Override
        public Integer getDefault() {
            return this.defaultValue;
        }

        @Override
        public Integer fromJson() {
            return GsonHelper.getAsInt(location, key, defaultValue);
        }

        @Override
        public void toJson(Integer value) {
            this.location.addProperty(key, value);
        }
    }

    public static class IntegerRange extends RangeEntry<Integer> {

        public IntegerRange(Integer defaultValue, int minVal, int maxVal) {
            super(defaultValue, minVal, maxVal);
        }

        @Override
        public Integer fromJson() {
            return GsonHelper.getAsInt(location, key, defaultValue);
        }

        @Override
        public void toJson(Integer value) {
            this.location.addProperty(key, value);
        }
    }

    public static class StringEntry extends Entry<String> {

        public StringEntry(String defaultValue) {
            super(defaultValue);
        }

        @Override
        public String fromJson() {
            return GsonHelper.getAsString(location, key, defaultValue);
        }

        @Override
        public void toJson(String value) {
            this.location.addProperty(key, value);
        }
    }

    public static abstract class ArrayEntry<T> extends Entry<List<T>> {
        public ArrayEntry(List<T> defaultValue) {
            super(defaultValue);
        }

        protected abstract T getValue(JsonElement element);
        protected abstract void add(JsonArray array, T element);

        private JsonArray toArray(List<T> input) {
            final JsonArray array = new JsonArray();
            input.forEach(s -> add(array, s));
            return array;
        }

        @Override
        public List<T> fromJson() {
            final JsonArray resArray = GsonHelper.getAsJsonArray(location, key, toArray(defaultValue));
            final List<T> res = new ArrayList<>(resArray.size());
            resArray.forEach(e -> res.add(getValue(e)));

            return res;
        }

        @Override
        public void toJson(List<T> value) {
            this.location.add(key, toArray(value));
        }
    }

    public static class StringArrayEntry extends ArrayEntry<String> {
        public StringArrayEntry(List<String> defaultValue) {
            super(defaultValue);
        }

        @Override
        protected String getValue(JsonElement el) {
            return el.getAsString();
        }

        protected void add(JsonArray array, String el) {
            array.add(el);
        }

        @Override
        protected boolean hasDefaultInName() {
            return false;
        }
    }

    public static class EnumEntry<T extends Enum<T>> extends Entry<T> {
        private final Type type;

        public EnumEntry(T defaultValue) {
            super(defaultValue);
            TypeToken<T> token = new TypeToken<T>() {
                private static final long serialVersionUID = 1L;
            };
            this.type = token.getType();
        }

        @Override
        public T getDefault() {
            return this.defaultValue;
        }

        @Override
        public T fromJson() {
            return JsonFactory.GSON.fromJson(location.get(key), type);
        }

        @Override
        public void toJson(T value) {
            location.addProperty(key, JsonFactory.GSON.toJson(value, type));
        }
    }

    public static abstract class RangeEntry<T extends Comparable<T>> extends Entry<T> {
        private final T min, max;

        public RangeEntry(T defaultValue, T minVal, T maxVal) {
            super(defaultValue);
            this.min = minVal;
            this.max = maxVal;
        }

        @Override
        public void setValue(T value) {
            super.setValue(value.compareTo(min) < 0 ? min : value.compareTo(max) > 0 ? max : value);
        }

        public T minValue() {
            return this.min;
        }

        public T maxValue() {
            return this.max;
        }
    }

    public static abstract class Entry<T> {
        protected final T defaultValue;
        protected Consumer<T> writer;
        protected Supplier<T> reader;
        protected JsonObject location;
        protected String key;

        public abstract T fromJson();

        public abstract void toJson(T value);

        public Entry(T defaultValue) {
            this.defaultValue = defaultValue;
        }

        protected void setWriter(Consumer<T> writer) {
            this.writer = writer;
        }

        protected void setReader(Supplier<T> reader) {
            this.reader = reader;
        }

        protected boolean setLocation(JsonObject location, String key) {
            this.location = location;
            this.key = key;
            if (!location.has(key)) {
                this.toJson(defaultValue);
                return true;
            }
            return false;
        }

        protected boolean hasLocation() {
            return this.location != null && this.key != null;
        }

        public T getValue() {
            return this.reader.get();
        }

        public void setValue(T value) {
            this.writer.accept(value);
        }

        public T getDefault() {
            return this.defaultValue;
        }

        public void setDefault() {
            this.setValue(defaultValue);
        }

        protected boolean hasDefaultInName() {
            return true;
        }
    }
}
