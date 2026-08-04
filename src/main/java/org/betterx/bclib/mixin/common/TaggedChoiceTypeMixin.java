package org.betterx.bclib.mixin.common;

import org.betterx.bclib.BCLib;

import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps a world whose dimensions use one of our chunk generators loadable when Minecraft's
 * DataFixerUpper walks {@code WorldGenSettings}.
 * <p>
 * DFU's {@code TaggedChoiceType} only knows the vanilla generator ids, and Fabric API patches
 * {@code getMapCodec} so an unknown id falls back to {@link Codec#PASSTHROUGH} instead of failing.
 * In {@code fabric-dimensions-v1} 4.0.19 &mdash; the version bundled with Fabric API for the 1.21.x
 * line &mdash; that fallback hands back a bare {@link Codec} even though DFU expects a
 * {@link MapCodec}, and the very next step casts it:
 * <pre>
 * TaggedChoiceType_DimDataFix  Not recognizing key wover:betterx. Using pass-through codec.
 * java.lang.ClassCastException: Codec$20 cannot be cast to MapCodec
 *         at TaggedChoice$TaggedChoiceType.lambda$buildCodec$2(TaggedChoice.java:205)
 * </pre>
 * The world then fails to load outright. Fabric fixed this in {@code fabric-dimensions-v1} 5.1.x by
 * wrapping the pass-through in {@link MapCodec#assumeMapUnsafe(Codec)}; this mixin does the same
 * thing, but <b>only</b> for ids in our own namespaces, so worlds using a broken Fabric API build
 * still load. Every other unknown id is left to Fabric, unchanged.
 * <p>
 * Once the surrounding Fabric API ships the fix this becomes a no-op with identical behaviour, so
 * it is safe to keep either way.
 */
// Fabric's own injector sits at the head of the same method and cancels, so this one has to be
// applied before it to get a look at the key at all - hence the below-default priority.
@Mixin(value = TaggedChoice.TaggedChoiceType.class, remap = false, priority = 100)
public class TaggedChoiceTypeMixin<K> {
    @Shadow
    @Final
    protected Object2ObjectMap<K, Type<?>> types;

    /** The namespaces whose generator/biome-source ids this workaround covers. */
    @Unique
    private static final String[] bclib_ownNamespaces = {"bclib:", "wover:"};

    @Inject(method = "getMapCodec", at = @At("HEAD"), cancellable = true, remap = false)
    private void bclib_passThroughOwnGenerators(
            K key,
            CallbackInfoReturnable<DataResult<? extends MapCodec<?>>> cir
    ) {
        // A key DFU already knows must keep its real type.
        if (key == null || types.containsKey(key)) return;

        final String id = key.toString();
        boolean ours = false;
        for (String namespace : bclib_ownNamespaces) {
            if (id.startsWith(namespace)) {
                ours = true;
                break;
            }
        }
        if (!ours) return;

        BCLib.LOGGER.info("Using pass-through DFU codec for '{}'.", id);
        cir.setReturnValue(DataResult.success(MapCodec.assumeMapUnsafe(Codec.PASSTHROUGH)));
    }
}
