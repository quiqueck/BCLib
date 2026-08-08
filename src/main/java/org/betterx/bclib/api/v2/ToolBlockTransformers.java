package org.betterx.bclib.api.v2;

import org.betterx.bclib.interfaces.tools.AxeCanStrip;

import net.minecraft.core.Direction;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.BlockTransformerMappings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.CopyPropertiesProvider;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.ApiStatus;

/**
 * Teaches every axe/shovel about the block conversions BCLib declares.
 * <p>
 * This replaces two things BCLib carried up to 26.1:
 * <ul>
 *     <li>{@code AxeItemMixin}, which injected into {@code AxeItem#getStripped} and answered
 *     "what does this block strip to?" from {@link AxeCanStrip}.</li>
 *     <li>{@code ShovelItemAccessor}, which handed out {@code ShovelItem.FLATTENABLES} so
 *     {@link ShovelAPI} could put entries into it.</li>
 * </ul>
 * 26.3 deleted {@code AxeItem}/{@code ShovelItem} entirely - tools are no longer {@link Item}
 * subclasses, they are built from {@code Item.Properties#axe}/{@code #shovel} - and collapsed
 * stripping, path-making, tilling, scraping and waxing into a single data-driven
 * {@code minecraft:block_transformer} item component. {@code BlockTransformer#transformBlock} walks
 * that component's {@link BlockTransformer.BlockTransformData} list and applies the first entry whose
 * {@link BlockPredicate} matches the clicked block.
 * <p>
 * So the 26.3 answer is not "inject into stripping/flattening" but "append our entries to the list the
 * tool already carries". This mirrors WorldWeaver's {@code StripableBlockTransformers} (wover-sets-api),
 * which solved the same problem for its {@code StripableBlockTrait}.
 * <h2>Which items are modified</h2>
 * An item counts as an axe (resp. shovel) if its block transformer contains all of
 * {@link BlockTransformerMappings#AXE}'s (resp. {@code SHOVEL}'s) entries. That is exactly the set of
 * items built through {@code Item.Properties#axe}/{@code #shovel} - vanilla's plus any modded ones -
 * and because the test is "contains" rather than "equals" it keeps working when another mod has already
 * appended its own entries.
 */
@ApiStatus.Internal
public final class ToolBlockTransformers {
    private ToolBlockTransformers() {
    }

    /**
     * Registers the listener. Called once from {@code BCLib#onInitialize}.
     */
    public static void register() {
        DefaultItemComponentEvents.MODIFY.register(context -> {
            final List<BlockTransformer.BlockTransformData> axeTransforms = axeTransforms();
            if (!axeTransforms.isEmpty()) {
                context.modify(
                        ToolBlockTransformers::isAxe,
                        (builder, item) -> append(builder, item, axeTransforms)
                );
            }

            final List<BlockTransformer.BlockTransformData> shovelTransforms = shovelTransforms();
            if (!shovelTransforms.isEmpty()) {
                context.modify(
                        ToolBlockTransformers::isShovel,
                        (builder, item) -> append(builder, item, shovelTransforms)
                );
            }
        });
    }

    private static void append(
            net.minecraft.core.component.DataComponentMap.Builder builder,
            Item item,
            List<BlockTransformer.BlockTransformData> extra
    ) {
        final BlockTransformer current = item.components().get(DataComponents.BLOCK_TRANSFORMER);
        if (current == null) return;

        final List<BlockTransformer.BlockTransformData> merged = new ArrayList<>(current.transforms());
        merged.addAll(extra);
        builder.set(DataComponents.BLOCK_TRANSFORMER, new BlockTransformer(merged));
    }

    /**
     * One entry per {@link AxeCanStrip} block, shaped like vanilla's own strippables
     * ({@code BlockTransformerMappings#getStrippableBlockData}): {@code matches_blocks} on the source
     * block, a {@link CopyPropertiesProvider} onto the stripped block, and the
     * {@code minecraft:item.axe.strip} sound.
     * <p>
     * <b>Behaviour note.</b> {@link AxeCanStrip#strippedState(BlockState)} is an arbitrary Java function
     * of the clicked state, which a serialized, client-synced component cannot express: 26.3 offers no
     * {@link BlockPredicate} that matches an exact {@link BlockState}. The function is therefore
     * evaluated once, against the block's default state, and the resulting block is paired with a
     * {@link CopyPropertiesProvider} so that every property the two blocks share is carried over (this is
     * what covers {@code RotatedPillarBlock.AXIS} for logs, and it is exactly what vanilla does). An
     * implementation whose result <em>block</em> differs per state - rather than only its property values
     * - will now always yield the default state's block.
     */
    private static List<BlockTransformer.BlockTransformData> axeTransforms() {
        final List<BlockTransformer.BlockTransformData> result = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (!(block instanceof AxeCanStrip stripable)) continue;

            final BlockState stripped = stripable.strippedState(block.defaultBlockState());
            if (stripped == null) continue;

            result.add(BlockTransformer.BlockTransformData
                    .builder(
                            BlockPredicate.matchesBlocks(block),
                            new CopyPropertiesProvider(stripped.getBlock())
                    )
                    .sound(SoundEvents.AXE_STRIP)
                    .build()
            );
        }
        return result;
    }

    /**
     * One entry per {@link ShovelAPI#addShovelBehaviour} registration, shaped like vanilla's single
     * shovel entry: the target block with air above it, converted to the requested state, with the
     * {@code minecraft:item.shovel.flatten} sound and the bottom face disallowed.
     * <p>
     * The air-above and not-from-below constraints reproduce the checks 26.1's {@code ShovelItem#useOn}
     * applied to every {@code FLATTENABLES} entry.
     */
    private static List<BlockTransformer.BlockTransformData> shovelTransforms() {
        final List<BlockTransformer.BlockTransformData> result = new ArrayList<>();
        for (Map.Entry<Block, BlockState> e : ShovelAPI.flattenables().entrySet()) {
            result.add(BlockTransformer.BlockTransformData
                    .builder(
                            BlockPredicate.allOf(
                                    BlockPredicate.matchesBlocks(e.getKey()),
                                    BlockPredicate.matchesTag(Direction.UP, BlockTags.AIR)
                            ),
                            BlockStateProvider.simple(e.getValue())
                    )
                    .sound(SoundEvents.SHOVEL_FLATTEN)
                    .disallowedFaces(List.of(Direction.DOWN))
                    .build()
            );
        }
        return result;
    }

    private static boolean isAxe(Item item) {
        return carriesAllOf(item, BlockTransformerMappings.AXE);
    }

    private static boolean isShovel(Item item) {
        return carriesAllOf(item, BlockTransformerMappings.SHOVEL);
    }

    private static boolean carriesAllOf(Item item, BlockTransformer reference) {
        final BlockTransformer transformer = item.components().get(DataComponents.BLOCK_TRANSFORMER);
        return transformer != null && transformer.transforms().containsAll(reference.transforms());
    }
}
