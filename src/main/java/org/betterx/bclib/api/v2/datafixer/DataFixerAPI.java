package org.betterx.bclib.api.v2.datafixer;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.client.gui.screens.AtomicProgressListener;
import org.betterx.bclib.client.gui.screens.ConfirmFixScreen;
import org.betterx.bclib.client.gui.screens.LevelFixErrorScreen;
import org.betterx.bclib.client.gui.screens.LevelFixErrorScreen.Listener;
import org.betterx.bclib.client.gui.screens.ProgressScreen;
import org.betterx.bclib.config.Configs;
import de.ambertation.wover.core.api.Logger;
import de.ambertation.wover.state.api.WorldConfig;

import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.zip.ZipException;
import org.jetbrains.annotations.NotNull;

/**
 * API to manage Patches that need to get applied to a world
 */
public class DataFixerAPI {
    static final Logger LOGGER = Logger.create(BCLib.C);

    static class State {
        public boolean didFail = false;
        protected ArrayList<String> errors = new ArrayList<>();
        //Every rewrite is only logged at debug level, the summary reports these totals instead.
        //Files and regions are fixed in parallel streams, hence the atomics.
        final AtomicInteger rewrittenFiles = new AtomicInteger();
        final AtomicInteger rewrittenChunks = new AtomicInteger();

        public void addError(String s) {
            errors.add(s);
        }

        public boolean hasError() {
            return !errors.isEmpty();
        }

        public String getErrorMessage() {
            return errors.stream().reduce("", (a, b) -> a + "  - " + b + "\n");
        }

        public String[] getErrorMessages() {
            String[] res = new String[errors.size()];
            return errors.toArray(res);
        }
    }

    @FunctionalInterface
    public interface Callback {
        void call();
    }

    private static boolean wrapCall(
            LevelStorageSource levelSource,
            String levelID,
            Function<LevelStorageAccess, Boolean> runWithLevel
    ) {
        LevelStorageSource.LevelStorageAccess levelStorageAccess;
        try {
            levelStorageAccess = levelSource.createAccess(levelID);
        } catch (IOException e) {
            BCLib.LOGGER.warn("Failed to read level {} data", levelID, e);
            SystemToast.onWorldAccessFailure(Minecraft.getInstance(), levelID);
            Minecraft.getInstance().gui.setScreen(null);
            return true;
        }

        boolean returnValue = runWithLevel.apply(levelStorageAccess);

        try {
            levelStorageAccess.close();
        } catch (IOException e) {
            BCLib.LOGGER.warn("Failed to unlock access to level {}", levelID, e);
        }

        return returnValue;
    }

    /**
     * Will apply necessary Patches to the world.
     *
     * @param levelSource The SourceStorage for this Minecraft instance, You can get this using
     *                    {@code Minecraft.getInstance().getLevelSource()}
     * @param levelID     The ID of the Level you want to patch
     * @param showUI      {@code true}, if you want to present the user with a Screen that offers to backup the world
     *                    before applying the patches
     * @param onResume    When this method retursn {@code true}, this function will be called when the world is ready
     * @return {@code true} if the UI was displayed. The UI is only displayed if {@code showUI} was {@code true} and
     * patches were enabled in the config and the Guardian did find any patches that need to be applied to the world.
     */
    public static boolean fixData(
            LevelStorageSource levelSource,
            String levelID,
            boolean showUI,
            Consumer<Boolean> onResume
    ) {
        return wrapCall(levelSource, levelID, (levelStorageAccess) -> fixData(levelStorageAccess, showUI, onResume));
    }

    /**
     * Will apply necessary Patches to the world.
     *
     * @param levelStorageAccess The access class of the level you want to patch
     * @param showUI             {@code true}, if you want to present the user with a Screen that offers to backup the world
     *                           before applying the patches
     * @param onResume           When this method retursn {@code true}, this function will be called when the world is ready
     * @return {@code true} if the UI was displayed. The UI is only displayed if {@code showUI} was {@code true} and
     * patches were enabled in the config and the Guardian did find any patches that need to be applied to the world.
     */
    public static boolean fixData(
            LevelStorageSource.LevelStorageAccess levelStorageAccess,
            boolean showUI,
            Consumer<Boolean> onResume
    ) {
        File levelPath = levelStorageAccess.getLevelPath(LevelResource.ROOT).toFile();
        return fixData(levelPath, levelStorageAccess.getLevelId(), levelStorageAccess, showUI, onResume);
    }

