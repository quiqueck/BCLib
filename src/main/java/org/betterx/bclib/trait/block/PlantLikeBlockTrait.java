package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import org.betterx.wover.block.api.BlockDefinition;
import org.betterx.wover.block.api.trait.BlockTrait;
import org.betterx.wover.block.api.trait.BlockTraitKey;
import org.betterx.wover.block.api.trait.BlockTraits;
import org.betterx.wover.block.api.trait.Combiner;
import org.betterx.wover.block.api.trait.GenericBlockTrait;
import org.betterx.wover.block.impl.trait.BlockTraitImpl;
import org.betterx.wover.tag.api.predefined.CommonBlockTags;

import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * The trait replacement for the {@code BehaviourPlantLike} marker interface.
 * <p>
 * On its own this is a pure runtime marker: it carries no state, adds no tag and - crucially - sets
 * <b>no block property</b>. Its only job is to answer
 * {@code BlockTrait.hasRuntimeTrait(block, PlantLikeBlockTrait.KEY)}, which is what
 * {@code BehaviourPlantLike.TAB_PREDICATE} uses to decide whether a block belongs in the creative
 * "Plants"/nature tab of BetterNether and BetterEnd.
 * <p>
 * <b>Why this is not {@link PlantBlockTrait}.</b> {@link PlantBlockTrait} would also identify a block as
 * plant-like, but its {@code configure()} forces {@code mapColor}/{@code instabreak}/{@code sound}/
 * {@code noCollission}/{@code noOcclusion}/{@code pushReaction} onto the definition. A trait's
 * {@code configure()} runs inside {@code BlockDefinition.build()} and appends to {@code propertySetters},
 * which are applied <em>after</em> both the chain setters and the eager
 * {@code replacePropertiesWithCopy()} - so a trait beats them all regardless of call order, and only the
 * block constructor can override it. Adding {@link PlantBlockTrait} to an existing block therefore silently
 * restates it (e.g. {@code instabreak()} would zero BetterNether's barrel_cactus 0.4, nether_cactus 0.4 and
 * orange_mushroom 0.5). This trait exists so a block can be marked plant-like without any such side effect.
 * <p>
 * The static factories below bundle the marker with the other traits that each retired {@code Behaviour*}
 * interface used to imply, so a consumer can drop the interface and add one trait list instead. Every
 * constituent is property-free - see the {@code configure()} of {@link VegetationTagTrait},
 * {@link CompostableBlockTrait} (adds only the compostable item tag) and wover's
 * {@code MineableWithTagBuilder.Trait} (adds only its tool tag).
 * <p>
 * These are methods rather than constants on purpose: {@code BlockTraits.MINEABLE_WITH.needs*()} returns
 * {@code null} outside datagen, so a constant would capture that {@code null} once, at class-init.
 * {@link Combiner} drops the {@code null}s, which is why the lists are shorter outside datagen.
 */
public class PlantLikeBlockTrait extends BlockTraitImpl<Block, GenericBlockTrait> implements GenericBlockTrait {
    public static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "plant_like");

    private static final PlantLikeBlockTrait INSTANCE = new PlantLikeBlockTrait();

    /**
     * The bare marker, replacing a plain {@code BehaviourPlantLike}: puts the block in the creative nature
     * tab and does nothing else.
     */
    public static PlantLikeBlockTrait withDefault() {
        return INSTANCE;
    }

    /**
     * Replaces {@code BehaviourPlant} ({@code AddMineableHoe + BehaviourCompostable + BehaviourPlantLike}):
     * the nature-tab marker, {@code mineable/hoe}, the compostable item tag at the 0.1f default chance, and
     * {@link CommonBlockTags#PLANT} via {@link VegetationTagTrait#plant()}.
     */
    public static List<BlockTrait<?, ?>> plant() {
        return Combiner.of(
                INSTANCE,
                BlockTraits.MINEABLE_WITH.needsHoe(),
                CompostableBlockTrait.withDefault(),
                VegetationTagTrait.plant()
        ).combine();
    }

    /**
     * Replaces {@code BehaviourLeaves} ({@code AddMineableShears + AddMineableHoe + BehaviourCompostable}):
     * {@code mineable/shears}, {@code mineable/hoe}, the compostable item tag at {@code BehaviourLeaves}'
     * own 0.3f chance (not the 0.1f default), and the leaves block/item tags via
     * {@link VegetationTagTrait#leaves()}.
     * <p>
     * {@code BehaviourLeaves} is not itself {@code BehaviourPlantLike}, but {@code TAB_PREDICATE} matched it
     * explicitly, so the marker is included here to keep leaves in the nature tab after migration.
     * <p>
     * Not to be confused with {@link LeavesBlockTrait#withDefault()}, which is the full "build me a leaves
     * block" bundle (strength, sound, models, loot, flammability, {@link PlantBlockTrait}); this one only
     * reproduces what the marker interface contributed.
     */
    public static List<BlockTrait<?, ?>> leaves() {
        return Combiner.of(
                INSTANCE,
                BlockTraits.MINEABLE_WITH.needsShears(),
                BlockTraits.MINEABLE_WITH.needsHoe(),
                CompostableBlockTrait.withChance(0.3f),
                VegetationTagTrait.leaves()
        ).combine();
    }

    /**
     * Replaces {@code BehaviourSapling} ({@code AddMineableHoe + BehaviourCompostable + BehaviourPlantLike +
     * BehaviourSaplingLike}): {@link #plant()}'s marker/hoe/compostable, but with the sapling tags from
     * {@link VegetationTagTrait#sapling()} instead of {@link CommonBlockTags#PLANT} - {@code BehaviourSapling}
     * is not a {@code BehaviourPlant}, so it never received the plant tag.
     * <p>
     * Not to be confused with {@link SaplingBlockTrait#withDefault()}, which additionally forces properties,
     * models and loot.
     */
    public static List<BlockTrait<?, ?>> sapling() {
        return Combiner.of(
                INSTANCE,
                BlockTraits.MINEABLE_WITH.needsHoe(),
                CompostableBlockTrait.withDefault(),
                VegetationTagTrait.sapling()
        ).combine();
    }

    /**
     * Replaces {@code BehaviourSeed} ({@code AddMineableHoe + BehaviourCompostable + BehaviourPlantLike +
     * BehaviourSeedLike}): marker, {@code mineable/hoe}, compostable, and the seed block/item tags via
     * {@link VegetationTagTrait#seed()}. Like {@code BehaviourSapling}, {@code BehaviourSeed} was not a
     * {@code BehaviourPlant} and so never received {@link CommonBlockTags#PLANT}.
     */
    public static List<BlockTrait<?, ?>> seed() {
        return Combiner.of(
                INSTANCE,
                BlockTraits.MINEABLE_WITH.needsHoe(),
                CompostableBlockTrait.withDefault(),
                VegetationTagTrait.seed()
        ).combine();
    }

    private PlantLikeBlockTrait() {
    }

    @Override
    public BlockTraitKey key() {
        return KEY;
    }

    /**
     * Stored as a runtime trait so {@code hasRuntimeTrait(PlantLikeBlockTrait.KEY)} can identify the block,
     * the same way {@link SurvivesOnBlockTrait} and {@link VegetationTagTrait} do.
     */
    @Override
    public GenericBlockTrait forRuntime() {
        return this;
    }

    /**
     * Intentionally empty: this marker must never touch the definition. Overridden (rather than inherited
     * from {@code BlockTraitImpl}) to make that contract explicit and greppable - see the class javadoc.
     */
    @Override
    public void configure(BlockDefinition<Block, ? extends BlockDefinition<Block, ?>> definition) {
    }
}
