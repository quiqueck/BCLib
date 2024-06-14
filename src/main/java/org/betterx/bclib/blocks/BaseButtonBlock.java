package org.betterx.bclib.blocks;

import org.betterx.bclib.api.v3.datagen.DropSelfLootProvider;
import org.betterx.bclib.behaviours.BehaviourHelper;
import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.wover.block.api.BlockTagProvider;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;
import org.betterx.wover.item.api.ItemTagProvider;
import org.betterx.wover.tag.api.event.context.ItemTagBootstrapContext;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public abstract class BaseButtonBlock extends ButtonBlock implements BlockModelProvider, BlockTagProvider, ItemTagProvider, DropSelfLootProvider<BaseButtonBlock> {
    private final Block parent;

    protected BaseButtonBlock(Block parent, Properties properties, BlockSetType type) {
        this(parent, properties, 30, type);
    }

    protected BaseButtonBlock(
            Block parent,
            Properties properties,
            int ticksToStayPressed,
            BlockSetType type
    ) {
        super(
                type, ticksToStayPressed, properties.noCollission()
        );
        this.parent = parent;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void provideBlockModels(WoverBlockModelGenerators generators) {
        generators.modelFor(parent).createButton(this);
    }

    @Override
    public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
        context.add(this, BlockTags.BUTTONS);
    }

    @Override
    public void registerItemTags(ResourceLocation location, ItemTagBootstrapContext context) {
        context.add(this, ItemTags.BUTTONS);
    }

    public static class Metal extends BaseButtonBlock implements BehaviourMetal {
        public Metal(Block source, BlockSetType type) {
            super(source, Properties.ofFullCopy(source).noOcclusion(), type);
        }
    }

    public static class Stone extends BaseButtonBlock implements BehaviourStone {
        public Stone(Block source, BlockSetType type) {
            super(source, Properties.ofFullCopy(source).noOcclusion(), type);
        }

        @Override
        public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
            context.add(this, BlockTags.BUTTONS, BlockTags.STONE_BUTTONS);
        }

        @Override
        public void registerItemTags(ResourceLocation location, ItemTagBootstrapContext context) {
            context.add(this, ItemTags.BUTTONS, ItemTags.STONE_BUTTONS);
        }
    }

    public static class Wood extends BaseButtonBlock implements BehaviourWood {
        public Wood(Block source, BlockSetType type) {
            super(source, Properties.ofFullCopy(source).strength(0.5F, 0.5F).noOcclusion(), type);
        }


        @Override
        public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
            context.add(this, BlockTags.BUTTONS, BlockTags.WOODEN_BUTTONS);
        }

        @Override
        public void registerItemTags(ResourceLocation location, ItemTagBootstrapContext context) {
            context.add(this, ItemTags.BUTTONS, ItemTags.WOODEN_BUTTONS);
        }
    }

    public static BaseButtonBlock from(Block source, BlockSetType type) {
        return BehaviourHelper.from(source, type, Wood::new, Stone::new, Metal::new);
    }
}
