package org.betterx.bclib.api.v2.levelgen.biomes;


public class VanillaBiomeSettings extends BCLBiomeSettings {
    public static class Builder extends BCLBiomeSettings.CommonBuilder<VanillaBiomeSettings, VanillaBiomeSettings.Builder> {
        public Builder() {
            super(new VanillaBiomeSettings());
        }
    }

    public static Builder createVanilla() {
        return new Builder();
    }
}
