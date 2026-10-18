package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.trait.TraitLists;
import de.ambertation.wover.block.api.render.BlockRenderTraits;
import de.ambertation.wover.block.api.trait.BlockTrait;
import de.ambertation.wover.block.api.trait.BlockTraitKey;
import de.ambertation.wover.block.api.trait.BlockTraits;
import de.ambertation.wover.block.api.trait.GenericBlockTrait;
import de.ambertation.wover.block.impl.trait.BlockTraitImpl;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.List;

/**
 * Marks a block as a mushroom. From Minecraft 26.3 on it behaves like vanilla's shelf mushroom (its item joins
 * {@code #minecraft:mushrooms} and it uses the shelf mushroom sounds); this version has neither, so the trait
 * only marks the block and keeps the plant sounds.
 * <p>
 * The sound cannot ride on this trait: {@link PlantBlockTrait} sets the sound in its own {@code configure()},
 * which would override (or be overridden by) a second trait depending on the order they were added. The
 * factories below therefore hand {@link #SOUND} to the plant trait they bundle.
 */
public class MushroomBlockTrait extends BlockTraitImpl<Block, GenericBlockTrait> implements GenericBlockTrait {
    public static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "mushroom");
    /** Vanilla's shelf mushroom sounds only exist from 26.3 on, so this is the regular plant sound. */
    public static final SoundType SOUND = SoundType.GRASS;
    private static final MushroomBlockTrait DEFAULT = new MushroomBlockTrait();

    public static MushroomBlockTrait withDefault() {
        return DEFAULT;
    }

    /**
     * A mushroom growing on a wall: a non-walkable, grid-aligned {@link PlantBlockTrait} with the mushroom
     * sounds, plus this marker.
     *
     * @param color            the map color
     * @param validSpawnAlways whether mobs may always spawn on the block (see {@link PlantBlockTrait})
     */
    public static List<BlockTrait<?, ?>> wallMushroom(MapColor color, boolean validSpawnAlways) {
        return List.of(
                PlantBlockTrait.withColor(color, false, BlockBehaviour.OffsetType.NONE, SOUND, validSpawnAlways),
                DEFAULT
        );
    }

    /**
     * {@link #wallMushroom} with the rest of {@link PlantBlockTrait#compostableWithColor}: compostable,
     * cutout and dropping itself.
     *
     * @param color the map color
     */
    public static List<BlockTrait<?, ?>> compostableWallMushroom(MapColor color) {
        return TraitLists.and(
                wallMushroom(color, false),
                CompostableBlockTrait.withDefault(),
                BlockRenderTraits.RENDER_LAYER.cutout(),
                BlockTraits.LOOT_TABLE.dropSelf()
        );
    }

    private MushroomBlockTrait() {
    }

    @Override
    public BlockTraitKey key() {
        return KEY;
    }

    @Override
    public GenericBlockTrait forRuntime() {
        return this;
    }
}