    /**
     * Creates the patch level file for new worlds
     */
    public static void initializePatchData() {
        getMigrationProfile().markApplied();
        WorldConfig.saveFile(BCLib.C);
    }


    @Environment(EnvType.CLIENT)
    private static AtomicProgressListener showProgressScreen() {
        ProgressScreen ps = new ProgressScreen(
                Minecraft.getInstance().gui.screen(),
                Component.translatable("title.bclib.datafixer.progress"),
                Component.translatable("message.bclib.datafixer.progress")
        );
        Minecraft.getInstance().gui.setScreen(ps);
        return ps;
    }

    private static boolean makeBackupAndShowToast(
            LevelStorageSource.LevelStorageAccess access,
            String levelID
    ) {
        //Reuse the already opened access instead of opening a second one. Acquiring a second
        //LevelStorageAccess for a world that is already locked by this JVM would throw an
        //OverlappingFileLockException (a RuntimeException, not an IOException) and leave the
        //progress screen hanging forever.
        try {
            final LevelStorageSource.LevelStorageAccess accessToBackup = access != null
                    ? access
                    : Minecraft.getInstance().getLevelSource().createAccess(levelID);
            try {
                //EditWorldScreen#makeBackupAndShowToast opens a GenericWaitingScreen and calls
                //Minecraft#setScreenAndShow synchronously, which renders a frame immediately and
                //therefore requires the render thread. This method runs on the background
                //fixerThread, so calling it directly used to throw here; the exception was
                //swallowed by the catch below and reported as a generic "access failure" toast,
                //while execution still fell through to apply the fixes -- silently skipping the
                //backup the user asked for. We hop onto the main thread via the Minecraft
                //executor to make the call, then block this background thread on the resulting
                //future until the (now correctly scheduled) backup actually finishes.
                Boolean success = CompletableFuture
                        .supplyAsync(() -> accessToBackup, Minecraft.getInstance())
                        .thenCompose(EditWorldScreen::makeBackupAndShowToast)
                        .join();
                return Boolean.TRUE.equals(success);
            } finally {
                if (access == null) {
                    accessToBackup.close();
                }
            }
        } catch (Exception ex) {
            Minecraft.getInstance().execute(() -> SystemToast.onWorldAccessFailure(Minecraft.getInstance(), levelID));
            LOGGER.warn("Failed to create backup of level {}", levelID, ex);
            return false;
        }
    }

