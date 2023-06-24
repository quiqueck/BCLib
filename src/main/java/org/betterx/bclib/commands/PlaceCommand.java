package org.betterx.bclib.commands;

import de.ambertation.wunderlib.math.Bounds;
import de.ambertation.wunderlib.math.Float3;
import org.betterx.bclib.api.v2.levelgen.structures.StructureNBT;
import org.betterx.bclib.commands.arguments.ConnectorArgument;
import org.betterx.bclib.commands.arguments.Float3ArgumentType;
import org.betterx.bclib.commands.arguments.PlacementDirections;
import org.betterx.bclib.commands.arguments.TemplatePlacementArgument;
import org.betterx.bclib.util.BlocksHelper;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

class PlaceCommandBuilder {
    public static final String PATH = "path";
    public static final String NBT = "nbt";
    public static final String EMPTY = "empty";
    public static final String PLACEMENT = "placement";
    public static final String POS = "pos";
    public static final String RECURSION_DEPTH = "recursion_depth";
    public static final String SPAN = "span";
    public static final String BORDER = "border";
    public static final String ADD_CONTROLL_BLOCKS = "controller";
    public static final String FILL_VOID = "replaceair";
    public static final String JIGSAW = "jigsaw";
    public static final String CONNECTOR_NAME = "connector_name";
    public static final String REPLACE_WITH = "replace_with";
    public static final String ROLLABLE = "rollable";
    public static final String REPLACE_FROM_WORLD = "fromWorld";

    public void register(
            CommandBuildContext ctx,
            LiteralArgumentBuilder<CommandSourceStack> command
    ) {
        final Supplier<RequiredArgumentBuilder<CommandSourceStack, ResourceLocation>> path = () -> Commands.argument(
                PATH,
                ResourceLocationArgument.id()
        );
        final Supplier<RequiredArgumentBuilder<CommandSourceStack, PlacementDirections>> placement = () -> Commands.argument(
                PLACEMENT,
                TemplatePlacementArgument.templatePlacement()
        );
        final Supplier<RequiredArgumentBuilder<CommandSourceStack, Coordinates>> pos = () -> Commands.argument(
                POS,
                BlockPosArgument.blockPos()
        );
        final Supplier<RequiredArgumentBuilder<CommandSourceStack, Integer>> recursionDepth = () -> Commands.argument(
                RECURSION_DEPTH,
                IntegerArgumentType.integer(0, 16)
        );
        final Function<Boolean, RequiredArgumentBuilder<CommandSourceStack, PlacementDirections>> placeIt = (hasRecursionArg) -> placement
                .get()
                .then(
                        addOptionalsAndExecute(
                                ctx,
                                pos.get(),
                                hasRecursionArg,
                                PlaceCommandBuilder::placeNBT
                        )
                );

        final var nbtTree = Commands.literal(NBT).then(
                path.get()
                    .then(recursionDepth.get().then(placeIt.apply(true)))
                    .then(placeIt.apply(false))
        );

        final var emptyTree = Commands.literal(EMPTY).then(
                path.get().then(
                        placement.get().then(
                                pos.get().then(
                                        addOptionalsAndExecute(
                                                ctx,
                                                Commands.argument(SPAN, Float3ArgumentType.int3(0, 64)),
                                                false,
                                                PlaceCommandBuilder::placeEmpty
                                        )
                                )
                        )
                )
        );
        final Supplier<RequiredArgumentBuilder<CommandSourceStack, BlockInput>> replace = () -> Commands.argument(
                REPLACE_WITH,
                BlockStateArgument.block(ctx)
        );
        final Supplier<LiteralArgumentBuilder<CommandSourceStack>> replace_source = () -> Commands.literal(
                REPLACE_FROM_WORLD);
        final Supplier<LiteralArgumentBuilder<CommandSourceStack>> rotate = () -> Commands.literal(ROLLABLE);

        final var jigsawTree = Commands.literal(JIGSAW).then(
                Commands.argument(PlaceCommand.POOL, ResourceKeyArgument.key(Registries.TEMPLATE_POOL)).then(
                        Commands.argument(CONNECTOR_NAME, ConnectorArgument.id())
                                .then(replace.get()
                                             .then(rotate.get()
                                                         .then(pos.get()
                                                                  .executes(cc -> placeJigsaw(cc, true, true, false))))
                                             .then(pos.get().executes(cc -> placeJigsaw(cc, true, false, false)))
                                )
                                .then(replace_source.get()
                                                    .then(rotate.get()
                                                                .then(pos.get()
                                                                         .executes(cc -> placeJigsaw(
                                                                                 cc,
                                                                                 false,
                                                                                 true,
                                                                                 true
                                                                         ))))
                                                    .then(pos.get().executes(cc -> placeJigsaw(cc, false, false, true)))
                                )
                                .then(rotate.get()
                                            .then(pos.get().executes(cc -> placeJigsaw(cc, false, true, false))))
                                .then(
                                        pos.get().executes(cc -> placeJigsaw(cc, false, false, false))
                                )

                )
        );


//        final var testSpaner = Commands.literal("spawner")
//                                       .then(pos.get().executes(cc -> placeSpawner(cc)));

        command
                .then(nbtTree)
                .then(emptyTree)
                .then(jigsawTree)
        ;
    }

