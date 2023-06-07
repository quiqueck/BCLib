package org.betterx.bclib.items;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.client.models.ModelsHelper;
import org.betterx.bclib.commands.PlaceCommand;
import org.betterx.bclib.interfaces.ItemModelProvider;
import org.betterx.bclib.util.BlocksHelper;
import org.betterx.ui.ColorUtil;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class DebugDataItem extends Item implements ItemModelProvider {

    public static final ResourceLocation DEFAULT_ICON = new ResourceLocation("stick");

    interface DebugInteraction {
        InteractionResult use(UseOnContext useOnContext);
    }

    interface DebugEntityInteraction extends DebugInteraction {
        @Override
        default InteractionResult use(UseOnContext useOnContext) {
            var entity = useOnContext.getLevel().getBlockEntity(useOnContext.getClickedPos());
            if (entity != null) {
                return use(useOnContext.getPlayer(), entity, useOnContext);
            }
            return InteractionResult.FAIL;
        }

        InteractionResult use(Player player, BlockEntity entity, UseOnContext useOnContext);
    }

    protected final DebugInteraction interaction;
    protected final ResourceLocation icon;
    public final boolean placeInAir;

    public DebugDataItem(DebugEntityInteraction interaction, boolean placeInAir, ResourceLocation icon) {
        this((DebugInteraction) interaction, placeInAir, icon);
    }

    public DebugDataItem(DebugInteraction interaction, boolean placeInAir, ResourceLocation icon) {
        super(new Item.Properties().fireResistant().stacksTo(1));

        this.interaction = interaction;
        this.icon = (icon == null ? DEFAULT_ICON : icon);
        this.placeInAir = placeInAir;
    }

    @Override
    public boolean isFoil(ItemStack itemStack) {
        return true;
    }


    @Override
    @Environment(EnvType.CLIENT)
    public BlockModel getItemModel(ResourceLocation resourceLocation) {
        return ModelsHelper.createItemModel(icon);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        if (!useOnContext.getPlayer().canUseGameMasterBlocks() || !BCLib.isDevEnvironment()) {
            return InteractionResult.FAIL;
        }
        return interaction.use(useOnContext);
    }

    public static void message(Player player, String text) {
        message(player, text, ColorUtil.GRAY);
    }

    public static void message(Player player, String text, int color) {
        message(player, Component.literal(text).withStyle(Style.EMPTY.withColor(color)));
    }

    public static void message(Player player, Component component) {
        if (player instanceof ServerPlayer sp) {
            sp.sendSystemMessage(component, true);
        }
    }

    @Override
    public boolean canAttackBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        if (placeInAir) {
            final var vec = new Vec3(0, 0, 1)
                    .xRot(-player.getXRot() * Mth.DEG_TO_RAD)
                    .yRot(-player.getYHeadRot() * Mth.DEG_TO_RAD);

            BlockHitResult hit = level.isBlockInLine(new ClipBlockStateContext(
                    player.getEyePosition(),
                    player.getEyePosition().add(vec.scale(4.9)),
                    BlockBehaviour.BlockStateBase::isAir
            ));

            if (hit != null) {
                var result = this.useOn(new UseOnContext(player, interactionHand, hit));

                if (result == InteractionResult.SUCCESS)
                    return InteractionResultHolder.success(player.getItemInHand(interactionHand));
                else if (result == InteractionResult.FAIL)
                    return InteractionResultHolder.fail(player.getItemInHand(interactionHand));
                else if (result == InteractionResult.PASS)
                    return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
                else if (result == InteractionResult.CONSUME)
                    return InteractionResultHolder.consume(player.getItemInHand(interactionHand));
            }
        }

        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }

    public static DebugDataItem forLootTable(ResourceLocation table, Item icon) {
        ResourceLocation iconId = BuiltInRegistries.ITEM.getKey(icon);
        return new DebugDataItem(
                (player, entity, ctx) -> {
                    CompoundTag tag = entity.saveWithoutMetadata();
                    tag.remove(RandomizableContainerBlockEntity.LOOT_TABLE_SEED_TAG);
                    tag.remove("Items");

                    tag.putString(RandomizableContainerBlockEntity.LOOT_TABLE_TAG, table.toString());

                    entity.load(tag);
                    message(player, "Did set Loot Table to " + table.toString());
                    return InteractionResult.SUCCESS;
                },
                false,
                iconId
        );
    }

    public static DebugDataItem forSteetJigSaw(
            String modID,
            ResourceKey<StructureTemplatePool> pool,
            Item icon
    ) {
        return forJigsaw(
                pool == null ? Pools.EMPTY : pool,
                new ResourceLocation(modID, "street"),
                JigsawBlockEntity.JointType.ALIGNED,
                null,
                null,
                icon
        );
    }

    public static DebugDataItem forHouseEntranceJigSaw(
            String modID,
            ResourceKey<StructureTemplatePool> pool,
            Item icon
    ) {
        return forJigsaw(
                pool == null ? Pools.EMPTY : pool,
                new ResourceLocation(modID, "building_entrance"),
                pool == null ? new ResourceLocation("empty") : new ResourceLocation(modID, "street"),
                JigsawBlockEntity.JointType.ALIGNED,
                null,
                null,
                icon
        );
    }

    public static DebugDataItem forDecorationJigSaw(
            String modID,
            ResourceKey<StructureTemplatePool> pool,
            Item icon
    ) {
        return forJigsaw(
                pool == null ? Pools.EMPTY : pool,
                new ResourceLocation(modID, "bottom"),
                pool == null ? new ResourceLocation("empty") : new ResourceLocation(modID, "bottom"),
                JigsawBlockEntity.JointType.ROLLABLE,
                null,
                pool == null ? FrontAndTop.DOWN_WEST : FrontAndTop.UP_WEST,
                icon
        );
    }

    public static DebugDataItem forJigsaw(
            ResourceKey<StructureTemplatePool> pool,
            ResourceLocation connector,
            JigsawBlockEntity.JointType type,
            BlockState finalState,
            FrontAndTop forceOrientation,
            Item icon
    ) {
        return forJigsaw(pool, connector, connector, type, finalState, forceOrientation, icon);
    }

    public static DebugDataItem forJigsaw(
            ResourceKey<StructureTemplatePool> pool,
            ResourceLocation name,
            ResourceLocation target,
            JigsawBlockEntity.JointType type,
            BlockState finalState,
            FrontAndTop forceOrientation,
            Item icon
    ) {
        ResourceLocation iconId = BuiltInRegistries.ITEM.getKey(icon);
        return new DebugDataItem(
                (ctx) -> {
                    final var player = ctx.getPlayer();
                    final var level = ctx.getLevel();
                    final var pos = ctx.getClickedPos();
                    var state = level.getBlockState(pos);
                    var entity = level.getBlockEntity(pos);
                    var targetState = finalState;
                    if (!(entity instanceof JigsawBlockEntity)) {
                        if (targetState == null) {

                            targetState = state.isAir() ? Blocks.STRUCTURE_VOID.defaultBlockState() : state;
                        }

                        state = Blocks.JIGSAW.defaultBlockState();
                        level.setBlock(pos, state, BlocksHelper.SET_SILENT);
                        entity = level.getBlockEntity(pos);

                        message(player, "Created JigSaw at " + pos.toString());
                    }

                    if (entity instanceof JigsawBlockEntity e) {
                        if (forceOrientation == null) {
                            state = PlaceCommand.setJigsawOrientation(
                                    JigsawBlockEntity.JointType.ROLLABLE != type,
                                    player, pos, state
                            );
                        } else {
                            state = state.setValue(JigsawBlock.ORIENTATION, forceOrientation);
                        }
                        level.setBlock(pos, state, BlocksHelper.SET_SILENT);

                        if (pool != null) e.setName(name);
                        if (pool != null) e.setTarget(target);
                        if (pool != null) e.setPool(pool);
                        if (targetState != null) e.setFinalState(BlockStateParser.serialize(targetState));
                        e.setJoint(type);


                        message(player, "Did update Jigsaw at " + pos.toString());

                        return InteractionResult.SUCCESS;
                    }
                    return InteractionResult.FAIL;
                },
                true,
                iconId
        );
    }

}
