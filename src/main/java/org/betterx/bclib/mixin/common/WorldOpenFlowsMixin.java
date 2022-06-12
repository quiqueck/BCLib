package org.betterx.bclib.mixin.common;

import org.betterx.bclib.api.v2.LifeCycleAPI;
import org.betterx.bclib.api.v2.dataexchange.DataExchangeAPI;
import org.betterx.bclib.api.v2.datafixer.DataFixerAPI;
import org.betterx.bclib.api.v2.levelgen.biomes.InternalBiomeAPI;
import org.betterx.bclib.config.Configs;

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

@Mixin(WorldOpenFlows.class)
public abstract class WorldOpenFlowsMixin {

    @Shadow
    @Final
    private LevelStorageSource levelSource;

    @Shadow
    protected abstract void doLoadLevel(Screen screen, String levelID, boolean safeMode, boolean canAskForBackup);

    @Inject(method = "loadLevel", cancellable = true, at = @At("HEAD"))
    private void bcl_callFixerOnLoad(Screen screen, String levelID, CallbackInfo ci) {
        DataExchangeAPI.prepareServerside();
        InternalBiomeAPI.prepareNewLevel();

        if (DataFixerAPI.fixData(this.levelSource, levelID, true, (appliedFixes) -> {
            LifeCycleAPI._runBeforeLevelLoad();
            this.doLoadLevel(screen, levelID, false, false);
        })) {
            //cancel call when fix-screen is presented
            ci.cancel();
        } else {
            LifeCycleAPI._runBeforeLevelLoad();
            if (Configs.CLIENT_CONFIG.suppressExperimentalDialog()) {
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
        LifeCycleAPI.newWorldSetup(levelID, worldGenSettings, this.levelSource);
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
