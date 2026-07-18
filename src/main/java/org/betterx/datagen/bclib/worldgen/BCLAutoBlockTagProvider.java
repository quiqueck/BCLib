package org.betterx.datagen.bclib.worldgen;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.behaviours.interfaces.*;
import org.betterx.bclib.interfaces.Fuel;
import org.betterx.bclib.interfaces.tools.*;
import org.betterx.wover.block.api.BlockRegistry;
import org.betterx.wover.core.api.ModCore;
import org.betterx.wover.datagen.api.WoverAutoProvider;
import org.betterx.wover.datagen.api.WoverTagProvider;
import org.betterx.wover.tag.api.TagRegistry;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;
import org.betterx.wover.tag.api.event.context.TagElementWrapper;
import org.betterx.wover.tag.api.predefined.CommonBlockTags;
import org.betterx.wover.tag.api.predefined.MineableTags;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.registry.FuelRegistryEvents;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;


public class BCLAutoBlockTagProvider extends WoverTagProvider.ForBlocks implements WoverAutoProvider {
    public BCLAutoBlockTagProvider(ModCore modCore) {
        super(modCore);
    }

    /**
     * The tool tags that count as "this block declares how it is mined". These are exactly the tags the
     * {@code AddMineable*} marker interfaces above contribute; the {@code NEEDS_*_TOOL} tier tags in
     * {@link MineableTags} are deliberately not included, as they say which tier is needed, not which tool.
     */
    private static final Set<TagKey<Block>> TOOL_TAGS = Set.of(
            MineableTags.AXE,
            MineableTags.HOE,
            MineableTags.PICKAXE,
            MineableTags.SHEARS,
            MineableTags.SHOVEL,
            MineableTags.SWORD,
            MineableTags.HAMMER
    );

    /**
     * Records which blocks of a {@link BlockRegistry} declared one of the {@link #TOOL_TAGS} when they were
     * registered - i.e. through {@code BlockDefinition.addTags(...)}, whether that call came from a
     * {@link org.betterx.wover.block.api.trait.BlockTraits#MINEABLE_WITH} trait's {@code configure()}, from a
     * plain {@code addTags(BlockTags.MINEABLE_WITH_AXE)}, or from a {@code BlockTagProvider}.
     * <p>
     * This is a throw-away {@link TagBootstrapContext} that only collects: it is handed to the registry's
     * public {@link BlockRegistry#bootstrapBlockTags(TagBootstrapContext)} instead of the real datagen
     * context, so reading the declarations has no effect whatsoever on the tags that get written.
     * <p>
     * Note that the {@code MINEABLE_WITH} trait cannot be observed on the finished block via
     * {@code BlockTrait.hasRuntimeTrait(block, BlockTraits.MINEABLE_WITH.key())}: wover's
     * {@code MineableWithTagBuilder.Trait} does not override {@code forRuntime()}, so it inherits
     * {@code BlockTraitImpl}'s {@code null} and {@code BlockDefinition.build()} never stores it on the block.
     * The tag it contributes is its only observable effect, which is what this collector reads.
     */
    private static final class DeclaredTools implements TagBootstrapContext<Block> {
        private final Set<Block> blocksWithTool = new HashSet<>();
        private final Set<ResourceKey<Block>> keysWithTool = new HashSet<>();

        static DeclaredTools of(BlockRegistry registry) {
            final DeclaredTools collector = new DeclaredTools();
            registry.bootstrapBlockTags(collector);
            return collector;
        }

        boolean declaresTool(Block block) {
            if (blocksWithTool.contains(block)) return true;
            if (keysWithTool.isEmpty()) return false;
            return BuiltInRegistries.BLOCK
                    .getResourceKey(block)
                    .map(keysWithTool::contains)
                    .orElse(false);
        }

        private void record(TagKey<Block> tagID, Block[] elements) {
            if (!TOOL_TAGS.contains(tagID)) return;
            for (Block element : elements) {
                if (element != null) blocksWithTool.add(element);
            }
        }

        private void record(Block element, TagKey<Block>[] tags) {
            if (element == null) return;
            for (TagKey<Block> tag : tags) {
                if (TOOL_TAGS.contains(tag)) {
                    blocksWithTool.add(element);
                    return;
                }
            }
        }

        private void record(TagKey<Block> tagID, ResourceKey<Block>[] keys) {
            if (!TOOL_TAGS.contains(tagID)) return;
            for (ResourceKey<Block> key : keys) {
                if (key != null) keysWithTool.add(key);
            }
        }

        @Override
        public void add(TagKey<Block> tagID, Block... elements) {
            record(tagID, elements);
        }

        @Override
        public void add(Block element, TagKey<Block>... tags) {
            record(element, tags);
        }

        @Override
        public void add(TagKey<Block> tagID, ResourceKey<Block>... keys) {
            record(tagID, keys);
        }

        @Override
        public void addOptional(TagKey<Block> tagID, Block... elements) {
            record(tagID, elements);
        }

        @Override
        public void addOptional(Block element, TagKey<Block>... tags) {
            record(element, tags);
        }

        @Override
        public void addOptional(TagKey<Block> tagID, ResourceKey<Block>... keys) {
            record(tagID, keys);
        }

        // Tag-into-tag nesting cannot make an individual block declare a tool, so it is not tracked.
        @Override
        public void add(TagKey<Block> tagID, TagKey<Block>... tags) {
        }

        @Override
        public void addOptional(TagKey<Block> tagID, TagKey<Block>... tags) {
        }

