package org.betterx.worlds.together.mixin.client;

import org.betterx.worlds.together.world.event.WorldBootstrap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.jetbrains.annotations.Nullable;

@Mixin(WorldOpenFlows.class)
public abstract class WorldOpenFlowsMixin {

    @Shadow
    @Final
    private LevelStorageSource levelSource;


    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Nullable
    protected abstract LevelStorageSource.LevelStorageAccess createWorldAccess(String string);

    @Shadow
    protected abstract void openWorldLoadLevelData(
            LevelStorageSource.LevelStorageAccess levelStorageAccess,
            Runnable runnable
    );

    @Inject(
            method = "openWorld(Ljava/lang/String;Ljava/lang/Runnable;)V",
            cancellable = true,
            at = @At("HEAD")
    )
    private void wt_callFixerOnLoad(String levelID, Runnable screenRunner, CallbackInfo ci) {
        this.minecraft.forceSetScreen(new GenericMessageScreen(Component.translatable("selectWorld.data_read")));


        WorldBootstrap.InGUI.setupLoadedWorld(levelID, this.levelSource);
        if (WorldBootstrap.InGUI.applyWorldPatches(levelSource, levelID, (appliedFixes) -> {
            WorldBootstrap.finishedWorldLoad();

            LevelStorageSource.LevelStorageAccess levelStorageAccess = this.createWorldAccess(levelID);
            if (levelStorageAccess == null) {
                return;
            }
            this.openWorldLoadLevelData(levelStorageAccess, screenRunner);
        })) {
            //cancel call when fix-screen is presented
            ci.cancel();
        } else {
            WorldBootstrap.finishedWorldLoad();
        }
    }
}
