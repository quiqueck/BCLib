package org.betterx.bclib.blocks;

import org.betterx.bclib.behaviours.BehaviourHelper;
import org.betterx.bclib.behaviours.interfaces.BehaviourMetal;
import org.betterx.bclib.behaviours.interfaces.BehaviourStone;
import org.betterx.bclib.behaviours.interfaces.BehaviourWood;
import org.betterx.bclib.client.render.BCLRenderLayer;
import org.betterx.bclib.interfaces.RenderLayerProvider;
import org.betterx.wover.block.api.BlockTagProvider;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;
import org.betterx.wover.item.api.ItemTagProvider;
import org.betterx.wover.tag.api.event.context.ItemTagBootstrapContext;
import org.betterx.wover.tag.api.event.context.TagBootstrapContext;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootParams;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Collections;
import java.util.List;

public abstract class BaseDoorBlock extends DoorBlock implements RenderLayerProvider, BlockModelProvider, BlockTagProvider, ItemTagProvider {
    protected BaseDoorBlock(Block source, BlockSetType type) {
        this(Properties.ofFullCopy(source).strength(3F, 3F).noOcclusion(), type);
    }

    protected BaseDoorBlock(BlockBehaviour.Properties properties, BlockSetType type) {
        super(type, properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER)
            return Collections.singletonList(new ItemStack(this.asItem()));
        else return Collections.emptyList();
    }

    @Override
    public BCLRenderLayer getRenderLayer() {
        return BCLRenderLayer.CUTOUT;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void provideBlockModels(WoverBlockModelGenerators generators) {
        generators.vanillaGenerator.createDoor(this);
    }

    @Override
    public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
        context.add(this, BlockTags.DOORS);
    }

    @Override
    public void registerItemTags(ResourceLocation location, ItemTagBootstrapContext context) {
        context.add(this, ItemTags.DOORS);
    }

    //    @Override
//    @Environment(EnvType.CLIENT)
//    public @Nullable BlockModel getBlockModel(ResourceLocation resourceLocation, BlockState blockState) {
//        DoorType doorType = getDoorType(blockState);
//        Optional<String> pattern = PatternsHelper.createJson(BasePatterns.BLOCK_DOOR_BOTTOM, resourceLocation);
//        switch (doorType) {
//            case TOP_HINGE:
//                pattern = PatternsHelper.createJson(BasePatterns.BLOCK_DOOR_TOP_HINGE, resourceLocation);
//                break;
//            case BOTTOM_HINGE:
//                pattern = PatternsHelper.createJson(BasePatterns.BLOCK_DOOR_BOTTOM_HINGE, resourceLocation);
//                break;
//            case TOP:
//                pattern = PatternsHelper.createJson(BasePatterns.BLOCK_DOOR_TOP, resourceLocation);
//                break;
//            default:
//                break;
//        }
//        return ModelsHelper.fromPattern(pattern);
//    }
//
//    @Override
//    @Environment(EnvType.CLIENT)
//    public UnbakedModel getModelVariant(
//            ModelResourceLocation stateId,
//            BlockState blockState,
//            Map<ResourceLocation, UnbakedModel> modelCache
//    ) {
//        Direction facing = blockState.getValue(FACING);
//        DoorType doorType = getDoorType(blockState);
//        boolean open = blockState.getValue(OPEN);
//        boolean hinge = doorType.isHinge();
//        BlockModelRotation rotation = BlockModelRotation.X0_Y0;
//        switch (facing) {
//            case EAST:
//                if (hinge && open) {
//                    rotation = BlockModelRotation.X0_Y90;
//                } else if (open) {
//                    rotation = BlockModelRotation.X0_Y270;
//                }
//                break;
//            case SOUTH:
//                if (!hinge && !open || hinge && !open) {
//                    rotation = BlockModelRotation.X0_Y90;
//                } else if (hinge) {
//                    rotation = BlockModelRotation.X0_Y180;
//                }
//                break;
//            case WEST:
//                if (!hinge && !open || hinge && !open) {
//                    rotation = BlockModelRotation.X0_Y180;
//                } else if (hinge) {
//                    rotation = BlockModelRotation.X0_Y270;
//                } else {
//                    rotation = BlockModelRotation.X0_Y90;
//                }
//                break;
//            case NORTH:
//            default:
//                if (!hinge && !open || hinge && !open) {
//                    rotation = BlockModelRotation.X0_Y270;
//                } else if (!hinge) {
//                    rotation = BlockModelRotation.X0_Y180;
//                }
//                break;
//        }
//        ModelResourceLocation modelId = RuntimeBlockModelProvider.remapModelResourceLocation(stateId, blockState, "_" + doorType);
//        registerBlockModel(stateId, modelId, blockState, modelCache);
//        return ModelsHelper.createMultiVariant(modelId.id(), rotation.getRotation(), false);
//    }

//    protected DoorType getDoorType(BlockState blockState) {
//        boolean isHinge = isHinge(blockState.getValue(HINGE), blockState.getValue(OPEN));
//        switch (blockState.getValue(HALF)) {
//            case UPPER: {
//                return isHinge ? DoorType.TOP_HINGE : DoorType.TOP;
//            }
//            case LOWER: {
//                return isHinge ? DoorType.BOTTOM_HINGE : DoorType.BOTTOM;
//            }
//        }
//        return DoorType.BOTTOM;
//    }

//    private boolean isHinge(DoorHingeSide hingeSide, boolean open) {
//        boolean isHinge = hingeSide == DoorHingeSide.RIGHT;
//        return isHinge && !open || !isHinge && open;
//    }

//    @Override
//    public void addTags(List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
//        blockTags.add(BlockTags.DOORS);
//        itemTags.add(ItemTags.DOORS);
//    }

//    protected enum DoorType implements StringRepresentable {
//        BOTTOM_HINGE("bottom_hinge"), TOP_HINGE("top_hinge"), BOTTOM("bottom"), TOP("top");
//
//        private final String name;
//
//        DoorType(String name) {
//            this.name = name;
//        }
//
//        public boolean isHinge() {
//            return this == BOTTOM_HINGE || this == TOP_HINGE;
//        }
//
//        @Override
//        public String toString() {
//            return getSerializedName();
//        }
//
//        @Override
//        public String getSerializedName() {
//            return name;
//        }
//    }

    public static class Wood extends BaseDoorBlock implements BehaviourWood {
        public Wood(Block source, BlockSetType type) {
            super(source, type);
        }

        public Wood(Properties properties, BlockSetType type) {
            super(properties, type);
        }

        @Override
        public void registerBlockTags(ResourceLocation location, TagBootstrapContext<Block> context) {
            context.add(this, BlockTags.DOORS, BlockTags.WOODEN_DOORS);
        }

        @Override
        public void registerItemTags(ResourceLocation location, ItemTagBootstrapContext context) {
            context.add(this, ItemTags.DOORS, ItemTags.WOODEN_DOORS);
        }
    }

    public static class Metal extends BaseDoorBlock implements BehaviourMetal {
        public Metal(Block source, BlockSetType type) {
            super(source, type);
        }

        public Metal(Properties properties, BlockSetType type) {
            super(properties, type);
        }
    }

    public static class Stone extends BaseDoorBlock implements BehaviourStone {
        public Stone(Block source, BlockSetType type) {
            super(source, type);
        }

        public Stone(Properties properties, BlockSetType type) {
            super(properties, type);
        }
    }


    public static BaseDoorBlock from(Block source, BlockSetType type) {
        return BehaviourHelper.from(source, type, Wood::new, Stone::new, Metal::new);
    }
}
