package org.betterx.worlds.together.mixin.common;

import org.betterx.worlds.together.tag.v3.TagManager;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagLoader;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;

@Mixin(TagLoader.class)
public class TagLoaderMixin {
    @Final
    @Shadow
    private String directory;

    @ModifyArg(method = "loadAndBuild", at = @At(value = "INVOKE", target = "Lnet/minecraft/tags/TagLoader;build(Ljava/util/Map;)Ljava/util/Map;"))
    public Map<ResourceLocation, Tag.Builder> be_modifyTags(Map<ResourceLocation, Tag.Builder> tagsMap) {
        return TagManager.apply(directory, tagsMap);
    }
}