    private <T> RequiredArgumentBuilder<CommandSourceStack, T> addOptionalsAndExecute(
            CommandBuildContext commandBuildContext,
            RequiredArgumentBuilder<CommandSourceStack, T> root,
            boolean hasRecursionArgs,
            Executor runner
    ) {
        final Supplier<LiteralArgumentBuilder<CommandSourceStack>> addControllers = () -> Commands.literal(
                ADD_CONTROLL_BLOCKS);
        final Supplier<LiteralArgumentBuilder<CommandSourceStack>> replaceAir = () -> Commands.literal(FILL_VOID);
        final Supplier<RequiredArgumentBuilder<CommandSourceStack, BlockInput>> addBorder = () -> Commands.argument(
                BORDER,
                BlockStateArgument.block(commandBuildContext)
        );

        return root
                .executes(c -> runner.exec(c, false, false, false, hasRecursionArgs))
                .then(addBorder.get()
                               .executes(c -> runner.exec(c, true, false, false, hasRecursionArgs))
                               .then(addControllers.get()
                                                   .executes(c -> runner.exec(c, true, true, false, hasRecursionArgs)))
                               .then(addControllers.get()
                                                   .then(replaceAir.get()
                                                                   .executes(c -> runner.exec(
                                                                           c,
                                                                           true,
                                                                           true,
                                                                           true,
                                                                           hasRecursionArgs
                                                                   )))
                               )
                )
                .then(addControllers.get().executes(c -> runner.exec(c, false, true, false, hasRecursionArgs)))
                .then(addControllers.get()
                                    .then(replaceAir.get()
                                                    .executes(c -> runner.exec(
                                                            c,
                                                            false,
                                                            true,
                                                            true,
                                                            hasRecursionArgs
                                                    ))));
    }

    interface Executor {
        int exec(
                CommandContext<CommandSourceStack> ctx,
                boolean hasBorderArg,
                boolean controlBlocks,
                boolean replaceAir,
                boolean hasRecursionArg
        ) throws CommandSyntaxException;
    }

