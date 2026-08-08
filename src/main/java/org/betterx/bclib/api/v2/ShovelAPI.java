package org.betterx.bclib.api.v2;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.ApiStatus;

public class ShovelAPI {
    /**
     * 26.3 deleted {@code ShovelItem} (and with it the static {@code ShovelItem.FLATTENABLES} map this
     * API used to write into); shovel path-making is now an entry in the data-driven
     * {@code minecraft:block_transformer} item component. Registrations are collected here and appended
     * to every shovel's transformer by {@link ToolBlockTransformers}.
     * <p>
     * Insertion order is preserved because vanilla evaluates the transformer list first-match-wins.
     */
    private static final Map<Block, BlockState> FLATTENABLES = new LinkedHashMap<>();

    /**
     * Will add left-click behaviour to shovel: when it is targeting cetrain {@link Block} it will be converting to new
     * {@link BlockState} on usage. Example: grass converting to path.
     * <p>
     * Must be called during mod initialization: the entries are read when item components are (re)built,
     * i.e. on every datapack reload.
     *
     * @param target  {@link Block} that will be converted.
     * @param convert {@link BlockState} to convert block into.
     */
    public static void addShovelBehaviour(Block target, BlockState convert) {
        FLATTENABLES.put(target, convert);
    }

    @ApiStatus.Internal
    static Map<Block, BlockState> flattenables() {
        return Collections.unmodifiableMap(FLATTENABLES);
    }
}
