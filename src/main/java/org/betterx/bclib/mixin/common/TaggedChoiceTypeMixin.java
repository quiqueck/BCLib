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
 *
 * <h2>Why this only covers the dimension choice type</h2>
 * {@code TaggedChoiceType} is not only the dimension generator/biome-source choice. The same class
 * backs the {@code entity} and {@code block_entity} references, and handing <i>those</i> a
 * pass-through codec breaks old worlds instead of saving them. Nothing registers a modded entity or
 * block-entity id with DFU (see the note below), so every one of ours is an unknown key here; a
 * pass-through makes the read <i>succeed</i>, and the fixes that walk the value afterwards then
 * fail on a choice type that has no {@code Type} for the id:
 * <pre>
 * Failed to read chunk [0, 0]
 * java.lang.IllegalArgumentException: Added Pale Oak Boat and Pale Oak Chest Boat:
 *         Unknown type bclib:chair in 'entity'
 *         at net.minecraft.util.datafix.fixes.AddNewChoices
 *
 * Failed to load chunk 0,0
 * java.lang.IllegalArgumentException: Added Creaking Heart: Unknown type bclib:furnace
 *         in 'block_entity'
 * </pre>
 * The spawn chunks never finish loading and the world hangs at 100% on the loading screen. Making
 * {@code hasType} answer {@code true} as well only moves the failure one fix along, into
 * {@code EntityRenameFix} &rarr; {@code ExtraDataFixUtils.patchSubType}, which dereferences the
 * missing {@code Type} directly:
 * {@code NullPointerException: Cannot invoke "Type.all(...)" because "type" is null}. There is no
 * way to satisfy those callers without a real {@code Type} in the choice map.
 * <p>
 * Restricting the workaround to choice types keyed on {@code "type"} - which is how the generator
 * and biome-source choices are declared, and only them; content is keyed on {@code "id"} - keeps
 * the dimension fix and leaves world content on vanilla's own path. There an unknown id simply
 * fails the read, DFU hands the data back unfixed, and the world loads. Block entities do better
 * still: vanilla wraps them in {@code DSL.or(BLOCK_ENTITY, remainder())} per element, an escape
 * hatch a pass-through defeats by making the left branch match, so leaving them alone is what lets
 * each one fall back individually.
 *
 * <h2>Known limitation</h2>
 * A chunk's {@code entities} list is <i>not</i> wrapped in that {@code or(..., remainder())}, so a
 * single modded entity makes DFU skip the whole {@code entity_chunk} - the vanilla mobs in it miss
 * their fixes too (a 1.21 world's mobs keep {@code minecraft:generic.movement_speed} and lose their
 * attribute modifiers on load). Fixing that properly needs the modded ids to be in the DFU schemas,
 * and there is no point at which a mod can put them there: the schemas are built during vanilla's
 * own static initialisation ({@code Items.<clinit>} &rarr; {@code EntityType.<clinit>} &rarr;
 * {@code Util.fetchChoiceType} &rarr; {@code DataFixers.<clinit>}), before any mod initialiser
 * runs, and Fabric API ships no data-fixer module. This is the same trade-off Fabric API itself
 * takes with {@code fabric-object-builder-api-v1}'s {@code allowNoModdedDatafixers}.
 */
// Fabric's own injector sits at the head of the same method and cancels, so this one has to be
// applied before it to get a look at the key at all - hence the below-default priority.
@Mixin(value = TaggedChoice.TaggedChoiceType.class, remap = false, priority = 100)
public class TaggedChoiceTypeMixin<K> {
    @Shadow
    @Final
    protected Object2ObjectMap<K, Type<?>> types;

    @Shadow(remap = false)
    public String getName() {
        throw new AssertionError("shadow");
    }

    /** The namespaces whose generator/biome-source ids this workaround covers. */
    @Unique
    private static final String[] bclib_ownNamespaces = {"bclib:", "wover:"};

    /**
     * The tag the generator and biome-source choices are keyed on. World content ({@code entity},
     * {@code block_entity}) is keyed on {@code "id"} and must not be touched - see the class
     * javadoc.
     */
    @Unique
    private static final String bclib_dimensionChoiceTag = "type";

    @Inject(method = "getMapCodec", at = @At("HEAD"), cancellable = true, remap = false)
    private void bclib_passThroughOwnGenerators(
            K key,
            CallbackInfoReturnable<DataResult<? extends MapCodec<?>>> cir
    ) {
        // A key DFU already knows must keep its real type.
        if (key == null || types.containsKey(key)) return;
        if (!bclib_dimensionChoiceTag.equals(getName())) return;

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