    // /bclib place nbt betternether:city southOf 0 -59 0 controller
    // /bclib place nbt minecraft:village/plains 0 southOf 0 -59 0 controller
    protected static int placeNBT(
            CommandContext<CommandSourceStack> ctx,
            boolean hasBorderArg,
            boolean controlBlocks,
            boolean replaceAir,
            boolean hasRecursionArg
    ) throws CommandSyntaxException {
        final ResourceLocation id = ResourceLocationArgument.getId(ctx, PATH);
        final PlacementDirections searchDir = TemplatePlacementArgument.getPlacement(ctx, PLACEMENT);
        final BlockInput blockInput = hasBorderArg ? BlockStateArgument.getBlock(ctx, BORDER) : null;
        final BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, POS);
        final int recursionDepth = hasRecursionArg ? IntegerArgumentType.getInteger(ctx, RECURSION_DEPTH) : 1;
        final List<StructureNBT> structures = StructureNBT.createResourcesFrom(id, recursionDepth);
        if (structures != null) {
            Bounds b = Bounds.of(pos);
            Bounds all = Bounds.of(pos);
            BlockPos pNew = pos;
            BlockPos rowStart = pos;
            String lastPrefix = null;
            for (var s : structures) {
                String prefix = s.location.getPath().contains("/")
                        ? s.location.getPath().replaceAll("/[^/]*$", "")
                        : "";
                if (lastPrefix != null && !lastPrefix.equals(prefix)) {
                    pNew = searchDir.resetStart(b, pNew, 10);
                    rowStart = pNew;
                    b = Bounds.of(pNew);
                }
                lastPrefix = prefix;

                Bounds bb = Bounds.of(Objects.requireNonNull(PlaceCommand.placeBlocks(
                        ctx.getSource(),
                        rowStart,
                        searchDir.getOffset(),
                        blockInput,
                        controlBlocks,
                        replaceAir,
                        true,
                        s.location,
                        (p) -> s.getBoundingBox(p, Rotation.NONE, Mirror.NONE),
                        (level, p) -> s.generateAt(level, p, Rotation.NONE, Mirror.NONE)
                )));
                rowStart = searchDir.advanceStart(bb, rowStart);
                ;

                b = b.encapsulate(bb);
                all = all.encapsulate(bb);


                if (searchDir.sizeInDirection(b) > 10 * 16) {
                    pNew = searchDir.resetStart(b, pNew);
                    rowStart = pNew;
                    b = Bounds.of(pNew);
                }
            }
            Bounds finalAll = all;
            ctx.getSource()
               .sendSuccess(() -> Component.literal("Placed " + structures.size() + " NBTs: " + finalAll.toString())
                                           .setStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE)), true);


            return 0;
        } else {
            final StructureNBT structureNBT = StructureNBT.create(id);

            return PlaceCommand.placeBlocks(
                    ctx.getSource(),
                    pos,
                    searchDir.getOffset(),
                    blockInput,
                    controlBlocks,
                    replaceAir,
                    true,
                    structureNBT.location,
                    (p) -> structureNBT.getBoundingBox(p, Rotation.NONE, Mirror.NONE),
                    (level, p) -> structureNBT.generateAt(level, p, Rotation.NONE, Mirror.NONE)
            ) == null ? Command.SINGLE_SUCCESS : -1;
        }
    }

    protected static int placeEmpty(
            CommandContext<CommandSourceStack> ctx,
            boolean hasBorderArg,
            boolean controlBlocks,
            boolean replaceAir,
            boolean hasRecursionArg
    ) throws CommandSyntaxException {
        final ResourceLocation id = ResourceLocationArgument.getId(ctx, PATH);
        final PlacementDirections searchDir = TemplatePlacementArgument.getPlacement(ctx, PLACEMENT);
        final BlockInput blockInput = hasBorderArg ? BlockStateArgument.getBlock(ctx, BORDER) : null;
        final BlockPos span = Float3ArgumentType.getFloat3(ctx, SPAN).toBlockPos();

        return PlaceCommand.placeBlocks(
                ctx.getSource(),
                BlockPosArgument.getLoadedBlockPos(ctx, POS),
                searchDir == null || searchDir.dir == Float3.ZERO ? null : searchDir.dir.toBlockPos(),
                blockInput,
                controlBlocks,
                replaceAir,
                false, id,
                (p) -> BoundingBox.fromCorners(p, p.offset(span)),
                (level, p) -> {
                    var box = BoundingBox.fromCorners(p, p.offset(span));
                    PlaceCommand.fillStructureVoid(level, box);
                    if (blockInput != null) {
                        PlaceCommand.fill(
                                level,
                                new BoundingBox(
                                        box.minX(), box.minY() - 1, box.minZ(),
                                        box.maxX(), box.minY() - 1, box.maxZ()
                                ),
                                blockInput.getState()
                        );
                    }
                }
        ) == null ? Command.SINGLE_SUCCESS : -1;
    }

    public static int placeJigsaw(
            CommandContext<CommandSourceStack> ctx,
            boolean hasReplaceArg,
            boolean rotate,
            boolean replaceFromWorld
    ) throws CommandSyntaxException {
        // /bclib place jigsaw betterend:village/center_piece betterend:entrance fromWorld rollable 1235 -60 42
        final BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, POS);
        final Holder.Reference<StructureTemplatePool> pool = ResourceKeyArgument.getStructureTemplatePool(
                ctx,
                PlaceCommand.POOL
        );
        ResourceLocation connector = ResourceLocationArgument.getId(ctx, CONNECTOR_NAME);
        if (connector.getNamespace().equals("-")) {
            connector = new ResourceLocation(pool.key().location().getNamespace(), connector.getPath());
        }
        BlockState replaceWith = hasReplaceArg
                ? BlockStateArgument.getBlock(ctx, REPLACE_WITH).getState()
                : Blocks.AIR.defaultBlockState();

        final ServerLevel level = ctx.getSource().getLevel();
        final ServerPlayer player = ctx.getSource().getPlayer();
        final int deltaY = player.getBlockY() - pos.getY();

        BlockState state = Blocks.JIGSAW.defaultBlockState();


        if (deltaY < 2 && deltaY > -2 && !rotate) {
            state = state.setValue(
                    JigsawBlock.ORIENTATION,
                    FrontAndTop.fromFrontAndTop(player.getDirection().getOpposite(), Direction.UP)
            );
        } else if (deltaY < 0) {
            state = state.setValue(
                    JigsawBlock.ORIENTATION,
                    FrontAndTop.fromFrontAndTop(Direction.DOWN, player.getDirection().getOpposite())
            );
        } else {
            state = state.setValue(
                    JigsawBlock.ORIENTATION,
                    FrontAndTop.fromFrontAndTop(Direction.UP, player.getDirection().getOpposite())
            );
        }

        if (replaceFromWorld) {
            replaceWith = level.getBlockState(pos);
        }
        level.setBlock(pos, state, BlocksHelper.SET_SILENT);
        if (level.getBlockEntity(pos) instanceof JigsawBlockEntity entity) {
            entity.setName(connector);
            entity.setTarget(connector);
            entity.setPool(pool.key());
            entity.setFinalState(BlockStateParser.serialize(replaceWith));
            if (rotate) {
                entity.setJoint(JigsawBlockEntity.JointType.ROLLABLE);
            } else {
                entity.setJoint(JigsawBlockEntity.JointType.ALIGNED);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int placeSpawner(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, POS);
        final ServerLevel level = ctx.getSource().getLevel();

        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), BlocksHelper.SET_SILENT);

        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity entity) {
            CompoundTag tag = TagParser.parseTag(
                    "{SpawnData:{entity:{id:wither_skeleton,PersistenceRequired:1,HandItems:[{Count:1,id:netherite_sword},{Count:1,id:shield}],ArmorItems:[{Count:1,id:netherite_boots,tag:{Enchantments:[{id:protection,lvl:1}]}},{Count:1,id:netherite_leggings,tag:{Enchantments:[{id:protection,lvl:1}]}},{Count:1,id:netherite_chestplate,tag:{Enchantments:[{id:protection,lvl:1},{id:thorns,lvl:3}]}},{Count:1,id:netherite_helmet,tag:{Enchantments:[{id:protection,lvl:1}]}}],HandDropChances:[0.0f,0.0f],ArmorDropChances:[0.0f,0.0f,0.0f,0.0f]}, custom_spawn_rules:{sky_light_limit:{max_inclusive:13},block_light_limit:{max_inclusive:11}}},SpawnRange:4,SpawnCount:8,MaxNearbyEntities:18,Delay:499,MinSpawnDelay:300,MaxSpawnDelay:1600,RequiredPlayerRange:20}");
            entity.load(tag);
        }

        return Command.SINGLE_SUCCESS;
    }
}

