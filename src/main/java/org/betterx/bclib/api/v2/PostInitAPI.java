package org.betterx.bclib.api.v2;

import org.betterx.bclib.interfaces.PostInitable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.function.Consumer;

// Composter-chance registration, furnace block-entity registration, and the potion crafting-
// remainder fix used to live here too - they've been migrated to CompostableBlockTrait,
// BlockTraits.VALID_BLOCK_ENTITY, and BCLib.onInitialize() respectively, since none of them
// actually needed to wait for "everyone else is done"; they can run inline, at each block's own
// registration (or, for the potion fix, immediately at BCLib's own init).
public class PostInitAPI {
    private static List<Consumer<Boolean>> postInitFunctions = Lists.newArrayList();
    private static List<TagKey<Block>> blockTags = Lists.newArrayList();
    private static List<TagKey<Item>> itemTags = Lists.newArrayList();

    /**
     * Register a new function which will be called after all mods are initiated. Will be called on both client and server.
     *
     * @param function {@link Consumer} with {@code boolean} parameter ({@code true} for client, {@code false} for server).
     */
    public static void register(Consumer<Boolean> function) {
        postInitFunctions.add(function);
    }

    /**
     * Called in proper BCLib entry points, for internal usage only.
     *
     * @param isClient {@code boolean}, {@code true} for client, {@code false} for server.
     */
    public static void postInit(boolean isClient) {
        BuiltInRegistries.BLOCK.forEach(block -> {
            processBlockCommon(block);
            if (isClient) {
                processBlockClient(block);
            }
        });

        if (postInitFunctions != null) {
            postInitFunctions.forEach(function -> function.accept(isClient));
            postInitFunctions = null;
        }
        blockTags = null;
        itemTags = null;
    }

    @Environment(EnvType.CLIENT)
    private static void processBlockClient(Block block) {
    }

    private static void processBlockCommon(Block block) {
        if (block instanceof PostInitable) {
            ((PostInitable) block).postInit();
        }
    }
}
