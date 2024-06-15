package org.betterx.bclib.config;

import de.ambertation.wunderlib.configs.ConfigFile;
import org.betterx.bclib.BCLib;

public class MainConfig extends ConfigFile {
    public final static Group PATCH_GROUP = new Group(BCLib.C.namespace, Configs.MAIN_PATCH_CATEGORY, 0);

    public final BooleanValue applyPatches = new BooleanValue(
            PATCH_GROUP.title(),
            "apply_patches",
            true
    ).setGroup(PATCH_GROUP);


    public MainConfig() {
        super(BCLib.C, "main");
    }

    public boolean applyPatches() {
        return applyPatches.get();
    }
}
