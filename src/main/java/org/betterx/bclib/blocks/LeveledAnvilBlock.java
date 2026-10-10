package org.betterx.bclib.blocks;

import org.betterx.bclib.client.models.BCLModels;
import org.betterx.bclib.items.BaseAnvilItem;
import org.betterx.bclib.trait.block.BCLModelKeys;
import org.betterx.bclib.util.BCLDataComponents;
import org.betterx.bclib.util.BlocksHelper;
import org.betterx.bclib.util.LegacyTiers;
import org.betterx.bclib.util.LootUtil;
import de.ambertation.wover.block.api.BlockProperties;
import de.ambertation.wover.block.api.client.model.ClientBlockModelRegistry;
import de.ambertation.wover.block.api.client.trait.BlockModelTrait.ModelFactory;
import de.ambertation.wover.block.api.model.BlockModelBinding;
import de.ambertation.wover.block.api.model.ModelKey;
import de.ambertation.wover.block.api.trait.BlockTraitLookup;
import de.ambertation.wover.core.api.ModCore;
import de.ambertation.wover.sets.api.blocks.BlockSet;

import static net.minecraft.client.data.models.BlockModelGenerators.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A vanilla {@link AnvilBlock} with a durability track (the {@code destruction}/{@code durability}
 * blockstate properties), custom drop that preserves accumulated destruction, and a crafting
 * {@link #getCraftingLevel() level} used to gate recipes.
 * <p>
 * The custom {@link BaseAnvilItem} is no longer provided implicitly - register it at the block's
 * registration site with {@code .withBlockItem((def, block) -> new BlockItemDefinition<>(def,
 * id -> new BaseAnvilItem((LeveledAnvilBlock) block, id.getProperties())))}.
 * <p>
 * The block model is likewise no longer provided implicitly - register {@link #buildModel} at the
 * registration site (guarded by {@code ModCore.isDatagen()}).
 */
public class LeveledAnvilBlock extends AnvilBlock {
    public static final IntegerProperty DESTRUCTION = BlockProperties.DESTRUCTION;
    public IntegerProperty durability;
    protected final int level;

    public LeveledAnvilBlock(BlockBehaviour.Properties properties, int level) {
        super(properties);
        this.level = level;
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

    /**
     * Kept in a separate class file (not just an @Environment(CLIENT) method) since
     * LeveledAnvilBlock is always loaded on the server; a lambda body's synthetic method does not
     * inherit the annotation from its enclosing method, so leaving it here would strand vanilla
     * client-only type references in a class file the server actually has to verify.
     */
    public static BlockModelBinding buildModel(BlockSet<?> set, BlockTraitLookup traitLookup) {
        return BlockModelBinding.of(MODEL, null);
    }

    private static final ModelKey<Void> MODEL = BCLModelKeys.key("leveled_anvil_block");

    /**
     * Registers the client generators behind this class's model keys. Called from the mod's
     * {@code wover.client.traits} entrypoint.
     */
    @Environment(EnvType.CLIENT)
    public static void registerClientModels() {
        ClientModel.register();
    }

    @Environment(EnvType.CLIENT)
    private static class ClientModel {
        private static void register() {
            ClientBlockModelRegistry.register(
                    MODEL,
                    (key, block, generator, payload) -> build().provideBlockModels(key, block, generator)
            );
        }


        private static ModelFactory build() {
            return (key, block, generator) -> {
                final ResourceLocation id = TextureMapping.getBlockTexture(block);
                final TextureMapping mapping = new TextureMapping()
                        .put(TextureSlot.FRONT, id.withSuffix("_front"))
                        .put(TextureSlot.BACK, id.withSuffix("_back"))
                        .put(TextureSlot.BOTTOM, id.withSuffix("_bottom"))
                        .put(BCLModels.PANEL, id.withSuffix("_panel"));

                final var prop = PropertyDispatch.initial(DESTRUCTION, FACING);

                for (int d = 0; d < 3; d++) {
                    mapping.put(TextureSlot.TOP, id.withSuffix("_top_" + d));
                    final ResourceLocation modelLocation = BCLModels.ANVIL.createWithSuffix(
                            block,
                            "_" + d,
                            mapping,
                            generator.modelOutput()
                    );
                    final var model = plainVariant(modelLocation);

                    prop.select(d, Direction.NORTH, model);
                    prop.select(d, Direction.EAST, model.with(Y_ROT_90));
                    prop.select(d, Direction.SOUTH, model.with(Y_ROT_180));
                    prop.select(d, Direction.WEST, model.with(Y_ROT_270));
                }
                generator.acceptBlockState(MultiVariantGenerator.dispatch(block).with(prop));
                generator.delegateItemModel(block, id.withSuffix("_0"));
            };
        }
    }

    @Override
    public @NotNull List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        int destruction = state.getValue(DESTRUCTION);
        int durability = state.getValue(getDurabilityProp());
        ItemStack tool = builder.getParameter(LootContextParams.TOOL);
        if (LootUtil.isCorrectTool(this, state, tool)) {
            ItemStack itemStack = new ItemStack(this);

            CustomData.update(
                    BCLDataComponents.ANVIL_ENTITY_DATA,
                    itemStack,
                    (compoundTag) -> {
                        // two separate keys - a combined destruction * max + durability is ambiguous, as
                        // durability ranges 0..max inclusive
                        compoundTag.putInt(BaseAnvilItem.DESTRUCTION, destruction);
                        compoundTag.putInt(BaseAnvilItem.DURABILITY, durability);
                    }
            );

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

    public BlockState damageAnvilUse(BlockState state) {
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

    public int getCraftingLevel() {
        return level;
    }

    public static int getAnvilCraftingLevel(Block anvil) {
        if (anvil instanceof LeveledAnvilBlock l) return l.getCraftingLevel();
        if (anvil == Blocks.ANVIL || anvil == Blocks.CHIPPED_ANVIL || anvil == Blocks.DAMAGED_ANVIL)
            return LegacyTiers.IRON.level - 1;
        return 0;
    }

    public static boolean canHandle(Block anvil, int level) {
        return getAnvilCraftingLevel(anvil) >= level;
    }

    public static List<Block> getAnvils() {
        return BuiltInRegistries.BLOCK
                .stream()
                .filter(b -> b instanceof LeveledAnvilBlock || b == Blocks.ANVIL)
                .toList();
    }

    public static List<FormattedCharSequence> getNamesForLevel(int level) {
        MutableComponent names = getAnvils()
                .stream()
                .filter(b -> canHandle(b, level))
                .map(Block::getName)
                .reduce(
                        null,
                        (p, c) -> p == null ? c : p.append(net.minecraft.network.chat.Component.literal(", ")).append(c)
                );
        if (names == null) return List.of();
        return Minecraft.getInstance().font.split(names, 200);
    }
}
