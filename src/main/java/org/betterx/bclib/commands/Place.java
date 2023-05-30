package org.betterx.bclib.commands;

import de.ambertation.wunderlib.math.Float3;
import org.betterx.bclib.api.v2.levelgen.structures.StructureNBT;
import org.betterx.bclib.util.BlocksHelper;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

class PlaceNBT {
    protected <T extends ArgumentBuilder<CommandSourceStack, T>> T execute(
            T builder,
            BlockPos searchDir, boolean border, boolean commandBlock
    ) {
        return builder.executes(commandContext -> placeNBT(
                commandContext.getSource(),
                BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
                StringArgumentType.getString(commandContext, "path"),
                searchDir, border ? BlockStateArgument.getBlock(commandContext, "block") : null, commandBlock
        ));
    }

    protected <T extends ArgumentBuilder<CommandSourceStack, T>> T buildPosArgument(
            CommandBuildContext commandBuildContext,
            T builder,
            BlockPos searchDir
    ) {
        final var posBuilder = Commands.argument(
                "pos",
                BlockPosArgument.blockPos()
        );

        final var posOnly = addPosOnly(searchDir, posBuilder);
        final var all = addPosWithAttributes(commandBuildContext, searchDir, posBuilder);

        return builder.then(posOnly).then(all);
    }

    protected <P extends ArgumentBuilder<CommandSourceStack, P>> P addPosWithAttributes(
            CommandBuildContext commandBuildContext,
            BlockPos searchDir,
            P posBuilder
    ) {
        return posBuilder
                .then(execute(Commands.literal("structureblock"), searchDir, false, true))
                .then(Commands.literal("border")
                              .then(execute(
                                              Commands.argument("block", BlockStateArgument.block(commandBuildContext)),
                                              searchDir,
                                              true,
                                              false
                                      ).then(execute(Commands.literal("structureblock"), searchDir, true, true))
                              )
                );
    }

    protected <P extends ArgumentBuilder<CommandSourceStack, P>> P addPosOnly(
            BlockPos searchDir,
            P posBuilder
    ) {
        return execute(posBuilder, searchDir, false, false);
    }

    public void register(
            CommandBuildContext commandBuildContext,
            Map<String, Float3> directions,
            LiteralArgumentBuilder<CommandSourceStack> command
    ) {

        final LiteralArgumentBuilder<CommandSourceStack> nbtBuilder = Commands
                .literal("nbt")
                .then(Commands
                        .argument("path", StringArgumentType.string())
                        .then(buildPosArgument(commandBuildContext, Commands.literal("at"), null))
                        .then(buildFindCommand(commandBuildContext, directions))
                )
//                .then(Commands.literal("test")
//                              .then(Commands.argument("block", BlockStateArgument.block(commandBuildContext))
//                                            .executes(commandContext -> placeNBT(
//                                                    commandContext.getSource(),
//                                                    new BlockPos(100, 10, 0),
//                                                    "betternether:altar_01",
//                                                    Float3.NORTH.toBlockPos(),
//                                                    BlockStateArgument.getBlock(commandContext, "block"),
//                                                    true
//                                            )))
//                )
                ;


        command.then(nbtBuilder);
    }

    @NotNull
    protected LiteralArgumentBuilder<CommandSourceStack> buildFindCommand(
            CommandBuildContext commandBuildContext,
            Map<String, Float3> directions
    ) {
        final LiteralArgumentBuilder<CommandSourceStack> executeFindFree = Commands.literal("find");
        for (var dir : directions.entrySet()) {
            executeFindFree.then(buildPosArgument(
                    commandBuildContext,
                    Commands.literal(dir.getKey()),
                    dir.getValue().toBlockPos()
            ));
        }
        return executeFindFree;
    }

    static int placeNBT(
            CommandSourceStack stack,
            BlockPos pos,
            String location,
            BlockPos searchDir,
            BlockInput blockInput,
            boolean structureBlock
    ) {
        StructureNBT structureNBT = StructureNBT.create(new ResourceLocation(location));
        return Place.placeBlocks(
                stack,
                pos,
                searchDir,
                blockInput,
                structureBlock,
                structureNBT.location,
                (p) -> structureNBT.getBoundingBox(p, Rotation.NONE, Mirror.NONE),
                (level, p) -> structureNBT.generateAt(level, p, Rotation.NONE, Mirror.NONE)
        );
    }
}