    private static boolean fixData(
            File dir,
            String levelID,
            LevelStorageSource.LevelStorageAccess access,
            boolean showUI,
            Consumer<Boolean> onResume
    ) {
        MigrationProfile profile = loadProfileIfNeeded(dir);

        BiConsumer<Boolean, Boolean> runFixes = (createBackup, applyFixes) -> {
            final AtomicProgressListener progress;
            if (applyFixes) {
                if (showUI) {
                    progress = showProgressScreen();
                } else {
                    progress = new AtomicProgressListener() {
                        private long timeStamp = Util.getMillis();
                        private AtomicInteger counter = new AtomicInteger(0);

                        @Override
                        public void incAtomic(int maxProgress) {
                            int percentage = (100 * counter.incrementAndGet()) / maxProgress;
                            if (Util.getMillis() - this.timeStamp >= 1000L) {
                                this.timeStamp = Util.getMillis();
                                BCLib.LOGGER.info("Patching... {}%", percentage);
                            }
                        }

                        @Override
                        public void resetAtomic() {
                            counter = new AtomicInteger(0);
                        }

                        public void stop() {
                        }

                        public void progressStage(Component component) {
                            BCLib.LOGGER.info("Patcher Stage... {}%", component.getString());
                        }
                    };
                }
            } else {
                progress = null;
            }

            Supplier<State> runner = () -> {
                //Never let an exception (IOException OR RuntimeException) escape the runner. If it
                //did, the fixerThread would die before scheduling Minecraft.execute(...) and the
                //progress screen would hang at 0% forever. Route any failure to the State error
                //mechanism so the world always resumes (or shows the LevelFixErrorScreen).
                State state = new State();
                try {
                    if (createBackup) {
                        if (progress != null) {
                            progress.progressStage(Component.translatable("message.bclib.datafixer.progress.waitbackup"));
                        }
                        if (!makeBackupAndShowToast(access, levelID)) {
                            //The user explicitly asked for a backup before patching. If it
                            //failed, do not silently fall through and patch the world anyway --
                            //report the failure and stop here instead.
                            state.didFail = true;
                            state.addError("Failed to create a backup of the world (" + levelID + "). Fixes were not applied.");
                            return state;
                        }
                    }

                    if (applyFixes) {
                        return runDataFixes(levelID, dir, profile, progress);
                    }
                } catch (Throwable t) {
                    BCLib.LOGGER.error("Unexpected error while fixing level " + levelID + ": " + t);
                    t.printStackTrace();
                    state.didFail = true;
                    state.addError("Unexpected error while fixing world (" + t.getMessage() + ")");
                }

                return state;
            };

            if (showUI) {
                Thread fixerThread = new Thread(() -> {
                    final State state = runner.get();

                    Minecraft.getInstance()
                             .execute(() -> {
                                 if (profile != null && showUI) {
                                     //something went wrong, show the user our error
                                     if (state.didFail || state.hasError()) {
                                         showLevelFixErrorScreen(
                                                 state, (markFixed) -> {
                                                     if (markFixed) {
                                                         profile.markApplied();
                                                     }
                                                     onResume.accept(applyFixes);
                                                 }
                                         );
                                     } else {
                                         onResume.accept(applyFixes);
                                     }
                                 }
                             });

                });
                fixerThread.start();
            } else {
                State state = runner.get();
                if (state.hasError()) {
                    LOGGER.error("There were Errors while fixing the Level:");
                    LOGGER.error(state.getErrorMessage());
                }
            }
        };

        //we have some migrations
        if (profile != null) {
            //display the confirm UI.
            if (showUI) {
                showBackupWarning(levelID, runFixes);
                return true;
            } else {
                BCLib.LOGGER.warn("Applying Fixes on Level");
                runFixes.accept(false, true);
            }
        }
        return false;
    }

    @Environment(EnvType.CLIENT)
    private static void showLevelFixErrorScreen(State state, Listener onContinue) {
        // 26.3 moved screen management off Minecraft onto Minecraft#gui: #setScreen is now
        // Gui#setScreen and the former Minecraft#screen field is exposed as Gui#screen().
        Minecraft.getInstance()
                 .gui.setScreen(new LevelFixErrorScreen(
                         Minecraft.getInstance().gui.screen(),
                         state.getErrorMessages(),
                         onContinue
                 ));
    }

    private static MigrationProfile loadProfileIfNeeded(File levelBaseDir) {
        if (!Configs.MAIN_CONFIG.applyPatches()) {
            LOGGER.info("World Patches are disabled");
            return null;
        }

        //A dedicated server fires WORLD_FOLDER_READY before it creates a new world, so the folder has no
        //level.dat yet. There is nothing to patch, and CREATED_NEW_WORLD_FOLDER marks every patch as
        //applied right after. Running the fixer here used to fail on the missing level.dat and log it
        //as an error on every fresh server.
        if (!new File(levelBaseDir, "level.dat").exists() && !new File(levelBaseDir, "level.dat_old").exists()) {
            LOGGER.info("No level.dat in {}, new world, nothing to patch", levelBaseDir);
            return null;
        }

        MigrationProfile profile = getMigrationProfile();
        profile.runPrePatches(levelBaseDir);

        if (!profile.hasAnyFixes()) {
            LOGGER.info("Everything up to date");
            return null;
        }

        return profile;
    }

    @NotNull
    private static MigrationProfile getMigrationProfile() {
        final CompoundTag patchConfig = WorldConfig.getCompoundTag(BCLib.C, Configs.MAIN_PATCH_CATEGORY);
        MigrationProfile profile = Patch.createMigrationData(patchConfig);
        return profile;
    }

    @Environment(EnvType.CLIENT)
    static void showBackupWarning(String levelID, BiConsumer<Boolean, Boolean> whenFinished) {
        Minecraft.getInstance().gui.setScreen(new ConfirmFixScreen(null, whenFinished::accept));
    }

