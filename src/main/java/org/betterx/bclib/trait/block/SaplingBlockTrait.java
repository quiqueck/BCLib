package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import de.ambertation.wover.block.api.BlockDefinition;
import de.ambertation.wover.block.api.client.trait.BlockModelTrait;
import de.ambertation.wover.block.api.client.trait.ClientBlockTraits;
import de.ambertation.wover.block.api.trait.*;
import de.ambertation.wover.block.impl.trait.BlockTraitImpl;
import de.ambertation.wover.core.api.ModCore;
import de.ambertation.wover.tag.api.predefined.CommonBlockTags;
import de.ambertation.wover.tag.api.predefined.CommonItemTags;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;

public class SaplingBlockTrait extends BlockTraitImpl<Block, GenericBlockTrait> {
    private static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "sapling");

    public static List<BlockTrait<?, ?>> withDefault() {
        return withLight(0);
    }


    public static List<BlockTrait<?, ?>> withLight(int lightLevel) {
        return withColor(MapColor.PLANT, lightLevel);
    }

    public static List<BlockTrait<?, ?>> withColor(MapColor color) {
        return withColor(color, 0);
    }

    /**
     * The sapling's own behaviour on its own: random ticks, the sapling block/item tags, and the light
     * level - without the plant properties, model, loot and compostability that
     * {@link #withColor(MapColor, int)} bundles around it.
     * <p>
     * This is what makes a sapling <em>grow</em>. {@link org.betterx.bclib.blocks.FeatureSaplingBlock}
     * does all of its growing in {@code randomTick()}, and a block that never asked for random ticks is
     * never handed one - so a sapling registered without this still accepts bone meal and looks entirely
     * healthy while never growing on its own. Any registration that assembles its own trait list rather
     * than taking the {@link #withColor(MapColor, int)} bundle (BetterNether's wood-set sapling slot,
     * which brings its own model and survival rules) has to add this, or it registers a sapling that only
     * a player with bone meal can ever turn into a tree.
     */
    public static SaplingBlockTrait ticking() {
        return ticking(0);
    }

    /**
     * @param lightLevel the sapling's light emission; {@code 0} leaves the property untouched.
     * @see #ticking()
     */
    public static SaplingBlockTrait ticking(int lightLevel) {
        return new SaplingBlockTrait(lightLevel);
    }

    public static List<BlockTrait<?, ?>> withColor(MapColor color, int lightLevel) {
        return Combiner.of(
                PlantBlockTrait.withColor(color, false),
                ticking(lightLevel),
                BlockTraits.MINEABLE_WITH.needsHoe(),
                BlockTraits.LOOT_TABLE.dropSelf(),
                ClientBlockTraits.RENDER_LAYER.cutout(),
                ModCore.isDatagen() ? ClientModel.build(lightLevel) : null,
                CompostableBlockTrait.withDefault(),
                BlockTraits.FLAMMABLE.withDefault()
        ).combine();
    }

    /**
     * Kept in a separate class file (not just an @Environment(CLIENT)-guarded expression):
     * merely creating this lambda - even without ever invoking it - requires resolving the
     * client-only WoverBlockModelGenerators parameter type at the invokedynamic bootstrap site,
     * which throws immediately on a dedicated server. Gating with ModCore.isDatagen() keeps that
     * bootstrap instruction from ever executing there. See PathBlockTrait for the same pattern.
     */
    @Environment(EnvType.CLIENT)
    private static class ClientModel {
        private static BlockModelTrait build(int lightLevel) {
            return ClientBlockTraits.MODEL.with(
                    ((key, block, generator) -> {
                        generator.vanillaGenerator.createCrossBlock(
                                block,
                                lightLevel > 0
                                        ? BlockModelGenerators.PlantType.EMISSIVE_NOT_TINTED
                                        : BlockModelGenerators.PlantType.NOT_TINTED
                        );
                        // Flat item model (item/generated), not the block's cross model: vanilla saplings
                        // use createPlantWithDefaultItem -> registerSimpleFlatItemModel, and delegating to
                        // the cross model instead renders the dropped/held item at full block scale.
                        generator.createFlatItem(block);
                    })
            );
        }
    }

    public final int lightLevel;

    private SaplingBlockTrait(int lightLevel) {
        this.lightLevel = lightLevel;
    }

    @Override
    public BlockTraitKey key() {
        return KEY;
    }

    @Override
    public void configure(BlockDefinition<Block, ? extends BlockDefinition<Block, ?>> definition) {
        super.configure(definition);

        definition.randomTicks()
                  // 26.2 moved the block half of minecraft:saplings out of BlockTags and into
                  // BlockItemTags; ItemTags.SAPLINGS stayed where it was.
                  .addTags(BlockItemTags.SAPLINGS.block(), CommonBlockTags.SAPLINGS)
                  .addItemTags(ItemTags.SAPLINGS, CommonItemTags.SAPLINGS);

        if (lightLevel > 0) {
            definition.lightLevel(state -> lightLevel);
        }
    }
}