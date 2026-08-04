package org.betterx.bclib;

import de.ambertation.wunderlib.utils.Version;
import org.betterx.bclib.api.v2.datafixer.DataFixerAPI;
import org.betterx.bclib.api.v2.datafixer.MigrationProfile;
import org.betterx.bclib.api.v2.datafixer.Patch;
import org.betterx.bclib.interfaces.PatchFunction;

import net.minecraft.nbt.CompoundTag;

import java.util.Map;

public final class BCLibPatch {
    public static void register() {
        DataFixerAPI.registerPatch(SignPatch::new);
        DataFixerAPI.registerPatch(WoverNamespacePatch::new);
    }
}

/**
 * Moves the IDs that were left behind when the world-generation code was split out of BCLib into
 * WorldWeaver, and the recipes that moved from BCLib to BetterNether.
 */
class WoverNamespacePatch extends Patch {
    /** The namespace these IDs used before WorldWeaver existed. */
    private static final String LEGACY_NAMESPACE = "bclib:";
    /** The namespace the world-generation types live in today. */
    private static final String WOVER_NAMESPACE = "wover:";

    public WoverNamespacePatch() {
        super(BCLib.C, new Version(21, 8, 5));
    }

    @Override
    public Map<String, String> getIDReplacements() {
        return Map.<String, String>ofEntries(
                // The jigsaw pool element type moved to WorldWeaver. It is referenced from the
                // structure starts stored in chunks.
                Map.entry("bclib:single_end_pool_element", "wover:single_end_pool_element"),

                // The "unlock a vanilla recipe through a tag" recipes moved to BetterNether. They
                // are referenced from every player's recipe book.
                Map.entry("bclib:tag_bucket", "betternether:tag_bucket"),
                Map.entry("bclib:tag_cauldron", "betternether:tag_cauldron"),
                Map.entry("bclib:tag_compass", "betternether:tag_compass"),
                Map.entry("bclib:tag_hopper", "betternether:tag_hopper"),
                Map.entry("bclib:tag_minecart", "betternether:tag_minecart"),
                Map.entry("bclib:tag_piston", "betternether:tag_piston"),
                Map.entry("bclib:tag_rail", "betternether:tag_rail"),
                Map.entry("bclib:tag_shield", "betternether:tag_shield"),
                Map.entry("bclib:tag_shulker_box", "betternether:tag_shulker_box"),
                Map.entry("bclib:tag_smith_table", "betternether:tag_smith_table"),
                Map.entry("bclib:tag_stonecutter", "betternether:tag_stonecutter"),

                // Modded chests and barrels use the vanilla block entity now (see wover's
                // Chest/ChestBlockBuilder/BarrelBlockBuilder, which pass BlockEntityType.CHEST and
                // BlockEntityType.BARREL), so BCLib stopped registering its own. Without this the
                // game reports "Unknown type bclib:chest in 'block_entity'" and every modded chest
                // and barrel loses its contents. Same shape as the bclib:sign fix below.
                Map.entry("bclib:chest", "minecraft:chest"),
                Map.entry("bclib:barrel", "minecraft:barrel")

                // Deliberately not mapped: bclib:template_piece, bclib:furnace and bclib:chair are
                // all still registered by BCLib itself.
        );
    }

    @Override
    public PatchFunction<CompoundTag, Boolean> getLevelDatPatcher() {
        return WoverNamespacePatch::patchLevelDat;
    }

    /**
     * Rewrites the chunk-generator, biome-source and noise-settings IDs stored in
     * <i>level.dat</i> from the {@code bclib} namespace to {@code wover}.
     * <p>
     * A world created before the WorldWeaver split records {@code bclib:betterx} and
     * {@code bclib:end_biome_source} / {@code bclib:nether_biome_source} in
     * {@code Data.WorldGenSettings.dimensions}. Neither ID is registered any more, so the
     * dimension fails to decode and the world does not load. Dimensions contributed by other
     * mods are untouched, because only IDs in the legacy namespace are rewritten.
     *
     * @param root    The contents of level.dat
     * @param profile The active migration profile
     * @return {@code true} if any ID was rewritten
     */
    private static Boolean patchLevelDat(CompoundTag root, MigrationProfile profile) {
        final CompoundTag dimensions = root
                .getCompound("Data")
                .flatMap(data -> data.getCompound("WorldGenSettings"))
                .flatMap(settings -> settings.getCompound("dimensions"))
                .orElse(null);
        if (dimensions == null) return false;

        boolean changed = false;
        for (String dimensionKey : dimensions.keySet()) {
            final CompoundTag generator = dimensions
                    .getCompound(dimensionKey)
                    .flatMap(stem -> stem.getCompound("generator"))
                    .orElse(null);
            if (generator == null) continue;

            changed |= migrateLegacyID(generator, "type");
            changed |= migrateLegacyID(generator, "settings");

            final CompoundTag biomeSource = generator.getCompound("biome_source").orElse(null);
            if (biomeSource != null) changed |= migrateLegacyID(biomeSource, "type");
        }

        return changed;
    }

    /**
     * Moves a single ID from the legacy namespace into the current one, if it is in the legacy
     * namespace at all.
     *
     * @param tag The compound holding the ID
     * @param key The key of the ID within {@code tag}
     * @return {@code true} if the ID was rewritten
     */
    private static boolean migrateLegacyID(CompoundTag tag, String key) {
        final String id = tag.getString(key).orElse(null);
        if (id == null || !id.startsWith(LEGACY_NAMESPACE)) return false;

        final String migrated = WOVER_NAMESPACE + id.substring(LEGACY_NAMESPACE.length());
        BCLib.LOGGER.warn("Replacing generator ID '{}' with '{}'.", id, migrated);
        tag.putString(key, migrated);
        return true;
    }
}

class SignPatch extends Patch {
    public SignPatch() {
        super(BCLib.C, new Version(3, 0, 11));
    }

    @Override
    public Map<String, String> getIDReplacements() {
        return Map.ofEntries(
                Map.entry("bclib:sign", "minecraft:sign")
        );
    }
}