    private static State runDataFixes(
            String levelID,
            File dir,
            MigrationProfile profile,
            AtomicProgressListener progress
    ) {
        State state = new State();
        progress.resetAtomic();

        progress.progressStage(Component.translatable("message.bclib.datafixer.progress.reading"));
        List<File> players = getAllPlayers(dir);
        List<File> backpacks = getAllBackpacks(dir);
        List<File> regions = getAllRegions(dir, null);
        final int maxProgress = players.size() + backpacks.size() + regions.size() + 4;
        progress.incAtomic(maxProgress);
        //The individual files are only logged at debug level. A large (pre-generated) world has
        //thousands of region files, and the logger name is not part of the production log layout,
        //so the summary carries an explicit prefix to make clear who is reading the region files.
        final long startTime = Util.getMillis();
        LOGGER.info(
                "[BCLib] Inspecting {} player, {} backpack and {} region files in {} for world patches",
                players.size(), backpacks.size(), regions.size(), dir
        );

        progress.progressStage(Component.translatable("message.bclib.datafixer.progress.players"));
        RegionStorageInfo regionStorageInfo = new RegionStorageInfo(
                levelID,
                ResourceKey.create(Registries.DIMENSION, BCLib.makeID("world_fixer")),
                "mca"
        );
        players.parallelStream().forEach((file) -> {
            fixPlayer(profile, state, file.toPath(), regionStorageInfo);
            progress.incAtomic(maxProgress);
        });

        backpacks.parallelStream().forEach((file) -> {
            fixBackpack(profile, state, file.toPath());
            progress.incAtomic(maxProgress);
        });

        progress.progressStage(Component.translatable("message.bclib.datafixer.progress.level"));
        fixLevel(profile, state, dir, regionStorageInfo);
        progress.incAtomic(maxProgress);

        progress.progressStage(Component.translatable("message.bclib.datafixer.progress.worlddata"));
        try {
            profile.patchWorldData();
        } catch (PatchDidiFailException e) {
            state.didFail = true;
            state.addError("Failed fixing worldconfig (" + e.getMessage() + ")");
            BCLib.LOGGER.error(e.getMessage());
        }
        progress.incAtomic(maxProgress);

        progress.progressStage(Component.translatable("message.bclib.datafixer.progress.regions"));
        regions.parallelStream().forEach((file) -> {
            fixRegion(profile, state, file, regionStorageInfo);
            progress.incAtomic(maxProgress);
        });

        if (!state.didFail) {
            progress.progressStage(Component.translatable("message.bclib.datafixer.progress.saving"));
            profile.markApplied();
            WorldConfig.saveFile(BCLib.C);
        }
        progress.incAtomic(maxProgress);
        LOGGER.info(
                "[BCLib] Finished inspecting {} files in {}s, rewrote {} chunks and {} files{}",
                players.size() + backpacks.size() + regions.size() + 1,
                (Util.getMillis() - startTime) / 1000,
                state.rewrittenChunks.get(),
                state.rewrittenFiles.get(),
                state.didFail ? " with errors, the world patches will be retried on the next start" : ""
        );

        progress.stop();

        return state;
    }

    private static void fixLevel(
            MigrationProfile profile,
            State state,
            File levelBaseDir,
            RegionStorageInfo regionStorageInfo
    ) {
        try {
            LOGGER.debug("Inspecting level.dat in {}", levelBaseDir);

            //load the level (could already contain patches applied by patchLevelDat)
            CompoundTag level = profile.getLevelDat(levelBaseDir);
            boolean[] changed = {profile.isLevelDatChanged()};

            if (profile.getPrePatchException() != null) {
                throw profile.getPrePatchException();
            }

            if (level.contains("Data")) {
                CompoundTag dataTag = (CompoundTag) level.get("Data");
                if (dataTag.contains("Player")) {
                    CompoundTag player = (CompoundTag) dataTag.get("Player");
                    fixPlayerNbt(player, changed, profile);
                }
            }

            if (changed[0]) {
                LOGGER.debug("Writing '{}'", profile.getLevelDatPath());
                state.rewrittenFiles.incrementAndGet();
                NbtIo.writeCompressed(level, profile.getLevelDatPath());
            }
        } catch (Exception e) {
            BCLib.LOGGER.error("Failed fixing Level-Data.");
            state.addError("Failed fixing Level-Data in level.dat (" + e.getMessage() + ")");
            state.didFail = true;
            e.printStackTrace();
        }
    }

