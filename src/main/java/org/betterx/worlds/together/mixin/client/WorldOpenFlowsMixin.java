package org.betterx.worlds.together.mixin.client;

import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.world.event.WorldBootstrap;

import com.mojang.serialization.Dynamic;
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

import java.io.IOException;
import java.util.function.Function;

@Mixin(WorldOpenFlows.class)
public abstract class WorldOpenFlowsMixin {

    @Shadow
    @Final
    private LevelStorageSource levelSource;


    @Shadow
    protected abstract void loadLevel(
            LevelStorageSource.LevelStorageAccess levelStorageAccess,
            Dynamic<?> dynamic,
            boolean bl,
            boolean bl2,
            Runnable runnable
    );

    @Inject(
            method = "checkForBackupAndLoad(Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Ljava/lang/Runnable;)V",
            cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/worldselection/WorldOpenFlows;loadLevel(Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lcom/mojang/serialization/Dynamic;ZZLjava/lang/Runnable;)V")
    )
    private void wt_callFixerOnLoad(
            LevelStorageSource.LevelStorageAccess levelStorageAccess,
            Runnable runnable,
            CallbackInfo ci
    ) throws IOException {
        final String levelID = levelStorageAccess.getLevelId();
        final var dynamic = levelStorageAccess.getDataTag();
        WorldBootstrap.InGUI.setupLoadedWorld(levelID, this.levelSource);

        if (WorldBootstrap.InGUI.applyWorldPatches(levelSource, levelID, (appliedFixes) -> {
            WorldBootstrap.finishedWorldLoad();
            this.loadLevel(levelStorageAccess, dynamic, false, false, runnable);
        })) {
            //cancel call when fix-screen is presented
            ci.cancel();
        } else {
            WorldBootstrap.finishedWorldLoad();
            if (WorldsTogether.SURPRESS_EXPERIMENTAL_DIALOG) {
                this.loadLevel(levelStorageAccess, dynamic, false, false, runnable);
                //cancel call as we manually start the level load here
                ci.cancel();
            }
        }
    }

    @Inject(method = "createFreshLevel", at = @At("HEAD"))
    public void wt_createFreshLevel(
            String string,
            LevelSettings levelSettings,
            WorldOptions worldOptions,
            Function<RegistryAccess, WorldDimensions> function,
            Screen screen,
            CallbackInfo ci
    ) {
        WorldsTogether.LOGGER.warning("called createFreshLevel...");
        //TODO: 1.19.3 no mor dimensions at this stage...
        //WorldBootstrap.InFreshLevel.setupNewWorld(levelID, worldGenSettings, this.levelSource, Optional.empty());
    }
}
