package org.betterx.bclib.api.v2;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.trait.block.CompostableBlockTrait;
import org.betterx.bclib.trait.item.CompostableItemTrait;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Compostable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.ResolvableNumber;

import com.mojang.serialization.MapCodec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Runtime-registered compostability.
 * <p>
 * Up to 26.1 this simply put the item into vanilla's static {@code ComposterBlock.COMPOSTABLES}
 * {@code Object2FloatMap} (through {@code ComposterBlockAccessor}). 26.3 deleted both the map and the
 * {@code ComposterBlock.add(float, ItemLike)} method that filled it: compostability is now the
 * {@code minecraft:compostable} item component ({@link Compostable}), which does not carry a chance at all
 * but a {@link ResourceKey} into the reloadable {@code minecraft:number_provider} datapack registry. The
 * referenced provider returns how many layers to add, and vanilla's five tiers
 * ({@code compostable/low}, {@code low_medium}, {@code medium}, {@code medium_high}, {@code always_add_one})
 * encode exactly 30/50/65/85/100&nbsp;% - plus the "the first item into an empty composter always fills a
 * layer" rule that 26.1's {@code ComposterBlock#addItem} applied in Java.
 * <p>
 * A registry reference cannot express an arbitrary {@code float}, and BCLib's own default chance (0.1) is not
 * one of the five tiers, so instead of quantizing every registration to the nearest vanilla tier this class
 * keeps the chances in a runtime table and hands {@code ComposterBlockMixin} a <em>synthetic</em>
 * {@link Compostable} whose key is an opaque token ({@code bclib:compostable/runtime/<permille>}) that is
 * never resolved against the registry. The mixin recognises the token, looks the chance back up here and
 * substitutes a {@link RuntimeCompostChance} for the provider vanilla failed to resolve. Composting therefore
 * keeps using the exact chance that was registered, still resolved at composter-use time, and vanilla items
 * (which do carry a real component) are never touched.
 * <p>
 * Deliberately <em>no</em> {@code minecraft:compostable} component is written onto modded items: WoVer's
 * {@code WoverItemRegistrationsProvider}/{@code WoverBlockRegistrationsProvider} read that component first
 * and only fall back to the {@code CompostableTrait} because modded blocks never carry it, so setting it
 * would change the datagen oracles from reporting the chance to reporting a registry id.
 *
 * @see CompostableBlockTrait
 * @see CompostableItemTrait
 */
public class ComposterAPI {
    private ComposterAPI() {
    }

    /**
     * Path prefix of the synthetic, never-registered {@link ResourceKey} used as a marker in
     * {@link Compostable#layers()}. The suffix is the composting chance in permille, which makes the token
     * readable in a debugger or crash report; the authoritative value is {@link #KEY_TO_CHANCE}.
     */
    private static final String RUNTIME_PATH_PREFIX = "compostable/runtime/";

    /** Items registered through {@link #allowCompost(float, Item)}. */
    private static final Map<Item, Float> REGISTERED = new ConcurrentHashMap<>();

    private static final Map<Float, Compostable> CHANCE_TO_MARKER = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<NumberProvider>, Float> KEY_TO_CHANCE = new ConcurrentHashMap<>();

    public static Block allowCompost(float chance, Block block) {
        if (block != null) {
            allowCompost(chance, block.asItem());
        }
        return block;
    }

    public static Item allowCompost(float chance, Item item) {
        if (item != null && item != Items.AIR && chance > 0) {
            REGISTERED.put(item, chance);
        }
        return item;
    }

    /**
     * Resolves the composting chance BCLib knows for {@code item}, in the same precedence the 26.1
     * {@code ComposterBlockMixin} used: an explicit {@link #allowCompost} registration first (it used to
     * write straight into the vanilla map), then the block's {@link CompostableBlockTrait}, then the item's
     * own {@link CompostableItemTrait}.
     *
     * @param item the item a composter is being filled with
     * @return the chance in {@code (0,1]}, or {@code -1} when BCLib knows nothing about the item
     */
    public static float compostChance(Item item) {
        if (item == null) return -1.0f;

        final Float registered = REGISTERED.get(item);
        if (registered != null) return registered;

        final float blockChance = CompostableBlockTrait.chanceFor(item);
        if (blockChance >= 0.0f) return blockChance;

        return CompostableItemTrait.chanceFor(item);
    }

