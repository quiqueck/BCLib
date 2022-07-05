package org.betterx.bclib.api.v2.datafixer;

import org.betterx.bclib.interfaces.PatchBiFunction;
import org.betterx.bclib.interfaces.PatchFunction;
import org.betterx.worlds.together.util.ModUtil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public abstract class Patch {
    private static final List<Patch> ALL = new ArrayList<>(10);

    /**
     * The Patch-Level derived from {@link #version}
     */
    public final int level;

    /**
     * The Patch-Version string
     */
    public final String version;

    /**
     * The Mod-ID that registered this Patch
     */

    @NotNull
    public final String modID;

    /**
     * This Mod is tested for each level start
     */
    public final boolean alwaysApply;

    static List<Patch> getALL() {
        return ALL;
    }

    /**
     * Returns the highest Patch-Version that is available for the given mod. If no patches were
     * registerd for the mod, this will return 0.0.0
     *
     * @param modID The ID of the mod you want to query
     * @return The highest Patch-Version that was found
     */
    public static String maxPatchVersion(@NotNull String modID) {
        return ALL.stream().filter(p -> p.modID.equals(modID)).map(p -> p.version).reduce((p, c) -> c).orElse("0.0.0");
    }

    /**
     * Returns the highest patch-level that is available for the given mod. If no patches were
     * registerd for the mod, this will return 0
     *
     * @param modID The ID of the mod you want to query
     * @return The highest Patch-Level that was found
     */
    public static int maxPatchLevel(@NotNull String modID) {
        return ALL.stream().filter(p -> p.modID.equals(modID)).mapToInt(p -> p.level).max().orElse(0);
    }

    /**
     * Called by inheriting classes.
     * <p>
     * Performs some sanity checks on the values and might throw a #RuntimeException if any
     * inconsistencies are found.
     *
     * @param modID   The ID of the Mod you want to register a patch for. This should be your
     *                ModID only. The ModID can not be {@code null} or an empty String.
     * @param version The mod-version that introduces the patch. This needs Semantic-Version String
     *                like x.x.x. Developers are responsible for registering their patches in the correct
     *                order (with increasing versions). You are not allowed to register a new
     *                Patch with a version lower or equal than
     *                {@link Patch#maxPatchVersion(String)}
     */
    protected Patch(@NotNull String modID, String version) {
        this(modID, version, false);
    }

    /**
     * Internal Constructor used to create patches that can allways run (no matter what patchlevel a level has)
     *
     * @param modID       The ID of the Mod
     * @param version     The mod-version that introduces the patch. When {@Code runAllways} is set, this version will
     *                    determine the patchlevel that is written to the level
     * @param alwaysApply When true, this patch is always active, no matter the patchlevel of the world.
     *                    This should be used sparingly and just for patches that apply to level.dat (as they only take
     *                    effect when changes are detected). Use {@link ForcedLevelPatch} to instatiate.
     */
    Patch(@NotNull String modID, String version, boolean alwaysApply) {
        //Patchlevels need to be unique and registered in ascending order
        if (modID == null || modID.isEmpty()) {
            throw new RuntimeException("[INTERNAL ERROR] Patches need a valid modID!");
        }

        if (version == null || version.isEmpty()) {
            throw new RuntimeException("Invalid Mod-Version");
        }

        this.version = version;
        this.alwaysApply = alwaysApply;
        this.level = ModUtil.convertModVersion(version);
        if (!ALL.stream().filter(p -> p.modID.equals(modID)).noneMatch(p -> p.level >= this.level) || this.level <= 0) {
            throw new RuntimeException(
                    "[INTERNAL ERROR] Patch-levels need to be created in ascending order beginning with 1.");
        }

        this.modID = modID;
    }

    @Override
    public String toString() {
        return "Patch{" + modID + ':' + version + ':' + level + '}';
    }


    /**
     * Return block data fixes. Fixes will be applied on world load if current patch-level for
     * the linked mod is lower than the {@link #level}.
     * <p>
     * The default implementation of this method returns an empty map.
     *
     * @return The returned Map should contain the replacements. All occurences of the
     * {@code KeySet} are replaced with the associated value.
     */
    public Map<String, String> getIDReplacements() {
        return new HashMap<String, String>();
    }

    /**
     * Return a {@link PatchFunction} that is called with the content of <i>level.dat</i>.
     * <p>
     * The function needs to return {@code true}, if changes were made to the data.
     * If an error occurs, the method should throw a {@link PatchDidiFailException}
     * <p>
     * The default implementation of this method returns null.
     *
     * @return {@code true} if changes were applied and we need to save the data
     */
    public PatchFunction<CompoundTag, Boolean> getLevelDatPatcher() {
        return null;
    }

    /**
     * Return a {@link PatchFunction} that is called with the content from the
     * {@link org.betterx.worlds.together.world.WorldConfig} for this Mod.
     * The function needs to return {@code true}, if changes were made to the data.
     * If an error occurs, the method should throw a {@link PatchDidiFailException}
     * <p>
     * The default implementation of this method returns null.
     *
     * @return {@code true} if changes were applied and we need to save the data
     */
    public PatchFunction<CompoundTag, Boolean> getWorldDataPatcher() {
        return null;
    }

    /**
     * Return a {@link PatchBiFunction} that is called with pallette and blockstate of
     * each chunk in every region. This method is called AFTER all ID replacements
     * from {@link #getIDReplacements()} were applied to the pallete.
     * <p>
     * The first parameter is the palette and the second is the blockstate.
     * <p>
     * The function needs to return {@code true}, if changes were made to the data.
     * If an error occurs, the method should throw a {@link PatchDidiFailException}
     * <p>
     * The default implementation of this method returns null.
     *
     * @return {@code true} if changes were applied and we need to save the data
     */
    public PatchBiFunction<ListTag, ListTag, Boolean> getBlockStatePatcher() {
        return null;
    }

    /**
     * Generates ready to use data for all currently registered patches. The list of
     * patches is selected by the current patch-level of the world.
     * <p>
     * A {@link #Patch} with a given {@link #level} is only included if the patch-level of the
     * world is less
     *
     * @param config The current patch-level configuration*
     * @return a new {@link MigrationProfile} Object.
     */
    static MigrationProfile createMigrationData(CompoundTag config) {
        return new MigrationProfile(config, false);
    }

    /**
     * This method is supposed to be used by developers to apply id-patches to custom nbt structures. It is only
     * available in Developer-Mode
     */
    static MigrationProfile createMigrationData() {
        return new MigrationProfile(null, true);
    }

    /**
     * Returns a list of paths where your mod stores IDs in your {@link org.betterx.worlds.together.world.WorldConfig}-File.
     * <p>
     * {@link DataFixerAPI} will use information from the latest patch that returns a non-null-result. This list is used
     * to automatically fix changed IDs from all active patches (see {@link Patch#getIDReplacements()}
     * <p>
     * The end of the path can either be a {@link net.minecraft.nbt.StringTag}, a {@link net.minecraft.nbt.ListTag} or
     * a {@link CompoundTag}. If the Path contains a non-leaf {@link net.minecraft.nbt.ListTag}, all members of that
     * list will be processed. For example:
     * <pre>
     * 	 - global +
     * 			  | - key (String)
     * 			  | - items (List) +
     * 							   | - { id (String) }
     * 							   | - { id (String) }
     * </pre>
     * The path <b>global.items.id</b> will fix all <i>id</i>-entries in the <i>items</i>-list, while the path
     * <b>global.key</b> will only fix the  <i>key</i>-entry.
     * <p>
     * if the leaf-entry (= the last part of the path, which would be <i>items</i> in <b>global.items</b>) is a
     * {@link CompoundTag}, the system will fix any <i>id</i> entry. If the {@link CompoundTag} contains an <i>item</i>
     * or <i>tag.BlockEntityTag</i> entry, the system will recursivley continue with those. If an <i>items</i>
     * or <i>inventory</i>-{@link net.minecraft.nbt.ListTag} was found, the system will continue recursivley with
     * every item of that list.
     * <p>
     * if the leaf-entry is a {@link net.minecraft.nbt.ListTag}, it is handle the same as a child <i>items</i> entry
     * of a {@link CompoundTag}.
     *
     * @return {@code null} if nothing changes or a list of Paths in your {@link org.betterx.worlds.together.world.WorldConfig}-File.
     * Paths are dot-seperated (see {@link org.betterx.worlds.together.world.WorldConfig#getCompoundTag(String, String)}).
     */
    public List<String> getWorldDataIDPaths() {
        return null;
    }
}
