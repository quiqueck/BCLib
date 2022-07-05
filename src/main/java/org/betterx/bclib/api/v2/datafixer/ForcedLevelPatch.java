package org.betterx.bclib.api.v2.datafixer;

import org.betterx.bclib.interfaces.PatchBiFunction;
import org.betterx.bclib.interfaces.PatchFunction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;


/**
 * A Patch for level.dat that is always executed no matter what Patchlevel is set in a world.
 */
public abstract class ForcedLevelPatch extends Patch {
    protected ForcedLevelPatch(@NotNull String modID, String version) {
        super(modID, version, true);
    }

    @Override
    public final Map<String, String> getIDReplacements() {
        return new HashMap<String, String>();
    }

    @Override
    public final PatchFunction<CompoundTag, Boolean> getWorldDataPatcher() {
        return null;
    }

    @Override
    public final PatchBiFunction<ListTag, ListTag, Boolean> getBlockStatePatcher() {
        return null;
    }

    @Override
    public final List<String> getWorldDataIDPaths() {
        return null;
    }

    @Override
    public PatchFunction<CompoundTag, Boolean> getLevelDatPatcher() {
        return this::runLevelDatPatch;
    }

    /**
     * Called with the contents of level.dat in {@code root}
     *
     * @param root    The contents of level.dat
     * @param profile The active migration profile
     * @return true, if the run did change the contents of root
     */
    abstract protected Boolean runLevelDatPatch(CompoundTag root, MigrationProfile profile);
}

