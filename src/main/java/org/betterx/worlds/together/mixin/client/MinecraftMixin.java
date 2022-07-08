package org.betterx.worlds.together.mixin.client;

import org.betterx.worlds.together.WorldsTogether;
import org.betterx.worlds.together.world.event.WorldBootstrap;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.WorldStem;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorageSource;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    protected abstract void doLoadLevel(
            String string,
            Function<LevelStorageSource.LevelStorageAccess, WorldStem.DataPackConfigSupplier> function,
            Function<LevelStorageSource.LevelStorageAccess, WorldStem.WorldDataSupplier> function2,
            boolean bl,
            Minecraft.ExperimentalDialogType experimentalDialogType
    );

    @Shadow
    @Final
    private LevelStorageSource levelSource;

    @Inject(method = "loadLevel", cancellable = true, at = @At("HEAD"))
    private void wt_callFixerOnLoad(String levelID, CallbackInfo ci) {
        WorldBootstrap.InGUI.setupLoadedWorld(levelID, this.levelSource);

        //if (DataFixerAPI.fixData(this.levelSource, levelID, true, (appliedFixes) -> {
        if (WorldBootstrap.InGUI.applyWorldPatches(levelSource, levelID, (appliedFixes) -> {
            WorldBootstrap.InGUI.finishedWorldLoad(levelID, this.levelSource);
            this.doLoadLevel(
                    levelID,
                    WorldStem.DataPackConfigSupplier::loadFromWorld,
                    WorldStem.WorldDataSupplier::loadFromWorld,
                    false,
                    Minecraft.ExperimentalDialogType.NONE
            );
        })) {
            //cancel call when fix-screen is presented
            ci.cancel();
        } else {
            WorldBootstrap.InGUI.finishedWorldLoad(levelID, this.levelSource);
            if (WorldsTogether.SURPRESS_EXPERIMENTAL_DIALOG) {
                this.doLoadLevel(
                        levelID,
                        WorldStem.DataPackConfigSupplier::loadFromWorld,
                        WorldStem.WorldDataSupplier::loadFromWorld,
                        false,
                        Minecraft.ExperimentalDialogType.NONE
                );
                //cancel call as we manually start the level load here
                ci.cancel();
            }
        }
    }

    @Inject(method = "createLevel", at = @At("HEAD"))
    private void wt_callOnCreate(
            String levelID,
            LevelSettings levelSettings,
            RegistryAccess registryAccess,
            WorldGenSettings worldGenSettings,
            CallbackInfo ci
    ) {

    }
}
