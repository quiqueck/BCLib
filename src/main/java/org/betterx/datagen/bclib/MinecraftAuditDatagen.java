package org.betterx.datagen.bclib;

import de.ambertation.wover.core.api.ModCore;
import de.ambertation.wover.datagen.api.PackBuilder;
import de.ambertation.wover.datagen.api.provider.BlockPropertiesProvider;
import de.ambertation.wover.datagen.api.provider.WoverBlockRegistrationsProvider;
import de.ambertation.wover.datagen.api.provider.WoverBlockShapesProvider;
import de.ambertation.wover.datagen.api.provider.WoverItemRegistrationsProvider;

/**
 * Produces the four audit snapshots - {@code block_properties.json}, {@code block_registrations.json},
 * {@code item_registrations.json} and {@code block_shapes.txt} - for the vanilla {@code minecraft}
 * namespace rather than for a mod.
 * <p>
 * The providers themselves filter by {@link ModCore#namespace}, so producing the vanilla snapshots only
 * takes handing them a {@code minecraft} {@link ModCore}. That used to mean editing a line in BetterEnd's
 * datagen entry point by hand, running datagen, and moving the four files out of BetterEnd's own generated
 * folder; this class exists so the same thing is a normal build invocation instead:
 *
 * <pre>./gradlew :runMinecraftAudit</pre>
 *
 * which writes straight into the shared vanilla reference checkout at {@code ../minecraft/src/main/generated}
 * (see the {@code minecraftAudit} run in bclib.gradle). The run is selected by the presence of the
 * {@link #PROPERTY} system property, so it can also be driven directly with
 * {@code -Dwover.datagen.minecraft-audit} on any datagen run.
 * <p>
 * A run in this mode generates <em>nothing but</em> these four files: see {@link BCLibDatagen}.
 */
public class MinecraftAuditDatagen {
    /**
     * Presence of this system property switches datagen over to the vanilla audit.
     * It takes no value - {@code -Dwover.datagen.minecraft-audit} is enough.
     */
    public static final String PROPERTY = "wover.datagen.minecraft-audit";

    private static final ModCore MINECRAFT = ModCore.create("minecraft");

    private MinecraftAuditDatagen() {
    }

    public static boolean isEnabled() {
        return System.getProperty(PROPERTY) != null;
    }

    /**
     * Registers the four audit providers, bound to the vanilla namespace instead of to the
     * mod whose entry point is running them.
     */
    public static void addProviders(PackBuilder globalPack) {
        globalPack.addProvider((m) -> new BlockPropertiesProvider(MINECRAFT));
        globalPack.addProvider((m) -> new WoverBlockRegistrationsProvider(MINECRAFT));
        globalPack.addProvider((m) -> new WoverItemRegistrationsProvider(MINECRAFT));
        globalPack.addProvider((m) -> new WoverBlockShapesProvider(MINECRAFT));
    }
}
