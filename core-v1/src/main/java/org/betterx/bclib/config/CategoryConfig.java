package org.betterx.bclib.config;

public class CategoryConfig extends IdConfig {

    public CategoryConfig(String modID, String group) {
        super(modID, group, (id, category) -> {
            return new ConfigKey(id.getPath(), id.getNamespace(), category);
        });
    }
}
