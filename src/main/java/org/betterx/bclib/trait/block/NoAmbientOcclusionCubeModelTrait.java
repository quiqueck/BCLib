package org.betterx.bclib.trait.block;

import de.ambertation.wover.block.api.client.trait.BlockModelTrait;
import de.ambertation.wover.block.api.client.trait.ClientBlockTraits;
import de.ambertation.wover.core.api.ModCore;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Optional;

/**
 * A full-cube block model whose parent turns ambient occlusion off, for blocks (BetterEnd's leaves) that used to
 * render through a hand-authored {@code cube_noshade} model but lost it in the wover migration - wover's default
 * cube trait ({@code ModelTraitLibrary.cube()}) always parents {@code minecraft:block/cube_all}, which has ambient
 * occlusion on, darkening the block's interior faces.
 * <p>
 * wover's leaf/cube model traits can't express "no ambient occlusion" (there is no vanilla {@code cube_all}
 * equivalent with {@code "ambientocclusion": false}), so this trait re-adds the option in BCLib: it generates a
 * cube model parented to the supplied no-AO parent (e.g. {@code betterend:block/cube_noshade}, whose single
 * {@code #texture} slot is filled with the block's own texture), matching the recovered hand-authored form. The
 * blockstate and item model are the plain full-cube ones wover's {@code cube()} produced, so only the model's
 * parent (and thus its ambient occlusion) changes.
 * <p>
 * Returns {@code null} outside a datagen environment; see {@link WeightedPillarModelTrait} for the same guard.
 */
public class NoAmbientOcclusionCubeModelTrait {
    /**
     * @param parent the no-ambient-occlusion cube parent model (with a single {@code #texture} slot), e.g.
     *               {@code betterend:block/cube_noshade}
     * @return the model trait, or {@code null} outside of datagen
     */
    public static BlockModelTrait withParent(Identifier parent) {
        return ModCore.isDatagen() ? Impl.withParent(parent) : null;
    }

    @Environment(EnvType.CLIENT)
    private static class Impl {
        private static BlockModelTrait withParent(Identifier parent) {
            final ModelTemplate template = new ModelTemplate(
                    Optional.of(parent),
                    Optional.empty(),
                    TextureSlot.TEXTURE
            );
            return ClientBlockTraits.MODEL.with((key, block, generator) -> {
                final var mapping = new TextureMapping()
                        .put(TextureSlot.TEXTURE, TextureMapping.getBlockTexture(block));
                final var location = template.create(block, mapping, generator.modelOutput());
                generator.acceptBlockState(BlockModelGenerators.createSimpleBlock(
                        block,
                        BlockModelGenerators.plainVariant(location)
                ));
                generator.delegateItemModel(block, location);
            });
        }
    }
}
