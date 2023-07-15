package org.betterx.bclib;

import org.betterx.bclib.api.v2.datafixer.DataFixerAPI;
import org.betterx.bclib.api.v2.datafixer.ForcedLevelPatch;
import org.betterx.bclib.api.v2.datafixer.MigrationProfile;
import org.betterx.bclib.api.v2.datafixer.Patch;
import org.betterx.bclib.api.v2.generator.GeneratorOptions;
import org.betterx.bclib.api.v2.levelgen.LevelGenUtil;
import org.betterx.bclib.config.Configs;

import net.minecraft.nbt.CompoundTag;

import java.util.Map;

public final class BCLibPatch {
    public static void register() {
        if (Configs.MAIN_CONFIG.repairBiomes() && (GeneratorOptions.fixEndBiomeSource() || GeneratorOptions.fixNetherBiomeSource())) {
            DataFixerAPI.registerPatch(BiomeSourcePatch::new);
        }
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

final class BiomeSourcePatch extends ForcedLevelPatch {
    protected BiomeSourcePatch() {
        super(BCLib.MOD_ID, "1.2.1");
    }

    @Override
    protected Boolean runLevelDatPatch(CompoundTag root, MigrationProfile profile) {
        //make sure we have a working generators file before attempting to patch
        LevelGenUtil.migrateGeneratorSettings();

        return false;
    }
}