    /**
     * The marker component {@code ComposterBlockMixin} substitutes when vanilla reports no
     * {@code minecraft:compostable} for the item.
     *
     * @param item the item a composter is being filled with
     * @return {@code null} when BCLib has no composting chance for {@code item}
     */
    @ApiStatus.Internal
    @Nullable
    public static Compostable runtimeCompostable(Item item) {
        final float chance = compostChance(item);
        if (chance < 0.0f) return null;

        // Quantize to permille so the (readable) key and the lookup table can never disagree.
        final float quantized = Math.round(chance * 1000.0f) / 1000.0f;
        return CHANCE_TO_MARKER.computeIfAbsent(quantized, c -> {
            final ResourceKey<NumberProvider> key = ResourceKey.create(
                    Registries.NUMBER_PROVIDER,
                    BCLib.C.id(RUNTIME_PATH_PREFIX + Math.round(c * 1000.0f))
            );
            KEY_TO_CHANCE.put(key, c);
            return new Compostable(key);
        });
    }

    /**
     * Reverses {@link #runtimeCompostable(Item)}.
     *
     * @param layers the value {@code Compostable#layers()} produced
     * @return the chance the marker stands for, or {@code -1} when {@code layers} is a real (vanilla or
     * datapack) reference/constant rather than one of ours
     */
    @ApiStatus.Internal
    public static float runtimeChance(ResolvableNumber layers) {
        // runtimeCompostable() always builds a Reference (via the ResourceKey constructor), so anything else
        // is never one of our own markers.
        if (!(layers instanceof ResolvableNumber.Reference reference)) return -1.0f;
        final Float chance = KEY_TO_CHANCE.get(reference.key());
        return chance == null ? -1.0f : chance;
    }

    /**
     * The layer count {@code ComposterBlockMixin} substitutes for one of our runtime markers, reproducing
     * {@link RuntimeCompostChance}'s roll.
     *
     * @param layers      the value {@code Compostable#layers()} produced
     * @param lootContext the loot context {@code ComposterBlock#addLayer} resolves {@code layers} against
     * @return {@code 0} or {@code 1} layers to add, or {@code -1} when {@code layers} is not one of ours
     */
    @ApiStatus.Internal
    public static int runtimeLayerCount(ResolvableNumber layers, LootContext lootContext) {
        final float chance = runtimeChance(layers);
        if (chance < 0.0f) return -1;
        return new RuntimeCompostChance(chance).getFloat(lootContext) > 0.0f ? 1 : 0;
    }

    /**
     * Reproduces 26.1's {@code ComposterBlock#addItem} roll as a {@link NumberProvider}, so that everything
     * vanilla's {@code ComposterBlock#addLayer} does around the layer count (block update, game event, the
     * scheduled tick at level 7, and returning the state unchanged when the roll fails) keeps running
     * untouched.
     * <p>
     * 26.1: {@code if (level != 0 || !(chance > 0)) { if (random.nextDouble() >= chance) return state; }} -
     * i.e. add a layer iff the composter is empty, or the roll succeeds. That is exactly what vanilla's own
     * {@code compostable/*} providers encode as a {@code number_dispatcher} on {@code level=0}.
     */
    @ApiStatus.Internal
    public record RuntimeCompostChance(float chance) implements NumberProvider {
        @Override
        public float getFloat(LootContext lootContext) {
            final BlockState state = lootContext.getOptionalParameter(LootContextParams.BLOCK_STATE);
            final int fill = state != null && state.hasProperty(ComposterBlock.LEVEL)
                    ? state.getValue(ComposterBlock.LEVEL)
                    : 0;

            final boolean add = (fill == 0 && chance > 0.0f) || lootContext.getRandom().nextDouble() < chance;
            return add ? 1.0f : 0.0f;
        }

        /**
         * Never called: the instance is handed straight to {@code ComposterBlock#addLayer} and is never part
         * of a registry or a serialized loot table.
         */
        @Override
        public MapCodec<? extends NumberProvider> codec() {
            throw new UnsupportedOperationException(
                    "BCLib's runtime composting chance is not serializable - it only ever exists for the"
                            + " duration of a single ComposterBlock#addLayer call."
            );
        }
    }
}
