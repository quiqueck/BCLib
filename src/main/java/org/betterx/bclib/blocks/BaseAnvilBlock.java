package org.betterx.bclib.blocks;

import org.betterx.bclib.client.models.BCLModels;
import org.betterx.bclib.interfaces.tools.AddMineablePickaxe;
import org.betterx.bclib.items.BaseAnvilItem;
import org.betterx.bclib.util.BCLDataComponents;
import org.betterx.bclib.util.BlocksHelper;
import org.betterx.bclib.util.LootUtil;
import org.betterx.wover.block.api.BlockProperties;
import org.betterx.wover.block.api.CustomBlockItemProvider;
import org.betterx.wover.block.api.model.BlockModelProvider;
import org.betterx.wover.block.api.model.WoverBlockModelGenerators;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.ApiStatus;

public abstract class BaseAnvilBlock extends AnvilBlock implements AddMineablePickaxe, CustomBlockItemProvider, BlockModelProvider {
    public static final IntegerProperty DESTRUCTION = BlockProperties.DESTRUCTION;
    public IntegerProperty durability;

    public BaseAnvilBlock(MapColor color) {
        this(Properties.ofFullCopy(Blocks.ANVIL).mapColor(color));
    }

    public BaseAnvilBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        if (getMaxDurability() != 3) {
            durability = IntegerProperty.create("durability", 0, getMaxDurability());
        } else {
            durability = BlockProperties.DEFAULT_ANVIL_DURABILITY;
        }
        builder.add(DESTRUCTION, durability);
    }

    //    @Override
//    @Environment(EnvType.CLIENT)
//    public BlockModel getItemModel(ResourceLocation blockId) {
//        return getBlockModel(blockId, defaultBlockState());
//    }
//
//    @Override
//    @Environment(EnvType.CLIENT)
//    public @Nullable BlockModel getBlockModel(ResourceLocation blockId, BlockState blockState) {
//        int destruction = blockState.getValue(DESTRUCTION);
//        String name = blockId.getPath();
//        Map<String, String> textures = Maps.newHashMap();
//        textures.put("%modid%", blockId.getNamespace());
//        textures.put("%anvil%", name);
//        textures.put("%top%", name + "_top_" + destruction);
//        Optional<String> pattern = PatternsHelper.createJson(BasePatterns.BLOCK_ANVIL, textures);
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
//        int destruction = blockState.getValue(DESTRUCTION);
//        ModelResourceLocation modelLocation = RuntimeBlockModelProvider.remapModelResourceLocation(stateId, blockState, "_top_" + destruction);
//        registerBlockModel(stateId, modelLocation, blockState, modelCache);
//        return ModelsHelper.createFacingModel(modelLocation.id(), blockState.getValue(FACING), false, false);
//    }

    @Environment(EnvType.CLIENT)
    @Override
    public void provideBlockModels(WoverBlockModelGenerators generator) {
        final ResourceLocation id = TextureMapping.getBlockTexture(this);
        final TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.FRONT, id.withSuffix("_front"))
                .put(TextureSlot.BACK, id.withSuffix("_back"))
                .put(TextureSlot.BOTTOM, id.withSuffix("_bottom"))
                .put(BCLModels.PANEL, id.withSuffix("_panel"));

        final var prop = PropertyDispatch.properties(DESTRUCTION, FACING);

        for (int d = 0; d < 3; d++) {
            mapping.put(TextureSlot.TOP, id.withSuffix("_top_" + d));
            final ResourceLocation model = BCLModels.ANVIL.createWithSuffix(this, "_" + d, mapping, generator.modelOutput());

            prop.select(d, Direction.NORTH, Variant
                    .variant()
                    .with(VariantProperties.MODEL, model)
            );
            prop.select(d, Direction.EAST, Variant
                    .variant()
                    .with(VariantProperties.MODEL, model)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            );
            prop.select(d, Direction.SOUTH, Variant
                    .variant()
                    .with(VariantProperties.MODEL, model)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
            );
            prop.select(d, Direction.WEST, Variant
                    .variant()
                    .with(VariantProperties.MODEL, model)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
            );
        }
        generator.acceptBlockState(MultiVariantGenerator.multiVariant(this).with(prop));
        generator.delegateItemModel(this, id.withSuffix("_0"));
    }

    @Override
    public BlockItem getCustomBlockItem(ResourceLocation blockID, Item.Properties settings) {
        return new BaseAnvilItem(this, settings);
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        int destruction = state.getValue(DESTRUCTION);
        int durability = state.getValue(getDurabilityProp());
        int value = destruction * getMaxDurability() + durability;
        ItemStack tool = builder.getParameter(LootContextParams.TOOL);
        if (LootUtil.isCorrectTool(this, state, tool)) {
            ItemStack itemStack = new ItemStack(this);

            CustomData.update(BCLDataComponents.ANVIL_ENTITY_DATA, itemStack, (compoundTag) -> {
                compoundTag.putInt(BaseAnvilItem.DESTRUCTION, value);
            });

            return Lists.newArrayList(itemStack);
        }
        return Collections.emptyList();
    }

    public IntegerProperty getDurabilityProp() {
        return durability;
    }

    public int getMaxDurability() {
        return 5;
    }

    public BlockState damageAnvilUse(BlockState state, RandomSource random) {
        IntegerProperty durability = getDurabilityProp();
        int value = state.getValue(durability);
        if (value < getMaxDurability()) {
            return state.setValue(durability, value + 1);
        }
        value = state.getValue(DESTRUCTION);
        return value < 2 ? state.setValue(DESTRUCTION, value + 1).setValue(durability, 0) : null;
    }

    public BlockState damageAnvilFall(BlockState state) {
        int destruction = state.getValue(DESTRUCTION);
        return destruction < 2 ? state.setValue(DESTRUCTION, destruction + 1) : null;
    }

    @ApiStatus.Internal
    public static void destroyWhenNull(Level level, BlockPos blockPos, BlockState damaged) {
        if (damaged == null) {
            level.removeBlock(blockPos, false);
            level.levelEvent(LevelEvent.SOUND_ANVIL_BROKEN, blockPos, 0);
        } else {
            level.setBlock(blockPos, damaged, BlocksHelper.FLAG_SEND_CLIENT_CHANGES);
            level.levelEvent(LevelEvent.SOUND_ANVIL_USED, blockPos, 0);
        }
    }
}
