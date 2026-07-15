package org.betterx.bclib.trait.block;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.api.v2.ComposterAPI;
import org.betterx.wover.block.api.BlockDefinition;
import org.betterx.wover.block.api.trait.BlockTraitKey;
import org.betterx.wover.block.api.trait.GenericBlockTrait;
import org.betterx.wover.block.impl.trait.BlockTraitImpl;
import org.betterx.wover.events.api.WorldLifecycle;
import org.betterx.wover.tag.api.predefined.CommonItemTags;

import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public class CompostableBlockTrait extends BlockTraitImpl<Block, GenericBlockTrait> implements GenericBlockTrait {
    public static final BlockTraitKey KEY = BlockTraitKey.ofUnique(BCLib.C, "compostable");
    private static final CompostableBlockTrait DEFAULT = new CompostableBlockTrait(0.1f);

    // block.asItem() is not reliable yet at afterBlockRegistration time (it only resolves once
    // the block's BlockItem is constructed, which can happen after traits run) - so registration
    // is collected here and flushed once, the first time resources finish loading, by which point
    // every mod's blocks and items are guaranteed to exist.
    private record Pending(float chance, Block block) {}

    private static final List<Pending> PENDING = new ArrayList<>();
    private static boolean flushed = false;

    static {
        WorldLifecycle.RESOURCES_LOADED.subscribe(resourceManager -> {
            if (flushed) return;
            flushed = true;
            PENDING.forEach(p -> ComposterAPI.allowCompost(p.chance(), p.block()));
            PENDING.clear();
        });
    }

    public static CompostableBlockTrait withDefault() {
        return DEFAULT;
    }

    public static CompostableBlockTrait withChance(float chance) {
        return new CompostableBlockTrait(chance);
    }

    public final float compostingChance;

    private CompostableBlockTrait(float compostingChance) {
        this.compostingChance = compostingChance;
    }

    @Override
    public BlockTraitKey key() {
        return KEY;
    }

    @Override
    public GenericBlockTrait forRuntime() {
        return this;
    }

    @Override
    public void configure(BlockDefinition<Block, ? extends BlockDefinition<Block, ?>> definition) {
        super.configure(definition);
        // Tags the block's item as compostable at datagen. This used to be added by
        // BCLAutoItemTagProvider from the BehaviourCompostable marker interface; with that interface
        // gone, the trait itself is now responsible for the tag (the composter chance is handled in
        // afterBlockRegistration below).
        definition.addItemTags(CommonItemTags.COMPOSTABLE);
    }

    @Override
    public void afterBlockRegistration(
            Block block,
            BlockDefinition<Block, ? extends BlockDefinition<Block, ?>> definition
    ) {
        PENDING.add(new Pending(compostingChance, block));
    }
}
