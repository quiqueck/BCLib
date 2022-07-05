package org.betterx.worlds.together.world.event;

import net.minecraft.world.level.storage.LevelStorageSource;

import java.util.Iterator;
import java.util.function.Consumer;

class PatchWorldEvent extends EventImpl<OnWorldPatch> {

    public boolean applyPatches(
            LevelStorageSource.LevelStorageAccess storageAccess,
            Consumer<Boolean> allDone
    ) {
        return applyPatches(false, false, storageAccess, handlers.iterator(), allDone);
    }

    private boolean applyPatches(
            boolean didApplyFixes,
            boolean didShowUI,
            LevelStorageSource.LevelStorageAccess storageAccess,
            Iterator<OnWorldPatch> iterator,
            Consumer<Boolean> allDone
    ) {
        if (!iterator.hasNext()) {
            if (didShowUI) allDone.accept(didApplyFixes);
            return didApplyFixes;
        }
        OnWorldPatch now = iterator.next();

        boolean shouldHaltForUI = now.next(storageAccess, (appliedFixes) -> applyPatches(
                didApplyFixes || appliedFixes, true, storageAccess, iterator, allDone
        ));

        if (!shouldHaltForUI) {
            applyPatches(didApplyFixes, didShowUI, storageAccess, iterator, allDone);
        }
        return didApplyFixes || shouldHaltForUI;
    }

}
