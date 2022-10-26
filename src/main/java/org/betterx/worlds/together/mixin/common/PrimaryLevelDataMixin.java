package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.world.event.WorldBootstrap;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelVersion;
import net.minecraft.world.level.storage.PrimaryLevelData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;

@Mixin(PrimaryLevelData.class)
public class PrimaryLevelDataMixin {
    @Inject(method = "parse", at = @At("HEAD"))
    private static void bcl_parse(
            Dynamic<Tag> dynamic,
            DataFixer dataFixer,
            int i,
            @Nullable CompoundTag compoundTag,
            LevelSettings levelSettings,
            LevelVersion levelVersion,
            PrimaryLevelData.SpecialWorldProperty specialWorldProperty,
            WorldOptions worldOptions,
            Lifecycle lifecycle,
            CallbackInfoReturnable<PrimaryLevelData> cir
    ) {
        if (dynamic.getOps() instanceof RegistryOps<Tag> regOps) {
            WorldBootstrap.InGUI.registryReadyOnLoadedWorld(Optional.of(regOps));
        }
    }
}
