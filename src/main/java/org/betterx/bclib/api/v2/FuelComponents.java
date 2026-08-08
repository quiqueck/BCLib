package org.betterx.bclib.api.v2;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.interfaces.Fuel;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.ApiStatus;

/**
 * Makes the item of every {@link Fuel} block a furnace fuel.
 * <p>
 * 26.3 removed Fabric's {@code FuelValueEvents} and vanilla's static burn-time map, replacing both with
 * the data-driven {@code minecraft:cooking_fuel} item component ({@link CookingFuel}), whose
 * {@code burnTime}/{@code speedMultiplier} are {@link ResourceKey}s into the new
 * {@code minecraft:number_provider} datapack registry. This is the same change WorldWeaver's
 * {@code FuelBlockTrait} adapted to (commit {@code edfd33f5}).
 * <p>
 * Because a burn time is now a registry reference rather than an {@code int}, {@link Fuel#getFuelTime()}'s
 * value is matched against vanilla's own built-in {@code cooking/time_*} providers, each of which carries
 * exactly one tick value (verified against the 26.3 jar's
 * {@code data/minecraft/number_provider/cooking/*.json}). A block asking for a tick count vanilla has no
 * provider for is reported and left without the component, rather than a new registry entry being
 * bootstrapped - exactly the restriction WorldWeaver's {@code FuelBlockTrait#withTicks} took.
 * <p>
 * <b>Behaviour change:</b> up to 26.1 this registration lived in {@code BCLAutoBlockTagProvider}'s
 * {@code processBlockCommon}, which is only reachable from {@code prepareTags} - i.e. it only ever ran
 * during a datagen run, so {@link Fuel} had no effect in an actual game. Driving it from
 * {@code DefaultItemComponentEvents.MODIFY} makes it work at runtime, which is what the interface always
 * claimed to do.
 */
@ApiStatus.Internal
public final class FuelComponents {
    private FuelComponents() {
    }

    /**
     * Vanilla's built-in burn-time providers, keyed by the tick count they encode (their
     * {@code on_false} value; {@code on_true} is the halved blast-furnace/smoker value, which
     * {@code speed_default} accounts for separately).
     */
    private static final Map<Integer, ResourceKey<NumberProvider>> VANILLA_BURN_TIMES = new HashMap<>();

    private static final ResourceKey<NumberProvider> SPEED_DEFAULT = provider("cooking/speed_default");

    static {
        VANILLA_BURN_TIMES.put(50, provider("cooking/time_bamboo"));
        VANILLA_BURN_TIMES.put(67, provider("cooking/time_wool_carpets"));
        VANILLA_BURN_TIMES.put(100, provider("cooking/time_wood_items_extra_small"));
        VANILLA_BURN_TIMES.put(150, provider("cooking/time_wood_slabs"));
        VANILLA_BURN_TIMES.put(200, provider("cooking/time_wood_items_large"));
        VANILLA_BURN_TIMES.put(300, provider("cooking/time_wood_blocks"));
        VANILLA_BURN_TIMES.put(800, provider("cooking/time_hanging_signs"));
        VANILLA_BURN_TIMES.put(1200, provider("cooking/time_boats"));
        VANILLA_BURN_TIMES.put(1600, provider("cooking/time_coal"));
        VANILLA_BURN_TIMES.put(2400, provider("cooking/time_blaze_rod"));
        VANILLA_BURN_TIMES.put(4001, provider("cooking/time_dried_kelp_block"));
        VANILLA_BURN_TIMES.put(16000, provider("cooking/time_coal_block"));
        VANILLA_BURN_TIMES.put(20000, provider("cooking/time_lava_bucket"));
    }

    private static ResourceKey<NumberProvider> provider(String path) {
        return ResourceKey.create(Registries.NUMBER_PROVIDER, Identifier.withDefaultNamespace(path));
    }

    /**
     * Registers the listener. Called once from {@code BCLib#onInitialize}.
     */
    public static void register() {
        DefaultItemComponentEvents.MODIFY.register(context -> {
            final Map<Item, CookingFuel> fuels = collectFuels();
            if (fuels.isEmpty()) return;

            context.modify(
                    fuels::containsKey,
                    (builder, item) -> builder.set(DataComponents.COOKING_FUEL, fuels.get(item))
            );
        });
    }

    private static Map<Item, CookingFuel> collectFuels() {
        final Map<Item, CookingFuel> result = new LinkedHashMap<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (!(block instanceof Fuel fuel)) continue;

            final int ticks = fuel.getFuelTime();
            final ResourceKey<NumberProvider> burnTime = VANILLA_BURN_TIMES.get(ticks);
            if (burnTime == null) {
                BCLib.LOGGER.error(
                        "Block " + block + " asks for a burn time of " + ticks + " ticks, but 26.3 expresses"
                                + " fuel as a reference into minecraft:number_provider and vanilla ships no"
                                + " provider with that value. The block will not be a furnace fuel."
                );
                continue;
            }

            final Item item = ((ItemLike) block).asItem();
            if (item == null || item == net.minecraft.world.item.Items.AIR) continue;
            result.put(item, new CookingFuel(burnTime, SPEED_DEFAULT));
        }
        return result;
    }
}