class PlaceEmpty extends PlaceNBT {
    @Override
    protected <T extends ArgumentBuilder<CommandSourceStack, T>> T execute(
            T builder,
            BlockPos searchDir, boolean border, boolean commandBlock
    ) {
        return builder.executes(commandContext -> placeEmpty(
                commandContext.getSource(),
                BlockPosArgument.getLoadedBlockPos(commandContext, "pos"),
                new BlockPos(
                        IntegerArgumentType.getInteger(commandContext, "spanX"),
                        IntegerArgumentType.getInteger(commandContext, "spanY"),
                        IntegerArgumentType.getInteger(commandContext, "spanZ")
                ),
                StringArgumentType.getString(commandContext, "path"),
                searchDir, border ? BlockStateArgument.getBlock(commandContext, "block") : null, commandBlock
        ));
    }

    @Override
    protected <T extends ArgumentBuilder<CommandSourceStack, T>> T buildPosArgument(
            CommandBuildContext commandBuildContext,
            T builder,
            BlockPos searchDir
    ) {
        final var spanX = Commands.argument(
                "spanX",
                IntegerArgumentType.integer(0, 64)
        );
        final var spanY = Commands.argument(
                "spanY",
                IntegerArgumentType.integer(0, 64)
        );
        final var spanZ = Commands.argument(
                "spanZ",
                IntegerArgumentType.integer(0, 64)
        );
        final var posBuilder = Commands.argument(
                "pos",
                BlockPosArgument.blockPos()
        );

        final var posOnly = addPosOnly(searchDir, spanZ);
        final var all = addPosWithAttributes(commandBuildContext, searchDir, spanZ);

        return builder.then(posBuilder.then(spanX.then(spanY.then(posOnly).then(all))));
    }

    @Override
    public void register(
            CommandBuildContext commandBuildContext,
            Map<String, Float3> directions,
            LiteralArgumentBuilder<CommandSourceStack> command
    ) {

        final LiteralArgumentBuilder<CommandSourceStack> nbtBuilder = Commands
                .literal("empty")
                .then(Commands
                        .argument("path", StringArgumentType.string())
                        .then(buildPosArgument(commandBuildContext, Commands.literal("at"), null))
                        .then(buildFindCommand(commandBuildContext, directions))
                );


        command.then(nbtBuilder);
    }

    private static int placeEmpty(
            CommandSourceStack stack,
            BlockPos start,
            BlockPos span,
            String location,
            BlockPos searchDir,
            BlockInput blockInput,
            boolean structureBlock
    ) {
        return Place.placeBlocks(
                stack,
                start,
                searchDir,
                blockInput,
                structureBlock,
                new ResourceLocation(location),
                (p) -> BoundingBox.fromCorners(p, p.offset(span)),
                (level, p) -> {
                    var box = BoundingBox.fromCorners(p, p.offset(span));
                    Place.fillStructureVoid(level, box);
                    if (blockInput != null) {
                        Place.fill(
                                level,
                                new BoundingBox(
                                        box.minX(),
                                        box.minY() - 1,
                                        box.minZ(),
                                        box.maxX(),
                                        box.minY() - 1,
                                        box.maxZ()
                                ),
                                blockInput.getState()
                        );
                    }
                }
        );
    }
}

public class Place {
    public Place() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> register(
            LiteralArgumentBuilder<CommandSourceStack> bnContext,
            CommandBuildContext commandBuildContext
    ) {
        final Map<String, Float3> directions = Map.of(
                "northOf", Float3.NORTH,
                "southOf", Float3.SOUTH,
                "eastOf", Float3.EAST,
                "westOf", Float3.WEST,
                "above", Float3.UP,
                "below", Float3.DOWN
        );

        final var command = Commands
                .literal("place")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(2));

