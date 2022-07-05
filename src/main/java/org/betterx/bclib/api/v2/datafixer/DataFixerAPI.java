package org.betterx.bclib.api.v2.datafixer;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.client.gui.screens.AtomicProgressListener;
import org.betterx.bclib.client.gui.screens.ConfirmFixScreen;
import org.betterx.bclib.client.gui.screens.LevelFixErrorScreen;
import org.betterx.bclib.client.gui.screens.LevelFixErrorScreen.Listener;
import org.betterx.bclib.client.gui.screens.ProgressScreen;
import org.betterx.bclib.config.Configs;
import org.betterx.worlds.together.util.Logger;
import org.betterx.worlds.together.world.WorldConfig;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
    static final Logger LOGGER = new Logger("DataFixerAPI");

    static class State {
        public boolean didFail = false;
        protected ArrayList<String> errors = new ArrayList<>();

        public void addError(String s) {
            errors.add(s);
        }

        public boolean hasError() {
            return errors.size() > 0;
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
            BCLib.LOGGER.warning("Failed to read level {} data", levelID, e);
            SystemToast.onWorldAccessFailure(Minecraft.getInstance(), levelID);
            Minecraft.getInstance().setScreen(null);
            return true;
        }

        boolean returnValue = runWithLevel.apply(levelStorageAccess);

        try {
            levelStorageAccess.close();
        } catch (IOException e) {
            BCLib.LOGGER.warning("Failed to unlock access to level {}", levelID, e);
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
        return fixData(levelPath, levelStorageAccess.getLevelId(), showUI, onResume);
    }

    /**
     * Creates the patch level file for new worlds
     */
    public static void initializePatchData() {
        getMigrationProfile().markApplied();
        WorldConfig.saveFile(BCLib.MOD_ID);
    }


    @Environment(EnvType.CLIENT)
    private static AtomicProgressListener showProgressScreen() {
        ProgressScreen ps = new ProgressScreen(
                Minecraft.getInstance().screen,
                Component.translatable("title.bclib.datafixer.progress"),
                Component.translatable("message.bclib.datafixer.progress")
        );
        Minecraft.getInstance().setScreen(ps);
        return ps;
    }

    private static boolean fixData(File dir, String levelID, boolean showUI, Consumer<Boolean> onResume) {
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
                if (createBackup) {
                    progress.progressStage(Component.translatable("message.bclib.datafixer.progress.waitbackup"));
                    EditWorldScreen.makeBackupAndShowToast(Minecraft.getInstance().getLevelSource(), levelID);
                }

                if (applyFixes) {
                    return runDataFixes(dir, profile, progress);
                }

                return new State();
            };

            if (showUI) {
                Thread fixerThread = new Thread(() -> {
                    final State state = runner.get();

                    Minecraft.getInstance()
                             .execute(() -> {
                                 if (profile != null && showUI) {
                                     //something went wrong, show the user our error
                                     if (state.didFail || state.hasError()) {
                                         showLevelFixErrorScreen(state, (markFixed) -> {
                                             if (markFixed) {
                                                 profile.markApplied();
                                             }
                                             onResume.accept(applyFixes);
                                         });
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
                BCLib.LOGGER.warning("Applying Fixes on Level");
                runFixes.accept(false, true);
            }
        }
        return false;
    }

    @Environment(EnvType.CLIENT)
    private static void showLevelFixErrorScreen(State state, Listener onContinue) {
        Minecraft.getInstance()
                 .setScreen(new LevelFixErrorScreen(
                         Minecraft.getInstance().screen,
                         state.getErrorMessages(),
                         onContinue
                 ));
    }

    private static MigrationProfile loadProfileIfNeeded(File levelBaseDir) {
        if (!Configs.MAIN_CONFIG.applyPatches()) {
            LOGGER.info("World Patches are disabled");
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
        final CompoundTag patchConfig = WorldConfig.getCompoundTag(BCLib.MOD_ID, Configs.MAIN_PATCH_CATEGORY);
        MigrationProfile profile = Patch.createMigrationData(patchConfig);
        return profile;
    }

    @Environment(EnvType.CLIENT)
    static void showBackupWarning(String levelID, BiConsumer<Boolean, Boolean> whenFinished) {
        Minecraft.getInstance().setScreen(new ConfirmFixScreen(null, whenFinished::accept));
    }

    private static State runDataFixes(File dir, MigrationProfile profile, AtomicProgressListener progress) {
        State state = new State();
        progress.resetAtomic();

        progress.progressStage(Component.translatable("message.bclib.datafixer.progress.reading"));
        List<File> players = getAllPlayers(dir);
        List<File> regions = getAllRegions(dir, null);
        final int maxProgress = players.size() + regions.size() + 4;
        progress.incAtomic(maxProgress);

        progress.progressStage(Component.translatable("message.bclib.datafixer.progress.players"));
        players.parallelStream().forEach((file) -> {
            fixPlayer(profile, state, file);
            progress.incAtomic(maxProgress);
        });

        progress.progressStage(Component.translatable("message.bclib.datafixer.progress.level"));
        fixLevel(profile, state, dir);
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
            fixRegion(profile, state, file);
            progress.incAtomic(maxProgress);
        });

        if (!state.didFail) {
            progress.progressStage(Component.translatable("message.bclib.datafixer.progress.saving"));
            profile.markApplied();
            WorldConfig.saveFile(BCLib.MOD_ID);
        }
        progress.incAtomic(maxProgress);

        progress.stop();

        return state;
    }

    private static void fixLevel(MigrationProfile profile, State state, File levelBaseDir) {
        try {
            LOGGER.info("Inspecting level.dat in " + levelBaseDir);

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
                LOGGER.warning("Writing '{}'", profile.getLevelDatFile());
                NbtIo.writeCompressed(level, profile.getLevelDatFile());
            }
        } catch (Exception e) {
            BCLib.LOGGER.error("Failed fixing Level-Data.");
            state.addError("Failed fixing Level-Data in level.dat (" + e.getMessage() + ")");
            state.didFail = true;
            e.printStackTrace();
        }
    }

    private static void fixPlayer(MigrationProfile data, State state, File file) {
        try {
            LOGGER.info("Inspecting " + file);

            CompoundTag player = readNbt(file);
            boolean[] changed = {false};
            fixPlayerNbt(player, changed, data);

            if (changed[0]) {
                LOGGER.warning("Writing '{}'", file);
                NbtIo.writeCompressed(player, file);
            }
        } catch (Exception e) {
            BCLib.LOGGER.error("Failed fixing Player-Data.");
            state.addError("Failed fixing Player-Data in " + file.getName() + " (" + e.getMessage() + ")");
            state.didFail = true;
            e.printStackTrace();
        }
    }

    private static void fixPlayerNbt(CompoundTag player, boolean[] changed, MigrationProfile data) {
        //Checking Inventory
        ListTag inventory = player.getList("Inventory", Tag.TAG_COMPOUND);
        fixItemArrayWithID(inventory, changed, data, true);

        //Checking EnderChest
        ListTag enderitems = player.getList("EnderItems", Tag.TAG_COMPOUND);
        fixItemArrayWithID(enderitems, changed, data, true);

        //Checking ReceipBook
        if (player.contains("recipeBook")) {
            CompoundTag recipeBook = player.getCompound("recipeBook");
            changed[0] |= fixStringIDList(recipeBook, "recipes", data);
            changed[0] |= fixStringIDList(recipeBook, "toBeDisplayed", data);
        }
    }

    static boolean fixStringIDList(CompoundTag root, String name, MigrationProfile data) {
        boolean _changed = false;
        if (root.contains(name)) {
            ListTag items = root.getList(name, Tag.TAG_STRING);
            ListTag newItems = new ListTag();

            for (Tag tag : items) {
                final StringTag str = (StringTag) tag;
                final String replace = data.replaceStringFromIDs(str.getAsString());
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

    private static void fixRegion(MigrationProfile data, State state, File file) {
        try {
            Path path = file.toPath();
            LOGGER.info("Inspecting " + path);
            boolean[] changed = new boolean[1];
            RegionFile region = new RegionFile(path, path.getParent(), true);

            for (int x = 0; x < 32; x++) {
                for (int z = 0; z < 32; z++) {
                    ChunkPos pos = new ChunkPos(x, z);
                    changed[0] = false;
                    if (region.hasChunk(pos) && !state.didFail) {
                        DataInputStream input = region.getChunkDataInputStream(pos);
                        CompoundTag root = NbtIo.read(input);
                        // if ((root.toString().contains("betternether:chest") || root.toString().contains("bclib:chest"))) {
                        //   NbtIo.write(root, new File(file.toString() + "-" + x + "-" + z + ".nbt"));
                        // }
                        input.close();

                        //Checking TileEntities
                        ListTag tileEntities = root.getCompound("Level")
                                                   .getList("TileEntities", Tag.TAG_COMPOUND);
                        fixItemArrayWithID(tileEntities, changed, data, true);

                        //Checking Entities
                        ListTag entities = root.getList("Entities", Tag.TAG_COMPOUND);
                        fixItemArrayWithID(entities, changed, data, true);

                        //Checking Block Palette
                        ListTag sections = root.getCompound("Level")
                                               .getList("Sections", Tag.TAG_COMPOUND);
                        sections.forEach((tag) -> {
                            ListTag palette = ((CompoundTag) tag).getList("Palette", Tag.TAG_COMPOUND);
                            palette.forEach((blockTag) -> {
                                CompoundTag blockTagCompound = ((CompoundTag) blockTag);
                                changed[0] |= data.replaceStringFromIDs(blockTagCompound, "Name");
                            });

                            try {
                                changed[0] |= data.patchBlockState(
                                        palette,
                                        ((CompoundTag) tag).getList(
                                                "BlockStates",
                                                Tag.TAG_LONG
                                        )
                                );
                            } catch (PatchDidiFailException e) {
                                BCLib.LOGGER.error("Failed fixing BlockState in " + pos);
                                state.addError("Failed fixing BlockState in " + pos + " (" + e.getMessage() + ")");
                                state.didFail = true;
                                changed[0] = false;
                                e.printStackTrace();
                            }
                        });

                        if (changed[0]) {
                            LOGGER.warning("Writing '{}': {}/{}", file, x, z);
                            // NbtIo.write(root, new File(file.toString() + "-" + x + "-" + z + "-changed.nbt"));
                            DataOutputStream output = region.getChunkDataOutputStream(pos);
                            NbtIo.write(root, output);
                            output.close();
                        }
                    }
                }
            }
            region.close();
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
            patchConfTag = WorldConfig.getCompoundTag(BCLib.MOD_ID, Configs.MAIN_PATCH_CATEGORY);
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

        if (recursive && tag.contains("Items")) {
            fixItemArrayWithID(tag.getList("Items", Tag.TAG_COMPOUND), changed, data, true);
        }
        if (recursive && tag.contains("Inventory")) {
            ListTag inventory = tag.getList("Inventory", Tag.TAG_COMPOUND);
            fixItemArrayWithID(inventory, changed, data, true);
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

    private static CompoundTag readNbt(File file) throws IOException {
        try {
            return NbtIo.readCompressed(file);
        } catch (ZipException | EOFException e) {
            return NbtIo.read(file);
        }
    }

}