public class PlaceCommand {

    public static final String PLACE_COMMAND = "place";
    public static final String POOL = "pool";

    public PlaceCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> register(
            LiteralArgumentBuilder<CommandSourceStack> bnContext,
            CommandBuildContext commandBuildContext
    ) {
        final var command = Commands
                .literal(PLACE_COMMAND)
                .requires(commandSourceStack -> commandSourceStack.hasPermission(2));

        new PlaceCommandBuilder().register(commandBuildContext, command);

        return bnContext.then(command);
    }


    private static boolean isEmpty(Level level, BoundingBox bb) {
        for (int x = bb.minX(); x <= bb.maxX(); x++)
            for (int y = bb.minY(); y <= bb.maxY(); y++)
                for (int z = bb.minZ(); z <= bb.maxZ(); z++)
                    if (!level.isEmptyBlock(new BlockPos(x, y, z))) {
                        return false;
                    }

        return true;
    }

    private static void replaceAir(Level level, BoundingBox bb) {
        BlocksHelper.forAllInBounds(bb, (bp) -> {
            if (level.getBlockState(bp).is(Blocks.AIR)) {
                level.setBlock(bp, Blocks.STRUCTURE_VOID.defaultBlockState(), BlocksHelper.SET_OBSERV);
            }
        });
    }

