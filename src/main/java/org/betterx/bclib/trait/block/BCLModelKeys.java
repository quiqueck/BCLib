package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import de.ambertation.wover.block.api.model.ModelKey;
import de.ambertation.wover.block.api.trait.BlockTraitKey;

/**
 * Creates the {@link ModelKey}s for BCLib's model shapes. Each shape declares its key next to the common factory
 * that binds it; the client generator behind the key is registered by
 * {@link org.betterx.bclib.client.models.BCLibClientModels} through the {@code wover.client.traits} entrypoint.
 */
public final class BCLModelKeys {
    private BCLModelKeys() {
    }

    public static <P> ModelKey<P> key(String path) {
        return new ModelKey<>(BlockTraitKey.ofUnique(BCLib.C, "model/" + path));
    }
}
