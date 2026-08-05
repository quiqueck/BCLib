package org.betterx.bclib.api.v2;

import org.betterx.bclib.interfaces.Fuel;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.registry.FuelValueEvents;

/**
 * Makes every block implementing {@link Fuel} a furnace fuel.
 * <p>
 * This used to be wired up from {@code BCLAutoBlockTagProvider#processBlockCommon}, which is only reachable
 * from {@code prepareTags} - i.e. it only ever ran during a datagen run, never in a real game. Nothing in
 * BCLib, BetterEnd or BetterNether ever got the burn time it declared. On 26.1, {@link FuelValueEvents#BUILD}
 * (fabric-content-registries) is still the right API for this - unlike 26.3, which replaced it with the
 * data-driven {@code minecraft:cooking_fuel} item component - so the fix is simply to register the listener
 * from mod init instead of from the datagen provider.
 */
public final class FuelValueRegistration {
    private FuelValueRegistration() {
    }

    /**
     * Registers the listener. Called once from {@code BCLib#onInitialize}.
     */
    public static void register() {
        FuelValueEvents.BUILD.register((builder, context) -> {
            for (Block block : BuiltInRegistries.BLOCK) {
                if (block instanceof Fuel fuel) {
                    builder.add(block, fuel.getFuelTime());
                }
            }
        });
    }
}
