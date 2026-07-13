package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.client.models.BCLModels;
import org.betterx.wover.block.api.BlockDefinition;
import org.betterx.wover.block.api.client.trait.BlockModelTrait;
import org.betterx.wover.block.api.client.trait.ClientBlockTraits;
import org.betterx.wover.block.api.trait.*;
import org.betterx.wover.block.api.trait.behaviour.LootTableTrait;
import org.betterx.wover.block.impl.trait.BlockTraitImpl;
import org.betterx.wover.core.api.ModCore;

import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;

public class PathBlockTrait extends BlockTraitImpl<Block, GenericBlockTrait> {
    private static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "path");

    public static List<BlockTrait<?, ?>> withSource(Block source) {
        return Combiner.combine(
                new PathBlockTrait(),
                BlockTraits.LOOT_TABLE.with(drops(source)),
                ModCore.isDatagen() ? ClientModel.build(source) : null
        );
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
        private static BlockModelTrait build(Block source) {
            return ClientBlockTraits.MODEL.with(
                    (key, block, generator) -> {
                        var side = TextureMapping.getBlockTexture(block, "_side");
                        side = ResourceLocation.fromNamespaceAndPath(
                                side.getNamespace(), side
                                        .getPath()
                                        .replace("_path", "")
                        );

                        var mapping = new TextureMapping()
                                .put(TextureSlot.SIDE, side)
                                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_top"))
                                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(source));
                        var location = BCLModels.PATH.create(block, mapping, generator.modelOutput());

                        generator.acceptBlockState(generator.randomTopModelVariant(block, location));
                    });
        }
    }
}