    private static void removeLootTableSeed(Level level, BoundingBox bb) {
        BlocksHelper.forAllInBounds(bb, (bp) -> {
            if (level.getBlockEntity(bp) instanceof RandomizableContainerBlockEntity rnd) {
                rnd.setLootTable(rnd.lootTable, 0);
            }
        });
    }

    static void fill(Level level, BoundingBox bb, BlockState blockState) {
        BlocksHelper.forAllInBounds(bb, (bp) -> level.setBlock(bp, blockState, BlocksHelper.SET_OBSERV));
    }

    static void fillStructureVoid(Level level, BoundingBox bb) {
        fill(level, bb, Blocks.STRUCTURE_VOID.defaultBlockState());
    }

    //Draws a border around the bounding box
    private static void outline(Level level, BoundingBox bb, BlockState outlineState) {
        BlocksHelper.forOutlineInBounds(bb, (bp) -> level.setBlock(bp, outlineState, BlocksHelper.SET_OBSERV));
    }

    private static BoundingBox adapt(BoundingBox bb, boolean border, boolean structureBlock) {
        if (border) {
            return bb.inflatedBy(1);
        } else if (structureBlock) {
            return new BoundingBox(
                    bb.minX(),
                    bb.minY(),
                    bb.minZ(),
                    bb.maxX() + 1,
                    bb.maxY() + 1,
                    bb.maxZ() + 1
            );
        }
        return bb;
    }

    private static void createControlBlocks(CommandSourceStack stack, ResourceLocation location, BoundingBox bbNBT) {
        BlockPos structureBlockPos = new BlockPos(bbNBT.minX() - 1, bbNBT.minY() - 1, bbNBT.minZ() - 1);
        BlockPos commandBlockPos = new BlockPos(bbNBT.minX() - 1, bbNBT.minY() - 1, bbNBT.minZ());
        BlockPos buttonBlockPos = new BlockPos(bbNBT.minX() - 1, bbNBT.minY(), bbNBT.minZ());
        BlockState state = Blocks.STRUCTURE_BLOCK
                .defaultBlockState()
                .setValue(StructureBlock.MODE, StructureMode.SAVE);

        stack.getLevel().setBlock(structureBlockPos, state, BlocksHelper.SET_OBSERV);
        if (stack.getLevel().getBlockEntity(structureBlockPos) instanceof StructureBlockEntity entity) {
            entity.setIgnoreEntities(false);
            entity.setShowAir(false);
            entity.setMirror(Mirror.NONE);
            entity.setRotation(Rotation.NONE);
            entity.setShowBoundingBox(true);
            entity.setStructureName(location);
            entity.setStructurePos(new BlockPos(1, 1, 1));
            entity.setStructureSize(new Vec3i(bbNBT.getXSpan(), bbNBT.getYSpan(), bbNBT.getZSpan()));
        }

        state = Blocks.COMMAND_BLOCK.defaultBlockState().setValue(CommandBlock.FACING, Direction.DOWN);
        stack.getLevel().setBlock(commandBlockPos, state, BlocksHelper.SET_OBSERV);
        if (stack.getLevel().getBlockEntity(commandBlockPos) instanceof CommandBlockEntity entity) {
            entity.setAutomatic(false);
            entity.setPowered(false);
            entity.onlyOpCanSetNbt();
            entity.getCommandBlock().shouldInformAdmins();
            entity.getCommandBlock()
                  .setCommand(
                          "fill ~1 ~1 ~"
                                  + " ~" + bbNBT.getXSpan() + " ~" + bbNBT.getYSpan() + " ~" + bbNBT.getZSpan()
                                  + " " + BuiltInRegistries.BLOCK.getKey(Blocks.STRUCTURE_VOID)
                                  + " replace " + BuiltInRegistries.BLOCK.getKey(Blocks.AIR)
                  );
        }

        state = Blocks.OAK_BUTTON.defaultBlockState()
                                 .setValue(ButtonBlock.FACING, Direction.EAST)
                                 .setValue(ButtonBlock.FACE, AttachFace.FLOOR);
        stack.getLevel().setBlock(buttonBlockPos, state, BlocksHelper.SET_OBSERV);
    }


