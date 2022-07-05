package org.betterx.bclib.api.v2.datafixer;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.interfaces.PatchBiFunction;
import org.betterx.bclib.interfaces.PatchFunction;
import org.betterx.worlds.together.util.ModUtil;
import org.betterx.worlds.together.world.WorldConfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class MigrationProfile {
    final Set<String> mods;
    final Map<String, String> idReplacements;
    final List<PatchFunction<CompoundTag, Boolean>> levelPatchers;
    final List<PatchBiFunction<ListTag, ListTag, Boolean>> statePatchers;
    final List<Patch> worldDataPatchers;
    final Map<String, List<String>> worldDataIDPaths;

    private final CompoundTag config;
    private CompoundTag level;
    private File levelBaseDir;
    private boolean prePatchChangedLevelDat;
    private boolean didRunPrePatch;
    private Exception prePatchException;

    MigrationProfile(CompoundTag config, boolean applyAll) {
        this.config = config;

        this.mods = Collections.unmodifiableSet(Patch.getALL()
                                                     .stream()
                                                     .map(p -> p.modID)
                                                     .collect(Collectors.toSet()));

        HashMap<String, String> replacements = new HashMap<String, String>();
        List<PatchFunction<CompoundTag, Boolean>> levelPatches = new LinkedList<>();
        List<Patch> worldDataPatches = new LinkedList<>();
        List<PatchBiFunction<ListTag, ListTag, Boolean>> statePatches = new LinkedList<>();
        HashMap<String, List<String>> worldDataIDPaths = new HashMap<>();
        for (String modID : mods) {

            Patch.getALL()
                 .stream()
                 .filter(p -> p.modID.equals(modID))
                 .forEach(patch -> {
                     List<String> paths = patch.getWorldDataIDPaths();
                     if (paths != null) worldDataIDPaths.put(modID, paths);

                     if (applyAll || currentPatchLevel(modID) < patch.level || patch.alwaysApply) {
                         replacements.putAll(patch.getIDReplacements());
                         if (patch.getLevelDatPatcher() != null)
                             levelPatches.add(patch.getLevelDatPatcher());
                         if (patch.getWorldDataPatcher() != null)
                             worldDataPatches.add(patch);
                         if (patch.getBlockStatePatcher() != null)
                             statePatches.add(patch.getBlockStatePatcher());
                         DataFixerAPI.LOGGER.info("Applying " + patch);
                     } else {
                         DataFixerAPI.LOGGER.info("Ignoring " + patch);
                     }
                 });
        }

        this.worldDataIDPaths = Collections.unmodifiableMap(worldDataIDPaths);
        this.idReplacements = Collections.unmodifiableMap(replacements);
        this.levelPatchers = Collections.unmodifiableList(levelPatches);
        this.worldDataPatchers = Collections.unmodifiableList(worldDataPatches);
        this.statePatchers = Collections.unmodifiableList(statePatches);
    }

    /**
     * This method is supposed to be used by developers to apply id-patches to custom nbt structures. It is only
     * available in Developer-Mode
     */
    public static void fixCustomFolder(File dir) {
        if (!BCLib.isDevEnvironment()) return;
        MigrationProfile profile = Patch.createMigrationData();
        List<File> nbts = getAllNbts(dir, null);
        nbts.parallelStream().forEach((file) -> {
            DataFixerAPI.LOGGER.info("Loading NBT " + file);
            try {
                CompoundTag root = NbtIo.readCompressed(file);
                boolean[] changed = {false};
                int spawnerIdx = -1;
                if (root.contains("palette")) {
                    ListTag items = root.getList("palette", Tag.TAG_COMPOUND);
                    for (int idx = 0; idx < items.size(); idx++) {
                        final CompoundTag tag = (CompoundTag) items.get(idx);
                        if (tag.contains("Name") && tag.getString("Name").equals("minecraft:spawner"))
                            spawnerIdx = idx;
                        if (tag.contains("Name") && (tag.getString("Name").equals("minecraft:") || tag.getString("Name")
                                                                                                      .equals(""))) {
                            System.out.println("Empty Name");
                        }
                        if (tag.contains("id") && (tag.getString("id").equals("minecraft:") || tag.getString("id")
                                                                                                  .equals(""))) {
                            System.out.println("Empty ID");
                        }
                        changed[0] |= profile.replaceStringFromIDs(tag, "Name");
                    }
                }

                if (spawnerIdx >= 0 && root.contains("blocks")) {
                    ListTag items = root.getList("blocks", Tag.TAG_COMPOUND);
                    for (int idx = 0; idx < items.size(); idx++) {
                        final CompoundTag blockTag = (CompoundTag) items.get(idx);
                        if (blockTag.contains("state") && blockTag.getInt("state") == spawnerIdx && blockTag.contains(
                                "nbt")) {
                            CompoundTag nbt = blockTag.getCompound("nbt");
                            if (nbt.contains("SpawnData")) {
                                final CompoundTag entity = nbt.getCompound("SpawnData");
                                if (!entity.contains("entity")) {
                                    CompoundTag data = new CompoundTag();
                                    data.put("entity", entity);
                                    nbt.put("SpawnData", data);

                                    changed[0] = true;
                                }
                            }
                            if (nbt.contains("SpawnPotentials")) {
                                ListTag pots = nbt.getList("SpawnPotentials", Tag.TAG_COMPOUND);
                                for (Tag potItemIn : pots) {
                                    final CompoundTag potItem = (CompoundTag) potItemIn;
                                    if (potItem.contains("Weight")) {
                                        int weight = potItem.getInt("Weight");
                                        potItem.putInt("weight", weight);
                                        potItem.remove("Weight");

                                        changed[0] = true;
                                    }

                                    if (potItem.contains("Entity")) {
                                        CompoundTag entity = potItem.getCompound("Entity");
                                        CompoundTag data = new CompoundTag();
                                        data.put("entity", entity);

                                        potItem.put("data", data);
                                        potItem.remove("Entity");

                                        changed[0] = true;
                                    }
                                }
                            }
                        }
                    }
                }

                if (changed[0]) {
                    DataFixerAPI.LOGGER.info("Writing NBT " + file);
                    NbtIo.writeCompressed(root, file);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static List<File> getAllNbts(File dir, List<File> list) {
        if (list == null) {
            list = new ArrayList<>();
        }
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                getAllNbts(file, list);
            } else if (file.isFile() && file.getName().endsWith(".nbt")) {
                list.add(file);
            }
        }
        return list;
    }

    final public CompoundTag getLevelDat(File levelBaseDir) {
        if (level == null || this.levelBaseDir == null || !this.levelBaseDir.equals(levelBaseDir)) {
            runPrePatches(levelBaseDir);
        }
        return level;
    }

    final public boolean isLevelDatChanged() {
        return prePatchChangedLevelDat;
    }

    final public File getLevelDatFile() {
        return new File(levelBaseDir, "level.dat");
    }

    final public Exception getPrePatchException() {
        return prePatchException;
    }


    final public void runPrePatches(File levelBaseDir) {
        if (didRunPrePatch) {
            BCLib.LOGGER.warning("Already did run PrePatches for " + this.levelBaseDir + ".");
        }
        BCLib.LOGGER.info("Running Pre Patchers on " + levelBaseDir);

        this.levelBaseDir = levelBaseDir;
        this.level = null;
        this.prePatchException = null;
        didRunPrePatch = true;

        this.prePatchChangedLevelDat = runPreLevelPatches(getLevelDatFile());
    }

    private boolean runPreLevelPatches(File levelDat) {
        try {
            level = NbtIo.readCompressed(levelDat);

            boolean changed = patchLevelDat(level);
            return changed;
        } catch (IOException | PatchDidiFailException e) {
            prePatchException = e;
            return false;
        }
    }

    final public void markApplied() {
        for (String modID : mods) {
            DataFixerAPI.LOGGER.info(
                    "Updating Patch-Level for '{}' from {} to {}",
                    modID,
                    ModUtil.convertModVersion(currentPatchLevel(modID)),
                    ModUtil.convertModVersion(Patch.maxPatchLevel(modID))
            );
            if (config != null)
                config.putString(modID, Patch.maxPatchVersion(modID));
        }
    }

    public String currentPatchVersion(@NotNull String modID) {
        if (config == null || !config.contains(modID)) return "0.0.0";
        return config.getString(modID);
    }

    public int currentPatchLevel(@NotNull String modID) {
        return ModUtil.convertModVersion(currentPatchVersion(modID));
    }

    public boolean hasAnyFixes() {
        boolean hasLevelDatPatches;
        if (didRunPrePatch != false) {
            hasLevelDatPatches = prePatchChangedLevelDat;
        } else {
            hasLevelDatPatches = levelPatchers.size() > 0;
        }

        return idReplacements.size() > 0 || hasLevelDatPatches || worldDataPatchers.size() > 0;
    }

    public String replaceStringFromIDs(@NotNull String val) {
        final String replace = idReplacements.get(val);
        return replace;
    }

    public boolean replaceStringFromIDs(@NotNull CompoundTag tag, @NotNull String key) {
        if (!tag.contains(key)) return false;

        final String val = tag.getString(key);
        final String replace = idReplacements.get(val);

        if (replace != null) {
            DataFixerAPI.LOGGER.warning("Replacing ID '{}' with '{}'.", val, replace);
            tag.putString(key, replace);
            return true;
        }

        return false;
    }

    private boolean replaceIDatPath(@NotNull ListTag list, @NotNull String[] parts, int level) {
        boolean[] changed = {false};
        if (level == parts.length - 1) {
            DataFixerAPI.fixItemArrayWithID(list, changed, this, true);
        } else {
            list.forEach(inTag -> changed[0] |= replaceIDatPath((CompoundTag) inTag, parts, level + 1));
        }
        return changed[0];
    }

    private boolean replaceIDatPath(@NotNull CompoundTag tag, @NotNull String[] parts, int level) {
        boolean changed = false;
        for (int i = level; i < parts.length - 1; i++) {
            final String part = parts[i];
            if (tag.contains(part)) {
                final byte type = tag.getTagType(part);
                if (type == Tag.TAG_LIST) {
                    ListTag list = tag.getList(part, Tag.TAG_COMPOUND);
                    return replaceIDatPath(list, parts, i);
                } else if (type == Tag.TAG_COMPOUND) {
                    tag = tag.getCompound(part);
                }
            } else {
                return false;
            }
        }

        if (tag != null && parts.length > 0) {
            final String key = parts[parts.length - 1];
            final byte type = tag.getTagType(key);
            if (type == Tag.TAG_LIST) {
                final ListTag list = tag.getList(key, Tag.TAG_COMPOUND);
                final boolean[] _changed = {false};
                if (list.size() == 0) {
                    _changed[0] = DataFixerAPI.fixStringIDList(tag, key, this);
                } else {
                    DataFixerAPI.fixItemArrayWithID(list, _changed, this, true);
                }
                return _changed[0];
            } else if (type == Tag.TAG_STRING) {
                return replaceStringFromIDs(tag, key);
            } else if (type == Tag.TAG_COMPOUND) {
                final CompoundTag cTag = tag.getCompound(key);
                boolean[] _changed = {false};
                DataFixerAPI.fixID(cTag, _changed, this, true);
                return _changed[0];
            }
        }


        return false;
    }

    public boolean replaceIDatPath(@NotNull CompoundTag root, @NotNull String path) {
        String[] parts = path.split("\\.");
        return replaceIDatPath(root, parts, 0);
    }

    public boolean patchLevelDat(@NotNull CompoundTag level) throws PatchDidiFailException {
        boolean changed = false;
        for (PatchFunction<CompoundTag, Boolean> f : levelPatchers) {
            changed |= f.apply(level, this);
        }
        return changed;
    }

    public void patchWorldData() throws PatchDidiFailException {
        for (Patch patch : worldDataPatchers) {
            CompoundTag root = WorldConfig.getRootTag(patch.modID);
            boolean changed = patch.getWorldDataPatcher().apply(root, this);
            if (changed) {
                WorldConfig.saveFile(patch.modID);
            }
        }

        for (Map.Entry<String, List<String>> entry : worldDataIDPaths.entrySet()) {
            CompoundTag root = WorldConfig.getRootTag(entry.getKey());
            boolean[] changed = {false};
            entry.getValue().forEach(path -> {
                changed[0] |= replaceIDatPath(root, path);
            });

            if (changed[0]) {
                WorldConfig.saveFile(entry.getKey());
            }
        }
    }

    public boolean patchBlockState(ListTag palette, ListTag states) throws PatchDidiFailException {
        boolean changed = false;
        for (PatchBiFunction<ListTag, ListTag, Boolean> f : statePatchers) {
            changed |= f.apply(palette, states, this);
        }
        return changed;
    }
}
