package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.trait.TraitLists;
import de.ambertation.wover.block.api.BlockDefinition;
import de.ambertation.wover.block.api.client.trait.ClientBlockTraits;
import de.ambertation.wover.block.api.trait.*;
import de.ambertation.wover.block.impl.trait.BlockTraitImpl;
import de.ambertation.wover.pottable.api.trait.PottablePlantBlockTrait;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.List;

public class PlantBlockTrait extends BlockTraitImpl<Block, GenericBlockTrait> implements GenericBlockTrait {
    public static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "plant");


    public static PlantBlockTrait withDefault() {
        return withColor(MapColor.PLANT, false);
    }

    public static PlantBlockTrait withColor(MapColor color) {
        return withColor(color, false);
    }

    // Standalone ground-plant entry point: enables the vanilla-style random horizontal (X-Z)
    // offset so plants don't render perfectly grid-aligned. Mirrors WaterPlantBlockTrait, which
    // already sets OffsetType.XZ for underwater plants. Composing traits that must stay
    // grid-aligned (leaves, vines) use the offsetType overload below to opt out.
    public static PlantBlockTrait withColor(MapColor color, boolean walkable) {
        return withColor(color, walkable, BlockBehaviour.OffsetType.XZ);
    }

    // Composition-facing overload: lets traits that embed PlantBlockTrait (e.g. LeavesBlockTrait,
    // VineBlockTrait) pass OffsetType.NONE to keep their full/cube-ish blocks grid-aligned.
    public static PlantBlockTrait withColor(MapColor color, boolean walkable, BlockBehaviour.OffsetType offsetType) {
        return withColor(color, walkable, offsetType, SoundType.GRASS, false);
    }

    // Cross-mod overload (category-traits fold): lets a caller override the sound (BetterNether's
    // netherPlant()/makeNetherGrass() presets use SoundType.CROP, not GRASS) and opt into the
    // "always a valid mob-spawn surface" predicate that makeNetherGrass() forces (vanilla
    // PlantBlockTrait leaves isValidSpawn at its BlockBehaviour.Properties default). Every other
    // overload delegates here with (GRASS, false) so existing call sites - and their goldens - are
    // untouched.
    public static PlantBlockTrait withColor(
            MapColor color,
            boolean walkable,
            BlockBehaviour.OffsetType offsetType,
            SoundType sound,
            boolean validSpawnAlways
    ) {
        return new PlantBlockTrait(color, walkable, offsetType, sound, validSpawnAlways);
    }

    public static List<BlockTrait<?, ?>> compostableWithColor(MapColor color, boolean walkable, boolean flammable) {
        return compostableWithColor(color, walkable, flammable, BlockBehaviour.OffsetType.XZ);
    }

    public static List<BlockTrait<?, ?>> compostableWithColor(MapColor color, boolean walkable, boolean flammable, BlockBehaviour.OffsetType offsetType) {
        return Combiner.of(
                withColor(color, walkable, offsetType),
                CompostableBlockTrait.withDefault(),
                flammable ? BlockTraits.FLAMMABLE.withDefault() : null,
                ClientBlockTraits.RENDER_LAYER.cutout(),
                // Thin grass/moss/fern/crop-style insta-break plants get NO mineable tag, matching vanilla
                // short_grass/fern/flowers/crops. (Leaves keep needsShears via LeavesBlockTrait; woody/terrain
                // blocks keep their tags via their own MINEABLE_WITH traits.)
                BlockTraits.LOOT_TABLE.dropSelf()
        ).combine();
    }

    /**
     * BE's "ground cross-plant" micro-fold (category-traits Batch 4): the sub-core repeated 17x across
     * {@code EndPlantBlocks}' single-cross ground plants (cave_grass, crystal_grass, shadow_plant,
     * bushy_grass, amber_grass, jungle_grass, blooming_cooksonia, salteago, vaiolush_fern, fracturn,
     * clawfern, globulagus, orango, aeridium, lutebus, lamellarium, inflexia) - all identical but for their
     * {@code SurvivesOnBlockTrait} target and cross model. Bundles {@link #compostableWithColor} at
     * {@code (MapColor.PLANT, false, true)}, {@link VegetationTagTrait#plant()} and
     * {@link PottablePlantBlockTrait#any()}.
     * <p>
     * Each call site's chained {@code .offsetType(XZ)} is redundant (this trait already forces XZ via
     * {@code withColor(color, false)}) and is dropped, not reproduced. {@code .replaceable()} is a genuine
     * setter this bundle does not provide and must stay chained at the call site.
     */
    public static List<BlockTrait<?, ?>> groundCrossPlant() {
        return TraitLists.and(
                compostableWithColor(MapColor.PLANT, false, true),
                VegetationTagTrait.plant(),
                PottablePlantBlockTrait.any()
        );
    }

    public final MapColor color;
    public final boolean walkable;
    public final BlockBehaviour.OffsetType offsetType;
    public final SoundType sound;
    public final boolean validSpawnAlways;

    private PlantBlockTrait(
            MapColor color,
            boolean walkable,
            BlockBehaviour.OffsetType offsetType,
            SoundType sound,
            boolean validSpawnAlways
    ) {
        this.color = color;
        this.walkable = walkable;
        this.offsetType = offsetType;
        this.sound = sound;
        this.validSpawnAlways = validSpawnAlways;
    }

    @Override
    public BlockTraitKey key() {
        return KEY;
    }

    // Stored as a runtime trait so hasRuntimeTrait(PlantBlockTrait.KEY) can identify plant-like blocks
    // (e.g. BehaviourPlantLike.TAB_PREDICATE for the creative "Plants" tab), replacing the old
    // BehaviourPlantLike/BehaviourLeaves interface checks.
    @Override
    public GenericBlockTrait forRuntime() {
        return this;
    }

    @Override
    public void configure(BlockDefinition<Block, ? extends BlockDefinition<Block, ?>> definition) {
        super.configure(definition);

        definition.mapColor(color)
                  .noOcclusion()
                  .instabreak()
                  .sound(sound)
                  .offsetType(offsetType)
                  .pushReaction(PushReaction.DESTROY);

        if (validSpawnAlways) {
            definition.isValidSpawn((state, world, pos, type) -> true);
        }

        if (!walkable) {
            definition.noCollission();
        } else if (offsetType != BlockBehaviour.OffsetType.NONE) {
            // A walkable plant keeps its collision shape; a block that has both a collision shape and a
            // non-NONE offset must be marked dynamicShape, or vanilla throws at registration:
            // "<block> has a collision shape and an offset type, but is not marked as dynamicShape".
            definition.dynamicShape();
        }
    }
}
