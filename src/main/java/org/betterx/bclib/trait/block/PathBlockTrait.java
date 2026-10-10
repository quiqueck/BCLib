package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.blocks.BaseTerrainBlock;
import org.betterx.bclib.client.models.BCLModels;
import de.ambertation.wover.block.api.BlockDefinition;
import de.ambertation.wover.block.api.client.model.ClientBlockModelRegistry;
import de.ambertation.wover.block.api.client.trait.BlockModelTrait.ModelFactory;
import de.ambertation.wover.block.api.model.BlockModelBinding;
import de.ambertation.wover.block.api.model.ModelKey;
import de.ambertation.wover.block.api.trait.*;
import de.ambertation.wover.block.api.trait.BlockTrait;
import de.ambertation.wover.block.api.trait.behaviour.LootTableTrait;
import de.ambertation.wover.block.impl.trait.BlockTraitImpl;
import de.ambertation.wover.core.api.ModCore;

import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;

public class PathBlockTrait extends BlockTraitImpl<Block, GenericBlockTrait> {
    private static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "path");

    public static List<BlockTrait<?, ?>> withSource(Block source) {
        return withSource(source, true);
    }

    /**
     * @param generateModel when {@code false}, the default path block/item model is <em>not</em> attached, so
     *                      the block can supply its own (e.g. a multi-variant rotated path routed through a
     *                      dedicated model trait). Defaults to {@code true} for the standard single path model.
     */
    public static List<BlockTrait<?, ?>> withSource(Block source, boolean generateModel) {
        return Combiner.of(
                new PathBlockTrait(),
                BlockTraits.LOOT_TABLE.with(drops(source)),
                // Paths inherit reqTool=true from their terrain parent (via replacePropertiesWithCopy) but
                // no mineable tag, which makes them unharvestable. Match the parent terrain's pickaxe tag.
                BlockTraits.MINEABLE_WITH.needsPickAxe(),
                generateModel ? BlockModelBinding.of(MODEL, new ModelParams(source)) : null
        ).combine();
    }

    private PathBlockTrait() {
    }

    @Override
    public BlockTraitKey key() {
        return KEY;
    }

    @Override
    public void configure(BlockDefinition<Block, ? extends BlockDefinition<Block, ?>> definition) {
        definition.isValidSpawn((state, world, pos, type) -> false);
    }

    public static LootTableTrait.LootTableFactory drops(Block source) {
        return (tableKey, blockKey, block, provider) -> {
            return provider.dropWithSilkTouch(block, source, ConstantValue.exactly(1));
        };
    }

    private record ModelParams(Block source) {
    }

    private static final ModelKey<ModelParams> MODEL = BCLModelKeys.key("path_block_trait");

    /**
     * Registers the client generators behind this class's model keys. Called from the mod's
     * {@code wover.client.traits} entrypoint.
     */
    @Environment(EnvType.CLIENT)
    public static void registerClientModels() {
        ClientModel.register();
    }

    /**
     * The model-building lambda below references vanilla client-only datagen types
     * (e.g. {@code BlockModelDefinitionGenerator}). Those types must not leak into
     * {@link PathBlockTrait}'s own class file, since loading that class (e.g. just to
     * call {@link #withSource}) forces the JVM to verify every method declared in it -
     * including synthetic lambda bodies - even on a dedicated server where this branch
     * is never taken. Keeping it in a separate class file means it is only verified if
     * actually loaded, which only happens when {@code ModCore.isDatagen()} is true.
     */
    @Environment(EnvType.CLIENT)
    private static class ClientModel {
        private static void register() {
            ClientBlockModelRegistry.register(
                    MODEL,
                    (key, block, generator, payload) -> buildFrom(payload).provideBlockModels(key, block, generator)
            );
        }

        private static ModelFactory buildFrom(ModelParams p) {
            return build(p.source());
        }

        private static ModelFactory build(Block source) {
            return (key, block, generator) -> {
                var side = TextureMapping.getBlockTexture(block, "_side").sprite();
                side = Identifier.fromNamespaceAndPath(
                        side.getNamespace(), side
                                .getPath()
                                .replace("_path", "")
                );

                // A terrain block's own "bottom" is its base block (e.g. end_stone), not a
                // texture named after the terrain block itself - which doesn't exist on disk.
                var bottomSource = source instanceof BaseTerrainBlock terrain
                        ? terrain.getBaseBlock()
                        : source;

                var mapping = new TextureMapping()
                        .put(TextureSlot.SIDE, new Material(side))
                        .put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_top"))
                        .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(bottomSource));
                var location = BCLModels.PATH.create(block, mapping, generator.modelOutput());

                generator.acceptBlockState(generator.randomTopModelVariant(block, location));
                generator.delegateItemModel(block, location);
            };
        }
    }
}