    private static void fixPlayer(MigrationProfile data, State state, Path file, RegionStorageInfo regionStorageInfo) {
        try {
            LOGGER.debug("Inspecting {}", file);

            CompoundTag player = readNbt(file);
            boolean[] changed = {false};
            fixPlayerNbt(player, changed, data);

            if (changed[0]) {
                LOGGER.debug("Writing '{}'", file);
                state.rewrittenFiles.incrementAndGet();
                NbtIo.writeCompressed(player, file);
            }
        } catch (Exception e) {
            BCLib.LOGGER.error("Failed fixing Player-Data.");
            state.addError("Failed fixing Player-Data in " + file.getFileName() + " (" + e.getMessage() + ")");
            state.didFail = true;
            e.printStackTrace();
        }
    }

    private static void fixPlayerNbt(CompoundTag player, boolean[] changed, MigrationProfile data) {
        //Checking Inventory
        player
                .getList("Inventory")
                .ifPresent(inventory -> fixItemArrayWithID(inventory, changed, data, true));


        //Checking EnderChest
        player
                .getList("EnderItems")
                .ifPresent(enderItems -> fixItemArrayWithID(enderItems, changed, data, true));

        ;

        //Checking ReceipBook
        if (player.contains("recipeBook")) {
            player.getCompound("recipeBook").ifPresent(
                    recipeBook -> {
                        changed[0] |= fixStringIDList(recipeBook, "recipes", data);
                        changed[0] |= fixStringIDList(recipeBook, "toBeDisplayed", data);
                    }
            );
        }
    }

    static boolean fixStringIDList(CompoundTag root, String name, MigrationProfile data) {
        boolean _changed = false;
        if (root.contains(name)) {
            ListTag items = root.getList(name).orElse(new ListTag());
            ListTag newItems = new ListTag();

            for (Tag tag : items) {
                final StringTag str = (StringTag) tag;
                final String replace = data.replaceStringFromIDs(str.value());
                if (replace != null) {
                    _changed = true;
                    newItems.add(StringTag.valueOf(replace));
                } else {
                    newItems.add(tag);
                }
            }
            if (_changed) {
                root.put(name, newItems);
            }
        }
        return _changed;
    }

    /**
     * Replaces outdated IDs at the given keys anywhere below {@code tag}.
     * <p>
     * Used where the shape of the data is not known up front: chunk {@code structures} nest their
     * piece types ({@code id}) and jigsaw pool element types ({@code element_type}) at a depth that
     * differs per structure type, and third-party container files lay their inventories out however
     * their owning mod sees fit. Values that are not part of the replacement map are left
     * untouched, so walking everything is safe.
     *
     * @param tag     The tag to inspect
     * @param changed Set to {@code true} if anything was replaced
     * @param keys    The keys whose string values hold a registry ID
     */
    private static void fixIDsRecursively(Tag tag, boolean[] changed, MigrationProfile data, String... keys) {
        if (tag instanceof CompoundTag compound) {
            for (String key : keys) {
                changed[0] |= data.replaceStringFromIDs(compound, key);
            }
            for (String key : compound.keySet()) {
                fixIDsRecursively(compound.get(key), changed, data, keys);
            }
        } else if (tag instanceof ListTag list) {
            list.forEach(entry -> fixIDsRecursively(entry, changed, data, keys));
        }
    }

    private static List<File> getAllBackpacks(File dir) {
        return collectBackpacks(new File(dir, "backpacks"), new ArrayList<>());
    }

