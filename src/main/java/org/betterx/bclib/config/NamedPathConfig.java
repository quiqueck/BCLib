package org.betterx.bclib.config;

import org.betterx.bclib.BCLib;

import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class NamedPathConfig extends PathConfig {
    public static class ConfigTokenDescription<T> {
        public final ConfigToken<T> token;
        public final String internalName;
        public final Boolean hidden;
        public final int leftPadding;
        public final int topPadding;
        public final int minRange;
        public final int maxRange;

        @SuppressWarnings("unchecked")
        ConfigTokenDescription(Field fl) throws IllegalAccessException {
            token = (ConfigToken<T>) fl.get(null);
            internalName = fl.getName();

            ConfigUI ui = fl.getAnnotation(ConfigUI.class);
            if (ui != null) {
                this.hidden = ui.hide();
                leftPadding = ui.leftPadding();
                topPadding = ui.topPadding();
                minRange = ui.minValue();
                maxRange = ui.maxValue();
            } else {
                this.hidden = false;
                this.leftPadding = 0;
                topPadding = 0;
                minRange = 0;
                maxRange = 0;
            }

        }

        public String getPath() {
            StringBuilder path = new StringBuilder();
            for (String p : token.getPath()) {
                path.append(".")
                    .append(p);

            }
            path.append(".").append(token.getEntry());
            return path.toString();
        }
    }

    public static class DependendConfigToken<T> extends ConfigToken<T> {
        protected final Predicate<NamedPathConfig> dependenciesTrue;

        protected DependendConfigToken(
                Class<?> type,
                T defaultValue,
                String entry,
                ResourceLocation path,
                Predicate<NamedPathConfig> dependenciesTrue
        ) {
            this(type, defaultValue, entry, new String[]{path.getNamespace(), path.getPath()}, dependenciesTrue);
        }

        protected DependendConfigToken(
                Class<?> type,
                T defaultValue,
                String entry,
                String path,
                Predicate<NamedPathConfig> dependenciesTrue
        ) {
            super(type, defaultValue, entry, path);
            this.dependenciesTrue = dependenciesTrue;
        }

        protected DependendConfigToken(
                Class<?> type,
                T defaultValue,
                String entry,
                String[] path,
                Predicate<NamedPathConfig> dependenciesTrue
        ) {
            super(type, defaultValue, entry, path);
            this.dependenciesTrue = dependenciesTrue;
        }

        public boolean dependenciesTrue(NamedPathConfig config) {
            return dependenciesTrue.test(config);
        }

        public static DependendConfigToken<Boolean> Boolean(
                boolean defaultValue,
                String entry,
                String path,
                Predicate<NamedPathConfig> dependenciesTrue
        ) {
            return new DependendConfigToken<Boolean>(
                    ConfigKeeper.BooleanEntry.class,
                    defaultValue,
                    entry,
                    path,
                    dependenciesTrue
            );
        }

        public static DependendConfigToken<Float> Float(
                float defaultValue,
                String entry,
                String path,
                Predicate<NamedPathConfig> dependenciesTrue
        ) {
            return new DependendConfigToken<Float>(
                    ConfigKeeper.FloatEntry.class,
                    defaultValue,
                    entry,
                    path,
                    dependenciesTrue
            );
        }
    }

    public static class ConfigToken<T> extends ConfigKey {
        public final T defaultValue;
        public final Class<?> type;

        protected ConfigToken(Class<?> type, T defaultValue, String entry, ResourceLocation path) {
            this(type, defaultValue, entry, path.getNamespace(), path.getPath());
        }

        @SuppressWarnings("unchecked")
        protected ConfigToken(Class<?> type, T defaultValue, String entry, String... path) {
            super(entry, path);
            this.defaultValue = defaultValue;

            this.type = type;
        }

        public boolean dependenciesTrue(NamedPathConfig config) {
            return true;
        }

        public static ConfigToken<Boolean> Boolean(boolean defaultValue, String entry, String path) {
            return new ConfigToken<Boolean>(ConfigKeeper.BooleanEntry.class, defaultValue, entry, path);
        }

        public static ConfigToken<Integer> Int(int defaultValue, String entry, String path) {
            return new ConfigToken<Integer>(ConfigKeeper.IntegerEntry.class, defaultValue, entry, path);
        }

        public static ConfigToken<Float> Float(float defaultValue, String entry, String path) {
            return new ConfigToken<Float>(ConfigKeeper.FloatEntry.class, defaultValue, entry, path);
        }

        public static ConfigToken<String> String(String defaultValue, String entry, String path) {
            return new ConfigToken<String>(ConfigKeeper.StringEntry.class, defaultValue, entry, path);
        }

        public static ConfigToken<List<String>> StringArray(List<String> defaultValue, String entry, String path) {
            return new ConfigToken<List<String>>(ConfigKeeper.StringArrayEntry.class, defaultValue, entry, path);
        }
    }

    public NamedPathConfig(String modID, String group, boolean autoSync, boolean diffContent) {
        super(modID, group, autoSync, diffContent);
        onInit();
    }

    public NamedPathConfig(String modID, String group, boolean autoSync) {
        super(modID, group, autoSync);
        onInit();
    }

    public NamedPathConfig(String modID, String group) {
        super(modID, group);
        onInit();
    }

    public List<ConfigTokenDescription<?>> getAllOptions() {
        List<ConfigTokenDescription<?>> res = new LinkedList<>();
        for (Field fl : this.getClass().getDeclaredFields()) {
            int modifiers = fl.getModifiers();
            if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && ConfigToken.class.isAssignableFrom(fl.getType())) {
                try {
                    res.add(new ConfigTokenDescription<>(fl));
                } catch (IllegalAccessException e) {
                    BCLib.LOGGER.error("Could not access " + fl);
                }
            }
        }
        return res;
    }

    protected void onInit() {
        getAllOptions().forEach(e -> get(e.token));
        this.saveChanges();
    }

    /**
     * The value without any check of {@link DependendConfigToken}
     * <p>
     * In most cases you probably want to use {@link #get(ConfigToken)}, we use this Method if we
     * present the actual value of the Settings from the Config File without any additional processing.
     *
     * @param what The Option you want to get
     * @param <T>  The Type of the Option
     * @return The Value of the Option (without checking the {@link DependendConfigToken}):
     */
    public <T> T getRaw(ConfigToken<T> what) {
        return _get(what, true);
    }

    /**
     * The value of an Option
     *
     * @param what he Option you want to get
     * @param <T>  The Type of the Option
     * @return The Value of the Option. If this option is a {@link DependendConfigToken}, the returned value
     * may not be the value from the config File. For Example, on a {@link Boolean}-Type the result is always false
     * if {@link DependendConfigToken#dependenciesTrue} returns {@code false}.
     */
    public <T> T get(ConfigToken<T> what) {
        return _get(what, false);
    }

    @SuppressWarnings("unchecked")
    private <T> T _get(ConfigToken<T> what, boolean raw) {
        if (ConfigKeeper.BooleanEntry.class.isAssignableFrom(what.type)) {
            return (T) _getBoolean((ConfigToken<Boolean>) what, raw);
        }
        if (ConfigKeeper.IntegerEntry.class.isAssignableFrom(what.type)) {
            return (T) _getInt((ConfigToken<Integer>) what);
        }
        if (ConfigKeeper.FloatEntry.class.isAssignableFrom(what.type)) {
            return (T) _getFloat((ConfigToken<Float>) what);
        }
        if (ConfigKeeper.StringEntry.class.isAssignableFrom(what.type)) {
            return (T) _getString((ConfigToken<String>) what);
        }
        if (ConfigKeeper.StringArrayEntry.class.isAssignableFrom(what.type)) {
            return (T) _getStringArray((ConfigToken<List<String>>) what);
        }
        return this._get(what);
    }

    private <T> T _get(ConfigToken<T> what) {
        BCLib.LOGGER.error(what + " has unsupported Type.");
        return what.defaultValue;
    }

    public void set(ConfigToken<Boolean> what, boolean value) {
        this.setBoolean(what, value);
    }

    private Boolean _getBoolean(ConfigToken<Boolean> what, boolean raw) {
        if (!raw && !what.dependenciesTrue(this)) {
            return false;
        }

        return this.getBoolean(what, what.defaultValue);
    }

    public void set(ConfigToken<Integer> what, int value) {
        this.setInt(what, value);
    }

    private Integer _getInt(ConfigToken<Integer> what) {
        return this.getInt(what, what.defaultValue);
    }

    public void set(ConfigToken<Float> what, float value) {
        this.setFloat(what, value);
    }

    private Float _getFloat(ConfigToken<Float> what) {
        return this.getFloat(what, what.defaultValue);
    }

    public void set(ConfigToken<String> what, String value) {
        this.setString(what, value);
    }

    private String _getString(ConfigToken<String> what) {
        return this.getString(what, what.defaultValue);
    }

    public void set(ConfigToken<List<String>> what, List<String> value) {
        this.setStringArray(what, value);
    }

    private List<String> _getStringArray(ConfigToken<List<String>> what) {
        return this.getStringArray(what, what.defaultValue);
    }


}
