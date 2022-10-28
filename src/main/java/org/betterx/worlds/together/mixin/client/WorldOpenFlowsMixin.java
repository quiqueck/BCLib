package org.betterx.worlds.together.mixin.client;

import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.world.event.WorldBootstrap;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelStorageSource;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(WorldOpenFlows.class)
public abstract class WorldOpenFlowsMixin {

    @Shadow
    @Final
    private LevelStorageSource levelSource;

    @Shadow
    protected abstract void doLoadLevel(Screen screen, String levelID, boolean safeMode, boolean canAskForBackup);

    @Inject(method = "loadLevel", cancellable = true, at = @At("HEAD"))
    private void wt_callFixerOnLoad(Screen screen, String levelID, CallbackInfo ci) {
        WorldBootstrap.InGUI.setupLoadedWorld(levelID, this.levelSource);

        if (WorldBootstrap.InGUI.applyWorldPatches(levelSource, levelID, (appliedFixes) -> {
            WorldBootstrap.finishedWorldLoad();
            this.doLoadLevel(screen, levelID, false, false);
        })) {
            //cancel call when fix-screen is presented
            ci.cancel();
        } else {
            WorldBootstrap.finishedWorldLoad();
            if (WorldsTogether.SURPRESS_EXPERIMENTAL_DIALOG) {
                this.doLoadLevel(screen, levelID, false, false);
                //cancel call as we manually start the level load here
                ci.cancel();
            }
        }
    }

    @Inject(method = "createFreshLevel", at = @At("HEAD"))
    public void wt_createFreshLevel(
            String levelID,
            LevelSettings levelSettings,
            WorldOptions worldOptions,
            Function<RegistryAccess, WorldDimensions> function,
            CallbackInfo ci
    ) {
        WorldsTogether.LOGGER.warning("called createFreshLevel...");
        //TODO: 1.19.3 no mor dimensions at this stage...
        //WorldBootstrap.InFreshLevel.setupNewWorld(levelID, worldGenSettings, this.levelSource, Optional.empty());
    }
}
