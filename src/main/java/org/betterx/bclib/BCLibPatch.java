package org.betterx.bclib;

import org.betterx.bclib.api.v2.datafixer.DataFixerAPI;
import org.betterx.bclib.api.v2.datafixer.Patch;

import java.util.Map;

public final class BCLibPatch {
    public static void register() {
        DataFixerAPI.registerPatch(SignPatch::new);
    }
}

class SignPatch extends Patch {
    public SignPatch() {
        super(BCLib.MOD_ID, "3.0.11");
    }

    @Override
    public Map<String, String> getIDReplacements() {
        return Map.ofEntries(
                Map.entry("bclib:sign", "minecraft:sign")
        );
    }
}