    static BoundingBox placeBlocks(
            CommandSourceStack stack,
            BlockPos pos,
            BlockPos searchDir,
            BlockInput blockInput,
            boolean structureBlock,
            boolean replaceAir,
            boolean preFillStructureVoid,
            ResourceLocation location,
            Function<BlockPos, BoundingBox> getBounds,
            BiConsumer<ServerLevel, BlockPos> generate
    ) {
        if (searchDir != null) {
            int tries = 16 * 16;
            while (tries > 0 && !isEmpty(
                    stack.getLevel(),
                    adapt(
                            getBounds.apply(pos),
                            blockInput != null,
                            structureBlock
                    )
            )) {
                pos = pos.offset(searchDir);
                tries--;
            }

            if (tries <= 0) {
                stack.sendFailure(Component.literal("Failed to find free space")
                                           .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
                return null;
            }
        }

        if (structureBlock) {
            pos = pos.offset(1, 0, 1);
        }
        final BoundingBox bbNBT = getBounds.apply(pos);
        final BoundingBox bb;
        if (blockInput != null) {
            bb = adapt(bbNBT, true, structureBlock);
            outline(stack.getLevel(), bb, blockInput.getState());
            stack.sendSuccess(() -> Component.literal("Placed border: " + bb.toString())
                                             .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)), true);
        } else {
            bb = adapt(bbNBT, false, structureBlock);
        }

        if (preFillStructureVoid)
            replaceAir(stack.getLevel(), bb);
        generate.accept(stack.getLevel(), pos);
        if (replaceAir) {
            replaceAir(stack.getLevel(), bbNBT);
        }
        removeLootTableSeed(stack.getLevel(), bbNBT);

        stack.sendSuccess(() -> Component.literal("Placed NBT: " + bbNBT.toString())
                                         .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)), true);

        if (structureBlock) {
            createControlBlocks(stack, location, bbNBT);
        }
        return bb;
    }

    public static BlockState setJigsawOrientation(
            boolean rollable,
            Player player,
            BlockPos pos,
            BlockState state
    ) {
        final int deltaY = player.getBlockY() - pos.getY();
        if (deltaY < 2 && deltaY > -2 && rollable) {
            state = state.setValue(
                    JigsawBlock.ORIENTATION,
                    FrontAndTop.fromFrontAndTop(player.getDirection().getOpposite(), Direction.UP)
            );
        } else if (deltaY < 0) {
            state = state.setValue(
                    JigsawBlock.ORIENTATION,
                    FrontAndTop.fromFrontAndTop(Direction.DOWN, player.getDirection().getOpposite())
            );
        } else {
            state = state.setValue(
                    JigsawBlock.ORIENTATION,
                    FrontAndTop.fromFrontAndTop(Direction.UP, player.getDirection().getOpposite())
            );
        }
        return state;
    }
}

/*

/bclib place nbt "betternether:city/city_building_01" find northOf 0 -59 0 border glass structureblock
/bclib place nbt "betternether:city/city_center_04" find northOf 32 -59 0 border glass structureblock
/bclib place nbt "betternether:city/city_enchanter_02" find northOf 32 -59 0 border glass structureblock
/bclib place nbt "betternether:city/city_library_03" find northOf 64 -59 0 border glass structureblock
/bclib place nbt "betternether:city/city_park_02" find northOf 64 -59 0 border glass structureblock
/bclib place nbt "betternether:city/city_tower_04" find northOf 96 -59 0 border glass structureblock
/bclib place nbt "betternether:city/road_end_02" find northOf 96 -59 0 border glass structureblock
 */