        new PlaceNBT().register(commandBuildContext, directions, command);
        new PlaceEmpty().register(commandBuildContext, directions, command);

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
        for (int x = bb.minX(); x <= bb.maxX(); x++)
            for (int y = bb.minY(); y <= bb.maxY(); y++)
                for (int z = bb.minZ(); z <= bb.maxZ(); z++) {
                    BlockPos bp = new BlockPos(x, y, z);
                    if (level.getBlockState(bp).is(Blocks.AIR)) {
                        level.setBlock(bp, Blocks.STRUCTURE_VOID.defaultBlockState(), BlocksHelper.SET_OBSERV);
                    }
                }
    }

    static void fill(Level level, BoundingBox bb, BlockState blockState) {
        for (int x = bb.minX(); x <= bb.maxX(); x++)
            for (int y = bb.minY(); y <= bb.maxY(); y++)
                for (int z = bb.minZ(); z <= bb.maxZ(); z++) {
                    BlockPos bp = new BlockPos(x, y, z);
                    level.setBlock(bp, blockState, BlocksHelper.SET_OBSERV);
                }
    }

    static void fillStructureVoid(Level level, BoundingBox bb) {
        fill(level, bb, Blocks.STRUCTURE_VOID.defaultBlockState());
    }

    //Draws a border around the bounding box
    private static void outline(Level level, BoundingBox bb, BlockState outlineState) {
        for (int x = bb.minX(); x <= bb.maxX(); x++) {
            level.setBlock(new BlockPos(x, bb.minY(), bb.minZ()), outlineState, BlocksHelper.SET_OBSERV);
            level.setBlock(new BlockPos(x, bb.maxY(), bb.minZ()), outlineState, BlocksHelper.SET_OBSERV);
            level.setBlock(new BlockPos(x, bb.minY(), bb.maxZ()), outlineState, BlocksHelper.SET_OBSERV);
            level.setBlock(new BlockPos(x, bb.maxY(), bb.maxZ()), outlineState, BlocksHelper.SET_OBSERV);
        }
        for (int y = bb.minY(); y <= bb.maxY(); y++) {
            level.setBlock(new BlockPos(bb.minX(), y, bb.minZ()), outlineState, BlocksHelper.SET_OBSERV);
            level.setBlock(new BlockPos(bb.maxX(), y, bb.minZ()), outlineState, BlocksHelper.SET_OBSERV);
            level.setBlock(new BlockPos(bb.minX(), y, bb.maxZ()), outlineState, BlocksHelper.SET_OBSERV);
            level.setBlock(new BlockPos(bb.maxX(), y, bb.maxZ()), outlineState, BlocksHelper.SET_OBSERV);
        }
        for (int z = bb.minZ(); z <= bb.maxZ(); z++) {
            level.setBlock(new BlockPos(bb.minX(), bb.minY(), z), outlineState, BlocksHelper.SET_OBSERV);
            level.setBlock(new BlockPos(bb.maxX(), bb.minY(), z), outlineState, BlocksHelper.SET_OBSERV);
            level.setBlock(new BlockPos(bb.minX(), bb.maxY(), z), outlineState, BlocksHelper.SET_OBSERV);
            level.setBlock(new BlockPos(bb.maxX(), bb.maxY(), z), outlineState, BlocksHelper.SET_OBSERV);
        }
    }

    private static BoundingBox adapt(BoundingBox bb, boolean border, boolean structureBlock) {
        if (border) {
            return bb.inflatedBy(1);
        } else if (structureBlock) {
            return new BoundingBox(
                    bb.minX() - 1,
                    bb.minY() - 1,
                    bb.minZ() - 1,
                    bb.maxX(),
                    bb.maxY(),
                    bb.maxZ()
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
            entity.setShowAir(true);
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


    static int placeBlocks(
            CommandSourceStack stack,
            BlockPos pos,
            BlockPos searchDir,
            BlockInput blockInput,
            boolean structureBlock,
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
                return -1;
            }
        }

        final BoundingBox bbNBT = getBounds.apply(pos);
        if (blockInput != null) {
            final BoundingBox bb = bbNBT.inflatedBy(1);
            outline(stack.getLevel(), bb, blockInput.getState());
            stack.sendSuccess(() -> Component.literal("Placed border: " + bb.toString())
                                             .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)), true);
        }

        generate.accept(stack.getLevel(), pos);
        replaceAir(stack.getLevel(), bbNBT);

        stack.sendSuccess(() -> Component.literal("Placed NBT: " + bbNBT.toString())
                                         .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)), true);

        if (structureBlock) {
            createControlBlocks(stack, location, bbNBT);
        }
        return Command.SINGLE_SUCCESS;
    }


}
