package org.betterx.bclib.api.v2.datafixer;

import de.ambertation.wunderlib.utils.Version;
import de.ambertation.wover.core.api.ModCore;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guards {@link MigrationProfile#replaceStringFromIDs(CompoundTag, String)}, the single place where
 * every ID replacement in the world fixer goes through.
 * <p>
 * {@link DataFixerAPI} walks data whose layout it does not know - backpack files belonging to a
 * third-party storage mod, and the {@code structures} block of a chunk - looking for an {@code id}
 * key anywhere below the root. A key of that name is not necessarily a registry ID: Waystones keeps
 * the UUID of a bound waystone in {@code id} as an int-array, and hitting one used to throw out of
 * the whole file, mark the migration as failed and thereby skip recording the applied patch level
 * for the entire world.
 */
class MigrationProfileIDTest {
    private static final String OLD_ID = "bclib_test:old_id";
    private static final String NEW_ID = "bclib_test:new_id";

    private static MigrationProfile profile;

    @BeforeAll
    static void registerTestPatch() {
        DataFixerAPI.registerPatch(TestPatch::new);
        // applyAll, so the patch contributes its replacements without a world patch level to compare
        // against.
        profile = new MigrationProfile(new CompoundTag(), true);
    }

    @Test
    void replacesAKnownStringID() {
        final CompoundTag tag = new CompoundTag();
        tag.putString("id", OLD_ID);

        assertTrue(profile.replaceStringFromIDs(tag, "id"), "a mapped ID should report a change");
        assertEquals(NEW_ID, tag.getString("id").orElseThrow());
    }

    @Test
    void leavesAnUnmappedStringIDAlone() {
        final CompoundTag tag = new CompoundTag();
        tag.putString("id", "minecraft:shulker_shell");

        assertFalse(profile.replaceStringFromIDs(tag, "id"));
        assertEquals("minecraft:shulker_shell", tag.getString("id").orElseThrow());
    }

    @Test
    void leavesAMissingKeyAlone() {
        assertFalse(profile.replaceStringFromIDs(new CompoundTag(), "id"));
    }

    /**
     * The regression: {@code id} holding a UUID rather than a registry ID, as Waystones writes it
     * into the {@code waystones:bound_scroll} component of a scroll. This used to throw
     * {@code NoSuchElementException: No value present}.
     */
    @Test
    void leavesANonStringIDAlone() {
        final CompoundTag tag = new CompoundTag();
        final int[] uuid = UUIDToIntArray(UUID.fromString("f9baf266-edcf-4bc5-a99e-88fbe8166040"));
        tag.put("id", new IntArrayTag(uuid));

        assertFalse(
                assertDoesNotThrow(() -> profile.replaceStringFromIDs(tag, "id")),
                "a non-string value is not ours to replace"
        );
        assertArrayEquals(uuid, tag.getIntArray("id").orElseThrow());
    }

    private static int[] UUIDToIntArray(UUID uuid) {
        final long most = uuid.getMostSignificantBits();
        final long least = uuid.getLeastSignificantBits();
        return new int[]{(int) (most >> 32), (int) most, (int) (least >> 32), (int) least};
    }

    private static class TestPatch extends Patch {
        TestPatch() {
            super(ModCore.create("bclib_test"), new Version(1, 0, 0));
        }

        @Override
        public Map<String, String> getIDReplacements() {
            return Map.of(OLD_ID, NEW_ID);
        }
    }
}
