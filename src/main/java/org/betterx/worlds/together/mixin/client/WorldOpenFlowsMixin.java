package org.betterx.worlds.together.mixin.client;

import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.world.event.WorldBootstrap;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(WorldOpenFlows.class)
public abstract class WorldOpenFlowsMixin {

    @Shadow
    @Final
    private LevelStorageSource levelSource;

    @Shadow
    protected abstract void doLoadLevel(Screen screen, String levelID, boolean safeMode, boolean canAskForBackup);

    @Inject(method = "loadLevel", cancellable = true, at = @At("HEAD"))
    private void bcl_callFixerOnLoad(Screen screen, String levelID, CallbackInfo ci) {
        WorldBootstrap.InGUI.setupLoadedWorld(levelID, this.levelSource);

        //if (DataFixerAPI.fixData(this.levelSource, levelID, true, (appliedFixes) -> {
        if (WorldBootstrap.InGUI.applyWorldPatches(levelSource, levelID, (appliedFixes) -> {
            WorldBootstrap.InGUI.finishedWorldLoad(levelID, this.levelSource);
            this.doLoadLevel(screen, levelID, false, false);
        })) {
            //cancel call when fix-screen is presented
            ci.cancel();
        } else {
            WorldBootstrap.InGUI.finishedWorldLoad(levelID, this.levelSource);
            if (WorldsTogether.SURPRESS_EXPERIMENTAL_DIALOG) {
                this.doLoadLevel(screen, levelID, false, false);
                //cancel call as we manually start the level load here
                ci.cancel();
            }
        }
    }

    @Inject(method = "createFreshLevel", at = @At("HEAD"))
    public void bcl_createFreshLevel(
            String levelID,
            LevelSettings levelSettings,
            RegistryAccess registryAccess,
            WorldGenSettings worldGenSettings,
            CallbackInfo ci
    ) {
        WorldBootstrap.InFreshLevel.setupNewWorld(levelID, worldGenSettings, this.levelSource, Optional.empty());
    }

    @Inject(method = "createLevelFromExistingSettings", at = @At("HEAD"))
    public void bcl_createLevelFromExistingSettings(
            LevelStorageSource.LevelStorageAccess levelStorageAccess,
            ReloadableServerResources reloadableServerResources,
            RegistryAccess.Frozen frozen,
            WorldData worldData,
            CallbackInfo ci
    ) {
        //called from the CreateWorldScreenMixin now
        //LifeCycleAPI.newWorldSetup(levelStorageAccess, worldData.worldGenSettings());
    }
}