        @Override
        public void asPlaceholder(TagKey<Block> tagID) {
        }

        @Override
        public void forEach(BiConsumer<TagKey<Block>, List<TagElementWrapper<Block>>> consumer) {
        }

        @Override
        public TagRegistry<Block, ? extends TagBootstrapContext<Block>> registry() {
            return null;
        }
    }

    private static void processBlockCommon(
            TagBootstrapContext<Block> context,
            Block block,
            DeclaredTools declaredTools
    ) {
        if (!(block instanceof PreventMineableAdd)) {
            if (block instanceof AddMineableShears) {
                context.add(block, MineableTags.SHEARS);
            }
            if (block instanceof AddMineableAxe) {
//                if (!context.contains(BlockTags.WOODEN_DOORS, block)
//                        && !context.contains(BlockTags.WOODEN_BUTTONS, block)
//                        && !context.contains(BlockTags.WOODEN_SLABS, block)
//                        && !context.contains(BlockTags.WOODEN_FENCES, block)
//                        && !context.contains(BlockTags.WOODEN_STAIRS, block)
//                        && !context.contains(BlockTags.WOODEN_PRESSURE_PLATES, block)
//                        && !context.contains(BlockTags.WOODEN_TRAPDOORS, block)
//                        && !context.contains(CommonBlockTags.WOODEN_BARREL, block)
//                        && !context.contains(CommonBlockTags.WOODEN_CHEST, block)
//                        && !context.contains(CommonBlockTags.WOODEN_COMPOSTER, block)
//                        && !context.contains(CommonBlockTags.WORKBENCHES, block)
//                        && !context.contains(BlockTags.SIGNS, block)
//                        && !context.contains(BlockTags.PLANKS, block)
//                        && !context.contains(BlockTags.LOGS, block)
//                        && !context.contains(BlockTags.FENCE_GATES, block)
//                        && !context.contains(BlockTags.ALL_HANGING_SIGNS, block)
//                        && !context.contains(CommonBlockTags.WORKBENCHES, block)
//                        && !context.contains(org.betterx.wover.tag.api.predefined.CommonBlockTags.BOOKSHELVES, block)
//                ) {
                context.add(block, MineableTags.AXE);
//                }
            }
            if (block instanceof AddMineablePickaxe) {
                context.add(block, MineableTags.PICKAXE);
            }
            if (block instanceof AddMineableShovel) {
                context.add(block, MineableTags.SHOVEL);
            }
            if (block instanceof AddMineableHoe) {
                context.add(block, MineableTags.HOE);
            }
            if (block instanceof AddMineableSword) {
                context.add(block, MineableTags.SWORD);
            }
            if (block instanceof AddMineableHammer) {
                context.add(block, MineableTags.HAMMER);
            }
        }

        if (block instanceof BehaviourWaterPlantLike) {
            context.add(block, CommonBlockTags.WATER_PLANT);
        }

        if (block instanceof BehaviourPlant) {
            context.add(block, CommonBlockTags.PLANT);
        }

        if (block instanceof BehaviourVine) {
            context.add(block, CommonBlockTags.VINE);
        }

        if (block instanceof BehaviourSeedLike) {
            context.add(block, CommonBlockTags.SEEDS);
        }

        if (block instanceof BehaviourSaplingLike) {
            context.add(block, CommonBlockTags.SAPLINGS, BlockTags.SAPLINGS);
        }

        if (block instanceof BehaviourClimable c) {
            context.add(block, BlockTags.CLIMBABLE);
        }

        if (block instanceof BehaviourLeaves) {
            context.add(block, BlockTags.LEAVES, CommonBlockTags.LEAVES);
        }

        if (block instanceof BehaviourImmobile) {
            context.add(block, CommonBlockTags.IMMOBILE, BlockTags.DRAGON_IMMUNE);
        }

        if (block instanceof BehaviourObsidian) {
            context.add(block, CommonBlockTags.IS_OBSIDIAN, BlockTags.DRAGON_IMMUNE, BlockTags.NEEDS_DIAMOND_TOOL);
        }

        if (block instanceof BehaviourPortalFrame) {
            context.add(block, CommonBlockTags.NETHER_PORTAL_FRAME);
        }

        if (block instanceof Fuel fl) {
            FuelRegistryEvents.BUILD.register((builder, fuelContext) -> builder.add(block, fl.getFuelTime()));
        }

        final ResourceLocation location = BuiltInRegistries.BLOCK.getKey(block);
        if (!location.getNamespace().equals("minecraft")) {
            if (!(block instanceof HasMinableBehaviour)
                    && !declaredTools.declaresTool(block)
                    && block.defaultBlockState().requiresCorrectToolForDrops()) {
                BCLib.LOGGER.error("Block " + block + "(" + block.getClass() + ")" + " has no mineable behaviour!");
            }
        }
    }

    private static void processCommonBlockTags(TagBootstrapContext<Block> context, ModCore modCore) {
        BCLib.C.LOG.debug("Processing Blocks for " + modCore.namespace);
        final BlockRegistry registry = BlockRegistry.forMod(modCore);
        final DeclaredTools declaredTools = DeclaredTools.of(registry);
        registry
                .allBlocks()
                .forEach(block -> processBlockCommon(context, block, declaredTools));
    }

    @Override
    public void prepareTags(TagBootstrapContext<Block> context) {
        processCommonBlockTags(context, modCore);
    }
}