    /**
     * Collects the backpack files below {@code dir}, recursing into sub-directories: the storage is
     * laid out as {@code backpacks/<player-uuid>/<backpack-uuid>.dat}, so a flat listing finds
     * nothing but directories.
     */
    private static List<File> collectBackpacks(File dir, List<File> list) {
        if (!dir.exists() || !dir.isDirectory()) {
            return list;
        }
        final File[] files = dir.listFiles();
        if (files == null) {
            return list;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                collectBackpacks(file, list);
            } else if (file.isFile() && file.getName().endsWith(".dat")) {
                list.add(file);
            }
        }
        return list;
    }

    /**
     * Fixes the IDs in one backpack file.
     * <p>
     * Mods that give players extra storage (Traveler's Backpack, Bag of Holding, ...) keep it in
     * {@code <world>/backpacks/&lt;uuid&gt;.dat} rather than in the player file, so it is missed by
     * both {@link #getAllPlayers(File)} and {@link #getAllRegions(File, List)} - the latter only
     * collects {@code .mca}. Item stacks stored in there kept their outdated IDs and were dropped
     * on load. The layout is the owning mod's own, so every {@code id} below the root is
     * considered.
     */
    private static void fixBackpack(MigrationProfile data, State state, Path file) {
        try {
            LOGGER.debug("Inspecting {}", file);

            CompoundTag root = readNbt(file);
            boolean[] changed = {false};
            fixIDsRecursively(root, changed, data, "id");

            if (changed[0]) {
                LOGGER.debug("Writing '{}'", file);
                state.rewrittenFiles.incrementAndGet();
                NbtIo.writeCompressed(root, file);
            }
        } catch (Exception e) {
            BCLib.LOGGER.error("Failed fixing Backpack-Data.");
            state.addError("Failed fixing Backpack-Data in " + file.getFileName() + " (" + e.getMessage() + ")");
            state.didFail = true;
            e.printStackTrace();
        }
    }

    /**
     * Applies the ID replacements and all registered {@link Patch#getBlockStatePatcher()}s to a
     * single section palette.
     *
     * @param palette The block palette of the section, or {@code null} if the section has none
     * @param states  The packed block states belonging to the palette. Chunks from 1.18 onwards
     *                store these as a long-array instead of a list, in which case an empty list
     *                is passed and patchers only get to inspect the palette.
     */
    private static void fixPalette(
            ListTag palette,
            ListTag states,
            ChunkPos pos,
            boolean[] changed,
            MigrationProfile data,
            State state
    ) {
        if (palette == null) return;

        // Minecraft 26.3 (data version 5006, BlockStateFieldNamesFix) renamed the block state fields to
        // {id, properties} and writes a state without properties as a bare id string. The id replacements
        // and every registered state patcher are written against the legacy {Name, Properties} compound,
        // so chunks saved by 26.3 are converted to that shape for the duration of the fix and converted
        // back afterwards. Older chunks keep the legacy shape: vanilla's datafixer renames them on load.
        final boolean modernFormat = toLegacyPalette(palette);

        palette.forEach((blockTag) -> {
            CompoundTag blockTagCompound = ((CompoundTag) blockTag);
            changed[0] |= data.replaceStringFromIDs(blockTagCompound, "Name");
        });

        try {
            changed[0] |= data.patchBlockState(palette, states);
        } catch (PatchDidiFailException e) {
            BCLib.LOGGER.error("Failed fixing BlockState in " + pos);
            state.addError("Failed fixing BlockState in " + pos + " (" + e.getMessage() + ")");
            state.didFail = true;
            changed[0] = false;
            e.printStackTrace();
        }

        if (modernFormat) toModernPalette(palette);
    }

    /**
     * Rewrites 26.3-format palette entries (a bare id string, or {@code {id, properties}}) into the legacy
     * {@code {Name, Properties}} compound in place.
     *
     * @return {@code true} if any entry was in the 26.3 format
     */
    private static boolean toLegacyPalette(ListTag palette) {
        boolean modern = false;
        for (int i = 0; i < palette.size(); i++) {
            final Tag entry = palette.get(i);
            if (entry instanceof StringTag(String id)) {
                final CompoundTag legacy = new CompoundTag();
                legacy.putString("Name", id);
                palette.set(i, legacy);
                modern = true;
            } else if (entry instanceof CompoundTag compound && !compound.contains("Name") && compound.contains("id")) {
                final CompoundTag legacy = new CompoundTag();
                legacy.putString("Name", compound.getStringOr("id", ""));
                compound.getCompound("properties").ifPresent(props -> legacy.put("Properties", props));
                palette.set(i, legacy);
                modern = true;
            }
        }
        return modern;
    }

    /**
     * Inverse of {@link #toLegacyPalette(ListTag)}: writes every entry back in the 26.3 format, as a bare id
     * string when the state has no properties.
     */
    private static void toModernPalette(ListTag palette) {
        for (int i = 0; i < palette.size(); i++) {
            if (!(palette.get(i) instanceof CompoundTag legacy) || !legacy.contains("Name")) continue;
            final String id = legacy.getStringOr("Name", "");
            final CompoundTag props = legacy.getCompound("Properties").orElse(null);
            if (props == null || props.isEmpty()) {
                palette.set(i, StringTag.valueOf(id));
            } else {
                final CompoundTag modern = new CompoundTag();
                modern.putString("id", id);
                modern.put("properties", props);
                palette.set(i, modern);
            }
        }
    }

    private static void fixRegion(MigrationProfile data, State state, File file, RegionStorageInfo regionStorageInfo) {
        try {
            Path path = file.toPath();
            LOGGER.debug("Inspecting {}", path);
            boolean[] changed = new boolean[1];
            int rewrittenChunks = 0;
            RegionFile region = new RegionFile(regionStorageInfo, path, path.getParent(), true);

            for (int x = 0; x < 32; x++) {
                for (int z = 0; z < 32; z++) {
                    ChunkPos pos = new ChunkPos(x, z);
                    changed[0] = false;
                    if (region.hasChunk(pos) && !state.didFail) {
                        DataInputStream input = region.getChunkDataInputStream(pos);
                        //A chunk can be allocated in the header but still be unreadable (truncated,
                        //missing stream, ...). RegionFile already logged why, and vanilla treats it
                        //as a missing chunk, so there is nothing for us to fix. Failing the region
                        //instead meant the patch level was never saved and a dedicated server
                        //re-inspected the entire world on every start.
                        if (input == null) continue;
                        CompoundTag root = NbtIo.read(input);
                        // if ((root.toString().contains("betternether:chest") || root.toString().contains("bclib:chest"))) {
                        //   NbtIo.write(root, new File(file.toString() + "-" + x + "-" + z + ".nbt"));
                        // }
                        input.close();

                        // Pre-1.18 chunks nest their content below a "Level" compound and use
                        // capitalized keys. Since 1.18 the chunk root is flat and the keys are
                        // lower-case. Both layouts are handled here, so worlds that were never
                        // opened by a modern version still get fixed.
                        final CompoundTag legacy = root.getCompound("Level").orElse(null);

                        //Checking BlockEntities
                        if (legacy != null) {
                            legacy.getList("TileEntities")
                                  .ifPresent(tileEntities ->
                                          fixItemArrayWithID(tileEntities, changed, data, true));
                        }
                        root.getList("block_entities")
                            .ifPresent(blockEntities ->
                                    fixItemArrayWithID(blockEntities, changed, data, true));

                        //Checking Entities ("Entities" in entity-region files, "entities" in chunks)
                        root.getList("Entities")
                            .ifPresent(entities ->
                                    fixItemArrayWithID(entities, changed, data, true)
                            );
                        root.getList("entities")
                            .ifPresent(entities ->
                                    fixItemArrayWithID(entities, changed, data, true)
                            );

                        //Checking Block Palette
                        if (legacy != null) {
                            legacy.getList("Sections").ifPresent(sections -> sections.forEach((tag) -> {
                                final CompoundTag section = (CompoundTag) tag;
                                fixPalette(
                                        section.getList("Palette").orElse(null),
                                        section.getList("BlockStates").orElse(new ListTag()),
                                        pos, changed, data, state
                                );
                            }));
                        }
                        //Checking Structure starts and their (pool-)piece types
                        if (legacy != null) {
                            legacy.getCompound("Structures")
                                  .ifPresent(structures -> fixIDsRecursively(structures, changed, data, "id", "element_type"));
                        }
                        root.getCompound("structures")
                            .ifPresent(structures -> fixIDsRecursively(structures, changed, data, "id", "element_type"));

                        root.getList("sections").ifPresent(sections -> sections.forEach((tag) -> {
                            final CompoundTag blockStates = ((CompoundTag) tag)
                                    .getCompound("block_states")
                                    .orElse(null);
                            if (blockStates == null) return;

                            // Modern chunks store the packed indices as a long-array rather than a
                            // list, so the state patchers only receive the palette itself.
                            fixPalette(
                                    blockStates.getList("palette").orElse(null),
                                    new ListTag(),
                                    pos, changed, data, state
                            );
                        }));

                        //Whole-chunk patches run last, so they see the already renamed palette
                        try {
                            changed[0] |= data.patchChunk(root);
                        } catch (PatchDidiFailException e) {
                            BCLib.LOGGER.error("Failed patching chunk " + pos);
                            state.addError("Failed patching chunk " + pos + " (" + e.getMessage() + ")");
                            state.didFail = true;
                            changed[0] = false;
                            e.printStackTrace();
                        }

                        if (changed[0]) {
                            LOGGER.debug("Writing '{}': {}/{}", file, x, z);
                            rewrittenChunks++;
                            // NbtIo.write(root, new File(file.toString() + "-" + x + "-" + z + "-changed.nbt"));
                            DataOutputStream output = region.getChunkDataOutputStream(pos);
                            NbtIo.write(root, output);
                            output.close();
                        }
                    }
                }
            }
            region.close();
            if (rewrittenChunks > 0) {
                state.rewrittenFiles.incrementAndGet();
                state.rewrittenChunks.addAndGet(rewrittenChunks);
            }
        } catch (Exception e) {
            BCLib.LOGGER.error("Failed fixing Region.");
            state.addError("Failed fixing Region in " + file.getName() + " (" + e.getMessage() + ")");
            state.didFail = true;
            e.printStackTrace();
        }
    }

    static CompoundTag patchConfTag = null;

    static CompoundTag getPatchData() {
        if (patchConfTag == null) {
            patchConfTag = WorldConfig.getCompoundTag(BCLib.C, Configs.MAIN_PATCH_CATEGORY);
        }
        return patchConfTag;
    }

    static void fixItemArrayWithID(ListTag items, boolean[] changed, MigrationProfile data, boolean recursive) {
        items.forEach(inTag -> {
            fixID((CompoundTag) inTag, changed, data, recursive);
        });
    }


    static void fixID(CompoundTag inTag, boolean[] changed, MigrationProfile data, boolean recursive) {
        final CompoundTag tag = inTag;

        changed[0] |= data.replaceStringFromIDs(tag, "id");
        if (tag.contains("Item")) {
            CompoundTag item = (CompoundTag) tag.get("Item");
            fixID(item, changed, data, recursive);
        }

        if (recursive) {
            tag.getList("Items")
               .ifPresent(items -> fixItemArrayWithID(items, changed, data, true));
        }
        if (recursive) {
            tag.getList("Inventory")
               .ifPresent(inventory -> fixItemArrayWithID(inventory, changed, data, true));
        }
        if (tag.contains("tag")) {
            CompoundTag entityTag = (CompoundTag) tag.get("tag");
            if (entityTag.contains("BlockEntityTag")) {
                CompoundTag blockEntityTag = (CompoundTag) entityTag.get("BlockEntityTag");
                fixID(blockEntityTag, changed, data, recursive);
				/*ListTag items = blockEntityTag.getList("Items", Tag.TAG_COMPOUND);
				fixItemArrayWithID(items, changed, data, recursive);*/
            }
        }

        // Since 1.20.5 the legacy "tag" compound is replaced by item components. Nested item
        // stacks (shulker boxes, bundles, crossbows, ...) live in there and would otherwise keep
        // their outdated IDs and be dropped on load.
        if (recursive) {
            tag.getCompound("components").ifPresent(components -> {
                // {slot, item} pairs, e.g. a shulker box carried in an inventory
                components.getList("minecraft:container").ifPresent(container ->
                        container.forEach(slotTag -> ((CompoundTag) slotTag)
                                .getCompound("item")
                                .ifPresent(item -> fixID(item, changed, data, true))));

                // plain item lists
                components.getList("minecraft:bundle_contents").ifPresent(contents ->
                        fixItemArrayWithID(contents, changed, data, true));
                components.getList("minecraft:charged_projectiles").ifPresent(projectiles ->
                        fixItemArrayWithID(projectiles, changed, data, true));

                // the block entity carried by a placed-block item
                components.getCompound("minecraft:block_entity_data").ifPresent(blockEntity ->
                        fixID(blockEntity, changed, data, true));
            });
        }
    }

    private static List<File> getAllPlayers(File dir) {
        List<File> list = new ArrayList<>();
        dir = new File(dir, "playerdata");
        if (!dir.exists() || !dir.isDirectory()) {
            return list;
        }
        for (File file : dir.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".dat")) {
                list.add(file);
            }
        }
        return list;
    }

    private static List<File> getAllRegions(File dir, List<File> list) {
        if (list == null) {
            list = new ArrayList<>();
        }
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                getAllRegions(file, list);
            } else if (file.isFile() && file.getName().endsWith(".mca")) {
                list.add(file);
            }
        }
        return list;
    }

    /**
     * register a new Patch
     *
     * @param patch A #Supplier that will instantiate the new Patch Object
     */
    public static void registerPatch(Supplier<Patch> patch) {
        Patch.getALL().add(patch.get());
    }

    private static CompoundTag readNbt(Path file) throws IOException {
        try {
            return NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
        } catch (ZipException | EOFException e) {
            return NbtIo.read(file);
        }
    }

}
